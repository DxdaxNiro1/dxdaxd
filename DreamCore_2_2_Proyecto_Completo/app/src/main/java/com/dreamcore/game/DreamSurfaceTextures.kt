package com.dreamcore.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import kotlin.math.*

/** Photograph-like albedo plus a derived slope/roughness map, generated once on GL creation. */
object DreamSurfaceTextures {
    fun load(context:Context,quality:Int):Pair<Int,Int> {
        val source=checkNotNull(BitmapFactory.decodeResource(context.resources,R.drawable.dream_materials_21,BitmapFactory.Options().apply{inScaled=false}))
        val size=if(quality==0)512 else 1024
        val image=if(source.width!=size || source.height!=size)Bitmap.createScaledBitmap(source,size,size,true)else source
        if(image!==source)source.recycle()
        val pixels=IntArray(size*size);image.getPixels(pixels,0,size,0,0,size,size)
        val heights=FloatArray(pixels.size){val c=pixels[it];(((c shr 16) and 255)*.2126f+((c shr 8) and 255)*.7152f+(c and 255)*.0722f)/255f}
        val material=IntArray(pixels.size);val tile=size/4
        val rough=floatArrayOf(.95f,.86f,.90f,.89f,.83f,.27f,.31f,.20f,.72f,.98f,.68f,.82f,.92f,.95f,.88f,.32f)
        for(y in 0 until size)for(x in 0 until size) {
            val tx=x/tile;val ty=y/tile
            fun h(dx:Int,dy:Int):Float {val xx=tx*tile+((x%tile+dx+tile)%tile);val yy=ty*tile+((y%tile+dy+tile)%tile);return heights[yy*size+xx]}
            val gx=((h(1,0)-h(-1,0))*2f).coerceIn(-1f,1f)
            val gy=((h(0,1)-h(0,-1))*2f).coerceIn(-1f,1f)
            val r=((gx*.5f+.5f)*255).toInt();val g=((gy*.5f+.5f)*255).toInt()
            val b=(rough[ty*4+tx]*255).toInt();val a=(h(0,0)*255).toInt()
            material[y*size+x]=(a shl 24)or(r shl 16)or(g shl 8)or b
        }
        val detail=java.nio.ByteBuffer.allocateDirect(size*size*4)
        for(c in material){detail.put((c shr 16).toByte());detail.put((c shr 8).toByte());detail.put(c.toByte());detail.put((c ushr 24).toByte())}
        detail.position(0)
        fun upload(b:Bitmap):Int {
            val ids=IntArray(1);GLES20.glGenTextures(1,ids,0);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,ids[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,b,0);GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
            return ids[0]
        }
        val albedo=upload(image)
        val data=IntArray(1);GLES20.glGenTextures(1,data,0);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,data[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR)
        for(axis in intArrayOf(GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_TEXTURE_WRAP_T))GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,axis,GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,size,size,0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,detail)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        val ids=albedo to data[0];image.recycle();GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
        return ids
    }
}
