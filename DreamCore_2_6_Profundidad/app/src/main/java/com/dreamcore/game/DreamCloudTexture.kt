package com.dreamcore.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20

/** Densidad y gradiente de nubes precalculados; una textura reutilizada por cielo y reflejos. */
object DreamCloudTexture {
    fun load(context: Context): Int {
        val options = BitmapFactory.Options().apply {
            inScaled = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inPremultiplied = false
        }
        val bitmap = requireNotNull(BitmapFactory.decodeResource(
            context.resources, R.drawable.dream_cloud_volume_26, options
        )) { "Falta dream_cloud_volume_26.png" }
        try {
            require(bitmap.width > 0 && bitmap.height > 0 &&
                (bitmap.width and (bitmap.width - 1)) == 0 &&
                (bitmap.height and (bitmap.height - 1)) == 0
            ) { "La textura repetible de nubes debe tener dimensiones potencia de dos" }
            val ids = IntArray(1)
            GLES20.glGenTextures(1, ids, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT)
            // Alpha is volume density data, not opacity for the RGB density field.
            // Never premultiply these numerical channels during upload.
            val pixels=IntArray(bitmap.width*bitmap.height)
            bitmap.getPixels(pixels,0,bitmap.width,0,0,bitmap.width,bitmap.height)
            val raw=java.nio.ByteBuffer.allocateDirect(pixels.size*4)
            for(c in pixels){raw.put((c shr 16).toByte());raw.put((c shr 8).toByte());raw.put(c.toByte());raw.put((c ushr 24).toByte())}
            raw.position(0)
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,bitmap.width,bitmap.height,0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,raw)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            return ids[0]
        } finally {
            bitmap.recycle()
        }
    }
}
