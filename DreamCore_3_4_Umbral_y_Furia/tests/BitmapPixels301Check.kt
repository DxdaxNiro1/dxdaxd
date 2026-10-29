package com.dreamcore.game

import kotlin.math.*

fun main(){
 var checks=0;fun verify(ok:Boolean,text:String){check(ok){text};checks++}
 val src=intArrayOf(0x00102030,0x40406080,0x8080a0c0.toInt(),0xffc0e0ff.toInt())
 val identity=IntArray(8){0x12345678};DreamPixels301.resizeInto(src,2,2,identity,2,2,2)
 verify(identity.sliceArray(2..5).contentEquals(src),"identity keeps all straight-alpha channels, including RGB under alpha zero")
 verify(identity[0]==0x12345678&&identity[1]==0x12345678&&identity[6]==0x12345678,"destination offset guard")
 val average=IntArray(1);DreamPixels301.resizeInto(src,2,2,average,0,1,1)
 for(shift in 0..24 step 8){val expected=src.sumOf{(it ushr shift)and 255}/4.0;verify(abs(((average[0] ushr shift)and 255)-expected)<=.5,"independent bilinear channel $shift")}
 for(w in listOf(1,2,3,7))for(h in listOf(1,2,5))for(dw in listOf(1,2,4,9))for(dh in listOf(1,3,8)){
  val constant=IntArray(w*h){0x0097c154};val target=IntArray(dw*dh);DreamPixels301.resizeInto(constant,w,h,target,0,dw,dh)
  verify(target.all{it==0x0097c154},"constant transparent numerical channels survive $w $h $dw $dh")
 }
 val random=java.util.Random(301)
 repeat(300){val w=random.nextInt(12)+1;val h=random.nextInt(12)+1;val dw=random.nextInt(15)+1;val dh=random.nextInt(15)+1;val data=IntArray(w*h){random.nextInt()};val result=IntArray(dw*dh)
  DreamPixels301.resizeInto(data,w,h,result,0,dw,dh)
  for(y in 0 until dh)for(x in 0 until dw){val fx=((x+.5)*w/dw-.5).coerceIn(0.0,(w-1).toDouble());val fy=((y+.5)*h/dh-.5).coerceIn(0.0,(h-1).toDouble());val xx=floor(fx).toInt();val yy=floor(fy).toInt();val wx=fx-xx;val wy=fy-yy
   for(s in 0..24 step 8){fun c(dx:Int,dy:Int)=(data[minOf(yy+dy,h-1)*w+minOf(xx+dx,w-1)]ushr s)and 255
    val expected=(c(0,0)*(1-wx)*(1-wy)+c(1,0)*wx*(1-wy)+c(0,1)*(1-wx)*wy+c(1,1)*wx*wy).roundToInt()
    verify(abs(((result[y*dw+x]ushr s)and 255)-expected)<=1,"independent double-precision reference")
   }
  }
 }
 verify(runCatching{DreamPixels301.resizeInto(src,0,2,IntArray(1),0,1,1)}.isFailure,"zero source rejected")
 verify(runCatching{DreamPixels301.resizeInto(src,2,2,IntArray(1),1,1,1)}.isFailure,"out-of-bounds destination rejected")
 println("PASS $checks channel-preservation checks")
}
