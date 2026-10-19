package com.dreamcore.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils

object DreamArtTextures {
    fun load(context:Context,resource:Int,width:Int,height:Int):Int {
        val original=checkNotNull(BitmapFactory.decodeResource(context.resources,resource,BitmapFactory.Options().apply{inScaled=false}))
        val image=Bitmap.createScaledBitmap(original,width,height,true)
        if(image!==original)original.recycle()
        val ids=IntArray(1);GLES20.glGenTextures(1,ids,0);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,ids[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR)
        for(p in intArrayOf(GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_TEXTURE_WRAP_T))GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,p,GLES20.GL_CLAMP_TO_EDGE)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,image,0);GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);image.recycle()
        return ids[0]
    }
}
