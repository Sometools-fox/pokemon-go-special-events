package tw.sometools.pogocollector

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

class ShareActivity:Activity(){
 override fun onCreate(b:Bundle?){
  super.onCreate(b)
  val text=intent.getStringExtra(Intent.EXTRA_TEXT)
  val sourceUris=mutableListOf<Uri>()
  if(intent.action==Intent.ACTION_SEND) {
   intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let{sourceUris.add(it)}
  } else if(intent.action==Intent.ACTION_SEND_MULTIPLE) {
   intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.forEach{sourceUris.add(it)}
  }

  val savedUris=sourceUris.mapNotNull{copyToPrivateInbox(it)}
  if(sourceUris.isNotEmpty() && savedUris.size!=sourceUris.size){
   Toast.makeText(this,"圖片儲存失敗，未加入待處理",Toast.LENGTH_LONG).show()
   finish()
   return
  }

  val type=when{
   savedUris.size>1->"images"
   savedUris.size==1->"image"
   text?.startsWith("http")==true->"link"
   else->"text"
  }
  InboxStore(this).add(type,text,savedUris)
  Toast.makeText(this,"✓ 已加入待處理",Toast.LENGTH_SHORT).show()
  finish()
 }

 private fun copyToPrivateInbox(uri:Uri):String? {\n  return try {
  val mime=contentResolver.getType(uri)?:"image/jpeg"
  val ext=when(mime){"image/png"->"png";"image/webp"->"webp";else->"jpg"}
  val dir=File(filesDir,"shared-images").apply{mkdirs()}
  val out=File(dir,"${System.currentTimeMillis()}-${uri.hashCode()}.$ext")
  contentResolver.openInputStream(uri)?.use{input->
   FileOutputStream(out).use{output->input.copyTo(output)}
  } ?: return null
  Uri.fromFile(out).toString()
 } catch(_:Exception){ null }
}
