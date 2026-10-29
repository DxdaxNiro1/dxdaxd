package com.dreamcore.game
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import kotlin.math.*

/** CPU RGBA filtering stays straight-alpha; all GL allocation remains on its owning thread. */
object DreamSurfaceTextures {
    val carpetMean=floatArrayOf(.16f,.27f,.29f)
    data class Data(val size:Int,val pixels:IntArray,val normals:IntArray,val mean:FloatArray)
    fun prepare(context:Context,size:Int):Data {
        val height=size*2
        val pixels=IntArray(size*height)
        fun read(resource:Int,offset:Int){
            val source=checkNotNull(BitmapFactory.decodeResource(context.resources,resource,DreamTexture29.options(data=true)))
            try {
                if(source.width==size&&source.height==size)source.getPixels(pixels,offset,size,0,0,size,size)
                else {
                    // Android scales Bitmaps through Canvas, which rejects straight-alpha data.
                    // Read the unassociated channels first, then filter them without a Bitmap draw.
                    val raw=IntArray(source.width*source.height)
                    source.getPixels(raw,0,source.width,0,0,source.width,source.height)
                    DreamPixels301.resizeInto(raw,source.width,source.height,pixels,offset,size,size)
                }
            } finally { source.recycle() }
        }
        read(R.drawable.dream_materials_25,0)
        read(R.drawable.dream_botany_26,size*size)
        val tile=size/4;val sums=IntArray(3)
        for(y in tile*2 until tile*3)for(x in tile until tile*2){val c=pixels[y*size+x];sums[0]+=(c shr 16)and 255;sums[1]+=(c shr 8)and 255;sums[2]+=c and 255}
        val mean=FloatArray(3){sums[it]/(tile.toFloat()*tile*255f)}
        val rough=floatArrayOf(.95f,.86f,.90f,.89f,.83f,.27f,.31f,.20f,.72f,.98f,.68f,.82f,.92f,.95f,.88f,.32f,.72f,.86f,.88f,.80f)
        val normals=IntArray(pixels.size)
        for(y in 0 until height)for(x in 0 until size){
            val tx=x/tile;val ty=y/tile
            fun h(dx:Int,dy:Int):Float{val xx=tx*tile+((x%tile+dx+tile)%tile);val yy=ty*tile+((y%tile+dy+tile)%tile);val c=pixels[yy*size+xx];return (((c shr 16)and 255)*.2126f+((c shr 8)and 255)*.7152f+(c and 255)*.0722f)/255f}
            val gx=((h(1,0)-h(-1,0))*2f).coerceIn(-1f,1f);val gy=((h(0,1)-h(0,-1))*2f).coerceIn(-1f,1f)
            normals[y*size+x]=((h(0,0)*255).toInt() shl 24)or(((gx*.5f+.5f)*255).toInt() shl 16)or(((gy*.5f+.5f)*255).toInt() shl 8)or((rough.getOrNull(ty*4+tx)?:.95f)*255).toInt()
        }
        return Data(size,pixels,normals,mean)
    }
    fun load(context:Context,quality:Int):Pair<Int,Int>{
        val maximum=IntArray(1);GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE,maximum,0)
        val size=min(DreamLook25.atlasSize(quality),maximum[0]/2).coerceAtLeast(256)
        return upload(prepare(context,size))
    }
    fun upload(data:Data):Pair<Int,Int>{
        for(i in 0..2)carpetMean[i]=data.mean[i]
        val bytes=java.nio.ByteBuffer.allocateDirect(data.pixels.size*4)
        fun texture(pixels:IntArray):Int{
            bytes.clear()
            for(c in pixels){bytes.put((c shr 16).toByte());bytes.put((c shr 8).toByte());bytes.put(c.toByte());bytes.put((c ushr 24).toByte())};bytes.position(0)
            val id=IntArray(1);GLES20.glGenTextures(1,id,0);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,id[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR)
            for(axis in intArrayOf(GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_TEXTURE_WRAP_T))GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,axis,GLES20.GL_CLAMP_TO_EDGE)
            DreamGl29.drain();GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,data.size,data.size*2,0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,bytes)
            check(GLES20.glGetError()==GLES20.GL_NO_ERROR){"No se pudo subir el atlas RGBA8"};DreamTexture29.mipmaps();return id[0]
        }
        val a=texture(data.pixels);val n=texture(data.normals);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);return a to n
    }
}
