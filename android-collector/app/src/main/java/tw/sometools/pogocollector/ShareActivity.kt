package tw.sometools.pogocollector

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class ShareActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val sourceUris = mutableListOf<Uri>()

        when (intent.action) {
            Intent.ACTION_SEND ->
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let(sourceUris::add)
            Intent.ACTION_SEND_MULTIPLE ->
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let(sourceUris::addAll)
        }

        val savedUris = sourceUris.mapNotNull { copyToPrivateInbox(it) }
        if (sourceUris.isNotEmpty() && savedUris.size != sourceUris.size) {
            Toast.makeText(this, "圖片儲存失敗，未加入待處理", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val type = when {
            savedUris.size > 1 -> "images"
            savedUris.size == 1 -> "image"
            sharedText?.startsWith("http") == true -> "link"
            else -> "text"
        }

        InboxStore(this).add(type, sharedText, savedUris)
        Toast.makeText(this, "✓ 已加入待處理", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun copyToPrivateInbox(uri: Uri): String? {
        return try {
            val mime = contentResolver.getType(uri) ?: "image/jpeg"
            val ext = when (mime) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val dir = File(filesDir, "shared-images").apply { mkdirs() }
            val out = File(dir, "${System.currentTimeMillis()}-${uri.hashCode()}.$ext")
            val input = contentResolver.openInputStream(uri) ?: return null
            input.use { source ->
                FileOutputStream(out).use { target -> source.copyTo(target) }
            }
            Uri.fromFile(out).toString()
        } catch (_: Exception) {
            null
        }
    }
}
