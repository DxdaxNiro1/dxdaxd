package com.dreamcore.game

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20 as G
import kotlin.math.*

fun main(args:Array<String>){
 var checks=0;fun verify(ok:Boolean,text:String){check(ok){text};checks++}
 val ctx=Context(Resources(args.single()));val botany=BitmapFactory.decodeResource(ctx.resources,R.drawable.dream_botany_26,DreamTexture29.options(data=true))
 verify(runCatching{Bitmap.createScaledBitmap(botany,512,512,true)}.exceptionOrNull()?.message?.contains("Canvas: trying to use a non-premultiplied bitmap")==true,"old production resize violates the Android Canvas contract")
 botany.recycle();Bitmap.scaleCalls=0
 for(api in listOf(19,34))for(q in 0..2){
  android.os.Build.VERSION.SDK_INT=api;G.uploads.clear();val recycled=Bitmap.recycledCount;val ids=DreamSurfaceTextures.load(ctx,q);val size=DreamLook25.atlasSize(q)
  verify(G.uploads.size==2&&ids.first!=ids.second,"two independent atlas uploads $api/$q")
  verify(Bitmap.recycledCount-recycled==2,"both decoded bitmaps recycled $api/$q")
  verify(Bitmap.scaleCalls==0,"no Canvas scaling path $api/$q")
  val atlas=G.uploads[0];verify(atlas.width==size&&atlas.height==size*2,"quality dimensions retained $api/$q")
  for(resource in 1..2){val src=BitmapFactory.decodeResource(ctx.resources,resource,DreamTexture29.options(data=true));val random=java.util.Random(301)
   for(i in 0 until 256){val x=if(i==0)0 else random.nextInt(size);val y=if(i==0)0 else random.nextInt(size);val fx=((x+.5)*src.width/size-.5).coerceIn(0.0,(src.width-1).toDouble());val fy=((y+.5)*src.height/size-.5).coerceIn(0.0,(src.height-1).toDouble());val xx=fx.toInt();val yy=fy.toInt();val wx=fx-xx;val wy=fy-yy
    for(channel in 0..3){val shift=if(channel==3)24 else 16-channel*8
     fun c(dx:Int,dy:Int)=(src.pixels[minOf(yy+dy,src.height-1)*src.width+minOf(xx+dx,src.width-1)]ushr shift)and 255
     val expected=(c(0,0)*(1-wx)*(1-wy)+c(1,0)*wx*(1-wy)+c(0,1)*(1-wx)*wy+c(1,1)*wx*wy).roundToInt()
     val actual=atlas.bytes[(((resource-1)*size+y)*size+x)*4+channel].toInt()and 255
     verify(abs(expected-actual)<=1,"uploaded straight RGBA channel $resource/$api/$q")
    }
   };src.recycle()
  }
  val normal=G.uploads[1];val tile=size/4
  for(y in listOf(0,size-1,size,size*2-1))for(x in listOf(0,size-1)){
   val tx=x/tile;val ty=y/tile
   fun h(dx:Int,dy:Int):Float{val px=tx*tile+(x%tile+dx+tile)%tile;val py=ty*tile+(y%tile+dy+tile)%tile;val at=(py*size+px)*4;return ((atlas.bytes[at].toInt()and 255)*.2126f+(atlas.bytes[at+1].toInt()and 255)*.7152f+(atlas.bytes[at+2].toInt()and 255)*.0722f)/255f}
   val at=(y*size+x)*4;val gx=((h(1,0)-h(-1,0))*2f).coerceIn(-1f,1f);val gy=((h(0,1)-h(0,-1))*2f).coerceIn(-1f,1f)
   verify((normal.bytes[at].toInt()and 255)==((gx*.5f+.5f)*255).toInt(),"normal X remains numerical data")
   verify((normal.bytes[at+1].toInt()and 255)==((gy*.5f+.5f)*255).toInt(),"normal Y remains numerical data")
   verify((normal.bytes[at+3].toInt()and 255)==(h(0,0)*255).toInt(),"height in alpha is not premultiplied")
  }
 }
 G.uploads.clear();G.limit=1024;DreamSurfaceTextures.load(ctx,2);verify(G.uploads.first().width==512&&G.uploads.first().height==1024,"GPU texture limit preserved")
 println("PASS $checks actual-loader boundary checks; API contracts simulated, real atlas PNGs and production loader executed")
}
