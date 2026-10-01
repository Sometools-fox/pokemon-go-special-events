package tw.sometools.pogocollector
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID
data class InboxItem(val id:String,val createdAt:String,val type:String,val text:String?,val uris:List<String>,val status:String,val syncStatus:String="local")
class InboxStore(ctx:Context){private val p=ctx.getSharedPreferences("inbox",Context.MODE_PRIVATE)
 fun list():MutableList<InboxItem>{val a=JSONArray(p.getString("items","[]"));return MutableList(a.length()){i->val o=a.getJSONObject(i);val u=o.getJSONArray("uris");InboxItem(o.getString("id"),o.getString("createdAt"),o.getString("type"),o.optString("text").ifBlank{null},(0 until u.length()).map{u.getString(it)},o.getString("status"),o.optString("syncStatus","local"))}.sortedByDescending{it.createdAt}.toMutableList()}
 @Synchronized fun add(type:String,text:String?,uris:List<String>){val x=list();x.add(InboxItem(UUID.randomUUID().toString(),Instant.now().toString(),type,text,uris,"pending"));save(x)}
 @Synchronized fun toggle(id:String)=save(list().map{if(it.id==id)it.copy(status=if(it.status=="pending")"processed" else "pending") else it})
 @Synchronized fun markSynced(id:String)=save(list().map{if(it.id==id)it.copy(syncStatus="synced") else it})
 @Synchronized fun delete(id:String)=save(list().filter{it.id!=id})
 @Synchronized fun clearSynced()=save(list().filter{it.syncStatus!="synced"})
 private fun save(xs:List<InboxItem>){val a=JSONArray();xs.forEach{x->a.put(JSONObject().put("id",x.id).put("createdAt",x.createdAt).put("type",x.type).put("text",x.text?:"").put("uris",JSONArray(x.uris)).put("status",x.status).put("syncStatus",x.syncStatus))};p.edit().putString("items",a.toString()).commit()}}
