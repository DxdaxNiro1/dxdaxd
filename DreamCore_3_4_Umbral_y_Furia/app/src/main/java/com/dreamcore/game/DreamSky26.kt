package com.dreamcore.game

import android.opengl.GLES20 as G

/** March only the cloud volume at half resolution; planets and sky remain full resolution. */
class DreamSky26 {
    var texture=0;private set
    private var fbo=0;private var width=0;private var height=0;private var unavailable=false
    fun render(w:Int,h:Int,previousTarget:Int,draw:()->Unit):Boolean{
        if(unavailable)return false
        try{
            val sw=(w/2).coerceAtLeast(1);val sh=(h/2).coerceAtLeast(1)
            if(width!=sw || height!=sh || fbo==0){
                if(fbo!=0)G.glDeleteFramebuffers(1,intArrayOf(fbo),0)
                if(texture!=0)G.glDeleteTextures(1,intArrayOf(texture),0)
                width=sw;height=sh
                val ids=IntArray(1);G.glGenTextures(1,ids,0);texture=ids[0]
                G.glActiveTexture(G.GL_TEXTURE3);G.glBindTexture(G.GL_TEXTURE_2D,texture)
                for(axis in intArrayOf(G.GL_TEXTURE_MIN_FILTER,G.GL_TEXTURE_MAG_FILTER))G.glTexParameteri(G.GL_TEXTURE_2D,axis,G.GL_LINEAR)
                for(axis in intArrayOf(G.GL_TEXTURE_WRAP_S,G.GL_TEXTURE_WRAP_T))G.glTexParameteri(G.GL_TEXTURE_2D,axis,G.GL_CLAMP_TO_EDGE)
                G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_RGBA,sw,sh,0,G.GL_RGBA,G.GL_UNSIGNED_BYTE,null)
                G.glGenFramebuffers(1,ids,0);fbo=ids[0]
                G.glBindFramebuffer(G.GL_FRAMEBUFFER,fbo);G.glFramebufferTexture2D(G.GL_FRAMEBUFFER,G.GL_COLOR_ATTACHMENT0,G.GL_TEXTURE_2D,texture,0)
                check(G.glCheckFramebufferStatus(G.GL_FRAMEBUFFER)==G.GL_FRAMEBUFFER_COMPLETE)
            }
            // The sky sampler must not refer to the texture currently being written.
            G.glActiveTexture(G.GL_TEXTURE3);G.glBindTexture(G.GL_TEXTURE_2D,0)
            G.glBindFramebuffer(G.GL_FRAMEBUFFER,fbo);G.glViewport(0,0,sw,sh)
            draw()
            return true
        }catch(e:Exception){android.util.Log.w("DreamCore","Nubes compatibles",e);unavailable=true;return false}
        finally{G.glBindFramebuffer(G.GL_FRAMEBUFFER,previousTarget);G.glViewport(0,0,w,h)}
    }
}
