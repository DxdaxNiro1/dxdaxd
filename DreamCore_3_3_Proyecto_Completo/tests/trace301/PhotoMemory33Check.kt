package com.dreamcore.game
import android.graphics.*
fun main(){var checks=0;fun verify(v:Boolean,m:String){check(v){m};checks++}
 val original=intArrayOf(0xff517792.toInt(),0xff281916.toInt(),0xff467532.toInt(),0xff6633ff.toInt());val bitmap=Bitmap(2,2,original.copyOf(),false,true);val view=DreamPhotoMemory33(bitmap,true);val c=Canvas()
 view.draw(c);view.draw(c);verify(c.bitmapAlpha==listOf(255,255),"redrawing an aged photo never inherits anomaly transparency")
 verify(c.anomalies>0&&c.filters.all{it!=null},"revisit displays the view-only anomaly")
 view.compare=true;val before=c.anomalies;view.draw(c);verify(c.bitmapAlpha.last()==255&&c.filters.last()==null&&c.anomalies==before,"comparison restores fully visible unfiltered original")
 view.setAlpha(128);view.draw(c);verify(c.bitmapAlpha.last()==128,"Drawable opacity is respected")
 verify(bitmap.pixels.contentEquals(original)&&!bitmap.recycled,"original photo pixels remain unmodified and owned by album")
 println("PASS $checks photograph redraw/original-preservation boundary checks")}
