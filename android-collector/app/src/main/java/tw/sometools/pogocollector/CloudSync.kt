package tw.sometools.pogocollector

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

object CloudSync {
 fun upload(context:Context,item:InboxItem,onDone:(Boolean,String)->Unit){
  val auth=FirebaseAuth.getInstance()
  fun start(){
   val uid=auth.currentUser?.uid?:return onDone(false,"登入失敗")
   if(item.type=="image" || item.type=="images") uploadImages(context,item,uid,onDone)
   else writeInbox(item,uid,emptyList(),onDone)
  }
  if(auth.currentUser!=null) start() else auth.signInAnonymously()
   .addOnSuccessListener{start()}.addOnFailureListener{onDone(false,it.message?:"匿名登入失敗")}
 }

 private fun uploadImages(context:Context,item:InboxItem,uid:String,onDone:(Boolean,String)->Unit){
  if(item.uris.isEmpty()) return onDone(false,"找不到圖片附件")
  val storage=FirebaseStorage.getInstance()
  val paths=MutableList<String?>(item.uris.size){null}
  var remaining=item.uris.size
  var failed=false
  item.uris.forEachIndexed{index,raw->
   val uri=Uri.parse(raw)
   val mime=context.contentResolver.getType(uri)?:"image/jpeg"
   val ext=when(mime){"image/png"->"png";"image/webp"->"webp";else->"jpg"}
   val path="collector/$uid/${item.id}/${UUID.randomUUID()}.$ext"
   storage.reference.child(path).putFile(uri)
    .addOnSuccessListener{
     paths[index]=path
     remaining--
     if(remaining==0 && !failed) writeInbox(item,uid,paths.filterNotNull(),onDone)
    }
    .addOnFailureListener{
     if(!failed){failed=true;onDone(false,it.message?:"圖片上傳失敗")}
    }
  }
 }

 private fun writeInbox(item:InboxItem,uid:String,attachments:List<String>,onDone:(Boolean,String)->Unit){
  val data=hashMapOf<String,Any>(
   "protocolVersion" to 2,
   "clientItemId" to item.id,
   "createdAt" to item.createdAt,
   "type" to item.type,
   "text" to (item.text?:""),
   "attachmentCount" to attachments.size,
   "attachments" to attachments.map{hashMapOf("storagePath" to it)},
   "status" to "pending",
   "ownerUid" to uid,
   "uploadedAt" to FieldValue.serverTimestamp()
  )
  FirebaseFirestore.getInstance().collection("collectorInbox").document(item.id)
   .set(data).addOnSuccessListener{onDone(true,"已同步")}
   .addOnFailureListener{onDone(false,it.message?:"同步失敗")}
 }
}
