package tw.sometools.pogocollector
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
class ShareActivity:Activity(){override fun onCreate(b:Bundle?){super.onCreate(b);val text=intent.getStringExtra(Intent.EXTRA_TEXT);val uris=mutableListOf<String>();if(intent.action==Intent.ACTION_SEND)intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let{uris.add(it.toString())}else if(intent.action==Intent.ACTION_SEND_MULTIPLE)intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.forEach{uris.add(it.toString())};val type=when{uris.size>1->"images";uris.size==1->"image";text?.startsWith("http")==true->"link";else->"text"};InboxStore(this).add(type,text,uris);Toast.makeText(this,"✓ 已加入待處理",Toast.LENGTH_SHORT).show();finish()}}
