package com.dreamcore.game

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Atlas PNG real: no usa Internet y no exige una GPU moderna.
 * 16 materiales en una imagen de 512x512 para ahorrar cambios de textura.
 */
object DreamTextures {
    fun load(context: Context): Int {
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        check(ids[0] != 0) { "No se pudo crear la textura OpenGL" }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        // Si el recurso no se puede leer, el juego seguirá visible sin texturas.
        val bitmap = try {
            val options = BitmapFactory.Options().apply { inScaled = false }
            BitmapFactory.decodeResource(context.resources, R.drawable.dreamcore_atlas, options)
        } catch (_: Exception) { null }
        if (bitmap != null) {
            try {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
                // GLES2 permite mipmaps completos para texturas de potencia de dos.
                // Reducir el centelleo del atlas al mirar el borde y el fondo lejano.
                val w=bitmap.width; val h=bitmap.height
                if (w > 0 && h > 0 && (w and (w-1)) == 0 && (h and (h-1)) == 0) {
                    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR)
                }
            } finally { bitmap.recycle() }
        } else {
            val white = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
            white.put(byteArrayOf(-1, -1, -1, -1)).position(0)
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                1, 1, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, white)
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return ids[0]
    }
}
