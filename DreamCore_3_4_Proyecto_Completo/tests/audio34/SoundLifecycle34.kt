package com.dreamcore.game
import android.content.Context
import android.media.SoundPool
fun main(){var checks=0;fun verify(v:Boolean,m:String){check(v){m};checks++}
 val fx=DreamFootsteps(Context());val pool=SoundPool.last;val options=DreamOptions();val frame=DreamAcoustics34.Frame("city",1f,1f,1f)
 fx.update34(frame,options);verify(pool.live.isEmpty(),"title/paused screen never starts breathing or weather")
 fx.resume();fx.update34(frame,options);verify(pool.live.size==3&&pool.live.values.all{it.loop==-1},"only three environment/body loops can run")
 val plays=pool.plays;repeat(100){fx.update34(frame,options)};verify(pool.plays==plays,"UI frames never restart identical loops")
 verify(pool.live.values.all{it.left<=.28f&&it.rate<=1.4f},"bounded volume and breath rate")
 options.breathGain34=0f;fx.update34(frame,options);verify(pool.live.size==2,"breathing can be muted independently")
 fx.userGain=0f;fx.play("presence_bone34",.5f,false,.2f);verify(pool.live.size==3,"muting footsteps does not mute entity voices")
 for(style in DreamFatal35.styles.values){fx.play("attack_"+style.sound+"35",0f,false,.34f);verify(pool.live.size==3,"one scream replaces the previous presence voice ${style.sound}")}
 fx.voiceGain34=0f;val muted=pool.plays;fx.play("attack_tenant35",0f,false,.34f);verify(pool.plays==muted,"voice control also mutes fatal cries");fx.voiceGain34=.8f
 fx.silence34();verify(pool.live.isEmpty(),"opening a menu stops all three loops and the active creature cue")
 options.breathGain34=1f;fx.update34(frame,options);verify(pool.live.size==3,"returning from menu restores current layers")
 fx.pause();verify(pool.live.isEmpty(),"background pause cannot leave breathing/weather playing")
 fx.resume();fx.ambienceGain=0f;fx.update34(frame,options);verify(pool.live.isEmpty(),"ambience master mute silences all three layers")
 fx.release();fx.release();fx.pause();fx.update34(frame,options);verify(pool.dead&&pool.live.isEmpty(),"late updates and repeated release are harmless")
 println("PASS $checks actual SoundPool-owner lifecycle checks with recorded Android boundary")}
