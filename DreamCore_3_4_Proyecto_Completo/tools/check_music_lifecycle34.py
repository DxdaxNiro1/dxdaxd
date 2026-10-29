from pathlib import Path
import re,subprocess,shutil,os
p=Path(__file__).resolve().parents[1];v=Path(os.environ['DREAMCORE_VALIDATION_OUT']);root=v/'audio_stubs';root.mkdir(parents=True,exist_ok=True)
files={
'Animation.kt':'''package android.animation
open class Animator
open class AnimatorListenerAdapter {
 open fun onAnimationCancel(animation:Animator){}
 open fun onAnimationEnd(animation:Animator){}
}
class ValueAnimator:Animator(){
 var duration=0L;var interpolator:Any?=null;var animatedValue:Any=0f
 private val updates=mutableListOf<(ValueAnimator)->Unit>();private val listeners=mutableListOf<AnimatorListenerAdapter>()
 fun addUpdateListener(f:(ValueAnimator)->Unit){updates+=f}
 fun addListener(f:AnimatorListenerAdapter){listeners+=f}
 fun start(){pending+=this;updates.forEach{it(this)}}
 fun cancel(){pending.remove(this);listeners.forEach{it.onAnimationCancel(this)};listeners.forEach{it.onAnimationEnd(this)}}
 companion object {
  private val pending=mutableListOf<ValueAnimator>()
  fun ofFloat(vararg values:Float)=ValueAnimator()
  fun finish(){var count=0;while(pending.isNotEmpty()){check(count++<1000);val a=pending.removeAt(0);a.animatedValue=1f;a.updates.forEach{it(a)};a.listeners.forEach{it.onAnimationEnd(a)}}}
 }
}
''',
'Context.kt':'''package android.content
class Resources {fun getIdentifier(name:String,kind:String,pkg:String):Int=if(name.startsWith("music_score_"))1000+name.removePrefix("music_score_").substringBefore('_').toInt() else 0}
class Context {val resources=Resources();val packageName="com.dreamcore.game"}
''',
'Media.kt':'''package android.media
import android.content.Context
class MediaPlayer(val resource:Int){
 var isLooping=false;var isPlaying=false;private var released=false;var gain=0f
 fun setVolume(l:Float,r:Float){check(!released);check(l in 0f..1f&&r in 0f..1f);gain=l}
 fun start(){check(!released);isPlaying=true}
 fun pause(){check(!released);isPlaying=false}
 fun release(){check(!released);released=true;isPlaying=false;live.remove(this)}
 companion object {
  val live=mutableListOf<MediaPlayer>();var created=0;var maxLive=0
  fun create(c:Context,r:Int):MediaPlayer {check(r>0);created++;return MediaPlayer(r).also{live+=it;maxLive=maxOf(maxLive,live.size);check(live.count{it.resource>=1000}<=2);check(live.count{it.resource<1000}<=2)}}
 }
}
''',
'Log.kt':'''package android.util
object Log {fun w(tag:String,message:String,problem:Exception)=0}
''',
'Interpolator.kt':'''package android.view.animation
class LinearInterpolator
''',
'AudioLifecycle24Check.kt':'''package com.dreamcore.game
import android.content.Context
import android.animation.ValueAnimator
import android.media.MediaPlayer
fun main(){
 var checks=0;fun verify(ok:Boolean,label:String){check(ok){label};checks++}
 fun music()=MediaPlayer.live.filter{it.resource>=1000}
 fun settle(){ValueAnimator.finish();verify(MediaPlayer.live.size<=4,"bounded decoders")}
 val a=DreamAudio(Context());verify(MediaPlayer.created==0,"no decoding until foreground")
 a.resume();settle();verify(music().single().resource==1001&&music().single().isPlaying,"title has its own score")
 a.titleScreen(false);a.changeWorld(DreamWorlds.POOLS);settle();verify(music().single().resource==1003,"scene uses exact score")
 for(t in DreamMusic24.tracks.drop(1)){
  a.changeWorld(t.world);a.setZone(if(t.number%2==0)"CASA" else "VIENTO")
  settle();verify(music().single().resource==1000+t.number,"all 48 maps select distinct track")
 }
 // Rapid user requests during a live crossfade retain the latest choice without a third decoder.
 for(t in DreamMusic24.tracks)a.audition(t)
 settle();verify(music().single().resource==1000+DreamMusic24.tracks.last().number,"rapid next resolves latest")
 a.auditionPlaying(false);verify(music().none{it.isPlaying},"preview pauses")
 a.pause();val before=MediaPlayer.created;a.resume();settle()
 verify(MediaPlayer.created==before&&music().none{it.isPlaying},"paused preview stays paused across background")
 a.auditionPlaying(true);settle();verify(music().single().isPlaying,"resume preview")
 a.endAudition();settle();verify(music().single().resource==1000+DreamMusic24.tracks.last().number,"return to actual scene")
 a.changeWorld(DreamWorlds.CRATER);settle();a.audition(DreamMusic24.tracks[13]);settle()
 a.endAudition();settle();verify(music().single().resource==1004,"closing room restores scene not last preview")
 a.pause();val suspendedCount=MediaPlayer.created
 for(t in DreamMusic24.tracks){a.changeWorld(t.world);a.audition(t)}
 verify(MediaPlayer.created==suspendedCount&&MediaPlayer.live.none{it.isPlaying},"no decode while app backgrounded")
 a.resume();settle();verify(music().single().resource==1000+DreamMusic24.tracks.last().number,"foreground plays latest request")
 a.endAudition();a.titleScreen(true);settle();verify(music().single().resource==1001,"return home restores title")
 a.titleScreen(false);a.changeWorld(DreamWorlds.STATION);settle();val normal=music().single().gain
 repeat(60){a.proximity34(1f)};verify(music().single().gain<normal*.65f,"proximity creates room for breath and creature cues")
 a.audition(DreamMusic24.tracks.last());settle();verify(kotlin.math.abs(music().single().gain-normal)<.001f,"listening room keeps the user's full music level")
 a.endAudition();settle();a.clearProximity34();verify(kotlin.math.abs(music().single().gain-normal)<.001f,"menu/clear restores music level")
 a.setZone("VIENTO");settle();a.weatherLevel34(0f);verify(MediaPlayer.live.filter{it.resource<1000}.all{it.gain==0f},"weather slider also silences inherited wind layer");a.weatherLevel34(1f)
 a.levels(0f,0f);verify(MediaPlayer.live.all{it.gain==0f},"music and ambience controls silence mixer")
 a.pause();verify(MediaPlayer.live.none{it.isPlaying},"background is silent")
 a.release();settle();verify(MediaPlayer.live.isEmpty(),"all decoders released")
 println("PASS $checks checks; max simultaneous decoders ${MediaPlayer.maxLive}; actual DreamAudio under simulated Android lifecycle")
}
'''
}
audio=p/'app/src/main/java/com/dreamcore/game/DreamAudio.kt'
raws=sorted(set(re.findall(r'R.raw.(\w+)',audio.read_text())))
files['R.kt']='package com.dreamcore.game\nobject R {object raw {\n'+'\n'.join(f' const val {name}={i+1}' for i,name in enumerate(raws))+'\n}}\n'
for name,source in files.items():(root/name).write_text(source)
classpath=str(v/'checks.jar')+os.pathsep+os.environ['KOTLIN_STDLIB']
r=subprocess.run([os.environ.get('KOTLINC','kotlinc'),'-classpath',classpath,'-d',str(root/'audio-checks.jar'),str(audio),*[str(root/n) for n in files]],capture_output=True,text=True)
if r.returncode:print(r.stdout+r.stderr);r.check_returncode()
r=subprocess.run(['java','-cp',str(root/'audio-checks.jar')+':'+classpath,'com.dreamcore.game.AudioLifecycle24CheckKt'],capture_output=True,text=True)
print(r.stdout+r.stderr);r.check_returncode()
(v/'AudioLifecycle24Check.log').write_text(r.stdout+r.stderr)
