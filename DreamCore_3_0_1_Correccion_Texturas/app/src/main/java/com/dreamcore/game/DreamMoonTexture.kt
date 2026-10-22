package com.dreamcore.game

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20

object DreamMoonTexture {
    fun load(context:Context,resource:Int=R.drawable.dream_moon_face):Int {
        val original=checkNotNull(BitmapFactory.decodeResource(context.resources,resource,
            DreamTexture29.options())) { "Falta dream_moon_face.png" }
        val bitmap=if(original.width!=1024 || original.height!=1024)android.graphics.Bitmap.createScaledBitmap(original,1024,1024,true)else original
        if(bitmap!==original)original.recycle()
        try {
            val ids=IntArray(1);GLES20.glGenTextures(1,ids,0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,ids[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE)
            DreamTexture29.upload(bitmap)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
            return ids[0]
        } finally { bitmap.recycle() }
    }
}
