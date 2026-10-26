package com.dreamcore.game

import android.graphics.*
import android.graphics.drawable.Drawable

/** A revisit adds a view-only anomaly. The original JPEG and its metadata are never overwritten. */
class DreamPhotoMemory33(private val photo:Bitmap,private val aged:Boolean):Drawable(){
    var compare=false;set(value){field=value;invalidateSelf()}
    private var opacity33=255
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(c:Canvas){
        paint.alpha=opacity33
        paint.colorFilter=if(aged&&!compare)ColorMatrixColorFilter(ColorMatrix(floatArrayOf(.92f,.08f,0f,0f,3f,.03f,.91f,.03f,0f,1f,.04f,.04f,.86f,0f,0f,0f,0f,0f,1f,0f)))else null
        c.drawBitmap(photo,null,bounds,paint);paint.colorFilter=null
        if(aged&&!compare){val w=bounds.width().toFloat();val h=bounds.height().toFloat();val x=bounds.left+w*.68f;val y=bounds.top+h*.52f;val size=h*.085f
            paint.color=Color.argb(opacity33*62/255,20,25,25);c.drawOval(RectF(x-size*.18f,y-size*.58f,x+size*.18f,y-size*.18f),paint)
            val path=Path();path.moveTo(x-size*.16f,y-size*.24f);path.cubicTo(x-size*.34f,y+size*.1f,x-size*.27f,y+size*.5f,x-size*.40f,y+size*.9f);path.lineTo(x+size*.4f,y+size*.9f);path.cubicTo(x+size*.22f,y+size*.3f,x+size*.28f,y-size*.16f,x+size*.16f,y-size*.24f);path.close();c.drawPath(path,paint)
            paint.color=Color.argb(opacity33*38/255,27,24,19);for(i in 0..4)c.drawOval(RectF(x-size*.18f+i*size*.09f,y+size*(1.1f+i*.25f),x+size*.08f+i*size*.09f,y+size*(1.28f+i*.25f)),paint)
        }
    }
    override fun getIntrinsicWidth()=photo.width
    override fun getIntrinsicHeight()=photo.height
    override fun setAlpha(alpha:Int){opacity33=alpha.coerceIn(0,255);invalidateSelf()}
    override fun setColorFilter(filter:ColorFilter?){paint.colorFilter=filter}
    @Deprecated("Deprecated in Android") override fun getOpacity()=PixelFormat.OPAQUE
}
