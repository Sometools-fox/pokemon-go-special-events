package tw.sometools.pogocollector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object CloudSync {
 fun upload(item:InboxItem,onDone:(Boolean,String)->Unit){
  if(item.type!="link" && item.type!="text"){onDone(false,"圖片同步尚未開放");return}
  val auth=FirebaseAuth.getInstance()
  fun write(){
   val uid=auth.currentUser?.uid?:return onDone(false,"登入失敗")
   val data=hashMapOf<String,Any>(
    "protocolVersion" to 1,
    "clientItemId" to item.id,
    "createdAt" to item.createdAt,
    "type" to item.type,
    "text" to (item.text?:""),
    "attachmentCount" to 0,
    "status" to "pending",
    "ownerUid" to uid,
    "uploadedAt" to FieldValue.serverTimestamp()
   )
   FirebaseFirestore.getInstance().collection("collectorInbox").document(item.id)
    .set(data).addOnSuccessListener{onDone(true,"已同步")}
    .addOnFailureListener{onDone(false,it.message?:"同步失敗")}
  }
  if(auth.currentUser!=null) write() else auth.signInAnonymously()
   .addOnSuccessListener{write()}.addOnFailureListener{onDone(false,it.message?:"匿名登入失敗")}
 }
}
