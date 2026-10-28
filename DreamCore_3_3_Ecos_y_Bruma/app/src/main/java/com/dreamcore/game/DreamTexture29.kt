package com.dreamcore.game

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20 as G
import java.nio.ByteBuffer

/** Upload a defined RGBA8 channel order; do not inherit vendor bitmap/GPU internal formats. */
object DreamTexture29 {
    fun options(data:Boolean=false)=BitmapFactory.Options().apply{
        inScaled=false;inPreferredConfig=Bitmap.Config.ARGB_8888;inPremultiplied=!data
        if(android.os.Build.VERSION.SDK_INT>=26)inPreferredColorSpace=android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB)
    }
    private var staging=ByteBuffer.allocateDirect(0)
    @Synchronized fun upload(bitmap:Bitmap,mipmap:Boolean=true){
        val count=bitmap.width*bitmap.height
        val pixels=IntArray(count);bitmap.getPixels(pixels,0,bitmap.width,0,0,bitmap.width,bitmap.height)
        if(staging.capacity()<count*4)staging=ByteBuffer.allocateDirect(count*4)
        val bytes=staging;bytes.clear();bytes.limit(count*4)
        for(c in pixels){bytes.put((c shr 16).toByte());bytes.put((c shr 8).toByte());bytes.put(c.toByte());bytes.put((c ushr 24).toByte())};bytes.position(0)
        G.glPixelStorei(G.GL_UNPACK_ALIGNMENT,4);DreamGl29.drain()
        G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_RGBA,bitmap.width,bitmap.height,0,G.GL_RGBA,G.GL_UNSIGNED_BYTE,bytes)
        check(G.glGetError()==G.GL_NO_ERROR){"No se pudo subir una textura RGBA8"}
        if(mipmap)mipmaps()
    }
    fun mipmaps(){
        G.glGenerateMipmap(G.GL_TEXTURE_2D)
        if(G.glGetError()!=G.GL_NO_ERROR){G.glTexParameteri(G.GL_TEXTURE_2D,G.GL_TEXTURE_MIN_FILTER,G.GL_LINEAR);DreamGl29.drain()}
    }
}
