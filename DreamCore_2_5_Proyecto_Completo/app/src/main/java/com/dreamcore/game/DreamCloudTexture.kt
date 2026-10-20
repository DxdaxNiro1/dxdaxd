package com.dreamcore.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils

/** Densidad y gradiente de nubes precalculados; una textura reutilizada por cielo y reflejos. */
object DreamCloudTexture {
    fun load(context: Context): Int {
        val options = BitmapFactory.Options().apply {
            inScaled = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = requireNotNull(BitmapFactory.decodeResource(
            context.resources, R.drawable.dream_cloud_density, options
        )) { "Falta dream_cloud_density.png" }
        try {
            require(bitmap.width > 0 && bitmap.height > 0 &&
                (bitmap.width and (bitmap.width - 1)) == 0 &&
                (bitmap.height and (bitmap.height - 1)) == 0
            ) { "La textura repetible de nubes debe tener dimensiones potencia de dos" }
            val ids = IntArray(1)
            GLES20.glGenTextures(1, ids, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR_MIPMAP_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            return ids[0]
        } finally {
            bitmap.recycle()
        }
    }
}
