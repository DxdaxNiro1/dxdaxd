package com.dreamcore.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLUtils
import java.io.File

/** Four durable copies: album rotation never deletes a photograph placed at home. GL thread only. */
class DreamPhotoFrames(context:Context) {
    private val root=File(context.filesDir,"refuge_frames").apply{mkdirs()}
    var texture=0;private set
    var dirty=true
    fun pin(source:File,slot:Int):Boolean {
        if(slot !in 0..3 || !source.isFile)return false
        return try{val next=File(root,"next.jpg");source.inputStream().use{input->next.outputStream().use{input.copyTo(it)}};check(next.renameTo(File(root,"slot_$slot.jpg")));dirty=true;true}catch(_:Exception){false}
    }
    fun clear(){root.listFiles()?.forEach{it.delete()};dirty=true}
    fun load() {
        if(!dirty && texture!=0)return
        val bitmap=Bitmap.createBitmap(1024,1024,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(bitmap);canvas.drawColor(Color.rgb(46,49,63));val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        for(i in 0..3){val image=BitmapFactory.decodeFile(File(root,"slot_$i.jpg").path,BitmapFactory.Options().apply{inSampleSize=2})?:continue
            val scale=minOf(500f/image.width,500f/image.height);val w=image.width*scale;val h=image.height*scale
            val x=(i%2)*512f+(512f-w)/2;val y=(i/2)*512f+(512f-h)/2
            canvas.drawBitmap(image,null,RectF(x,y,x+w,y+h),paint);image.recycle()}
        if(texture==0){val id=IntArray(1);GLES20.glGenTextures(1,id,0);texture=id[0]}
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture);GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);bitmap.recycle();dirty=false
    }
    fun contextLost(){texture=0;dirty=true}
}
