package android.media
import android.content.Context
object AudioManager{const val STREAM_MUSIC=3}
class SoundPool(val max:Int,type:Int,quality:Int){
 data class Stream(val resource:Int,var left:Float,var right:Float,val loop:Int,var rate:Float,val priority:Int)
 val live=HashMap<Int,Stream>();var callback:((SoundPool,Int,Int)->Unit)?=null;var next=1;var dead=false;var plays=0
 init{last=this}
 fun setOnLoadCompleteListener(f:(SoundPool,Int,Int)->Unit){callback=f}
 fun load(context:Context,id:Int,priority:Int):Int{callback?.invoke(this,id,0);return id}
 fun play(id:Int,left:Float,right:Float,priority:Int,loop:Int,rate:Float):Int{check(!dead);check(rate in .5f..2f&&left in 0f..1f&&right in 0f..1f);if(live.size==max)live.remove(live.entries.minBy{it.value.priority}.key);val n=next++;live[n]=Stream(id,left,right,loop,rate,priority);plays++;return n}
 fun stop(id:Int){check(!dead);live.remove(id)}
 fun setVolume(id:Int,left:Float,right:Float){check(!dead);check(left in 0f..1f&&right in 0f..1f);live[id]?.let{it.left=left;it.right=right}}
 fun setRate(id:Int,rate:Float){check(!dead);live[id]?.rate=rate}
 fun autoPause(){check(!dead)};fun autoResume(){check(!dead)}
 fun release(){check(!dead);dead=true;live.clear()}
 companion object{lateinit var last:SoundPool}
}
