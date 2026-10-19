package com.dreamcore.game

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.Executors

/** Capture the rendered world plus its photographic clues, then encode off the GL thread. */
class DreamPhotoStore(context:Context) {
    val folder=File(context.filesDir,"dream_photos").apply{mkdirs()}
    private val encoder=Executors.newSingleThreadExecutor()
    fun capture(width:Int,height:Int,done:(Boolean)->Unit) {
        // Keep only one asynchronous image at a time in the caller, bound stored photos to twelve.
        val pixels=ByteBuffer.allocateDirect(width*height*4)
        GLES20.glReadPixels(0,0,width,height,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,pixels)
        if(GLES20.glGetError()!=GLES20.GL_NO_ERROR){done(false);return}
        encoder.submit {
            var bitmap:Bitmap?=null
            try {
                val scale=minOf(1f,1280f/width,720f/height)
                val targetWidth=(width*scale).toInt().coerceAtLeast(1);val targetHeight=(height*scale).toInt().coerceAtLeast(1)
                val data=IntArray(targetWidth*targetHeight)
                for(y in 0 until targetHeight)for(x in 0 until targetWidth){val i=((y*height/targetHeight)*width+x*width/targetWidth)*4
                    val r=pixels.get(i).toInt()and 255;val g=pixels.get(i+1).toInt()and 255;val b=pixels.get(i+2).toInt()and 255
                    data[(targetHeight-1-y)*targetWidth+x]=(255 shl 24)or(r shl 16)or(g shl 8)or b}
                bitmap=Bitmap.createBitmap(data,targetWidth,targetHeight,Bitmap.Config.ARGB_8888)
                val dest=File(folder,"dream_${System.currentTimeMillis()}.jpg")
                dest.outputStream().use{check(bitmap.compress(Bitmap.CompressFormat.JPEG,88,it))}
                folder.listFiles()?.sortedByDescending{it.lastModified()}?.drop(12)?.forEach{it.delete()}
                done(true)
            } catch(_:Exception){done(false)}finally{bitmap?.recycle()}
        }
    }
    fun close(){encoder.shutdown()}
}
