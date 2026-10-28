package com.dreamcore.game

import android.opengl.GLES20 as G
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** All queries/probes run once with the actual phone's current GL context. */
object DreamGl29 {
    fun drain(){repeat(16){if(G.glGetError()==G.GL_NO_ERROR)return}}
    private fun integer(name:Int):Int{val a=IntArray(1);G.glGetIntegerv(name,a,0);return a[0]}
    fun capabilities():DreamCompatibility29.Caps{
        val p=IntArray(1);G.glGetShaderPrecisionFormat(G.GL_FRAGMENT_SHADER,G.GL_HIGH_FLOAT,IntArray(2),0,p,0)
        val viewport=IntArray(2);G.glGetIntegerv(G.GL_MAX_VIEWPORT_DIMS,viewport,0)
        val extensions=G.glGetString(G.GL_EXTENSIONS).orEmpty().split(' ').toSet()
        return DreamCompatibility29.Caps(G.glGetString(G.GL_RENDERER).orEmpty(),G.glGetString(G.GL_VERSION).orEmpty(),p[0],integer(G.GL_MAX_TEXTURE_IMAGE_UNITS),integer(G.GL_MAX_VARYING_VECTORS),integer(G.GL_MAX_TEXTURE_SIZE),integer(G.GL_MAX_RENDERBUFFER_SIZE),viewport[0],viewport[1],"GL_OES_depth_texture" in extensions,"GL_OES_depth24" in extensions)
    }
    /** Detect channel corruption, incomplete targets and unusable depth sampling, not just advertised extensions. */
    fun probe(makeProgram:(String,String)->Int,testDepth:Boolean):Probe {
        val textures=IntArray(2);val fb=IntArray(1);
        var shader=0;var depthGood=false;var colorGood=false
        drain()
        try{
            G.glGenTextures(2,textures,0);G.glGenFramebuffers(1,fb,0);G.glBindFramebuffer(G.GL_FRAMEBUFFER,fb[0])
            fun bind(id:Int){G.glActiveTexture(G.GL_TEXTURE0);G.glBindTexture(G.GL_TEXTURE_2D,id);for(k in intArrayOf(G.GL_TEXTURE_MIN_FILTER,G.GL_TEXTURE_MAG_FILTER))G.glTexParameteri(G.GL_TEXTURE_2D,k,G.GL_NEAREST);for(k in intArrayOf(G.GL_TEXTURE_WRAP_S,G.GL_TEXTURE_WRAP_T))G.glTexParameteri(G.GL_TEXTURE_2D,k,G.GL_CLAMP_TO_EDGE)}
            bind(textures[0]);G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_RGBA,4,4,0,G.GL_RGBA,G.GL_UNSIGNED_BYTE,null)
            G.glFramebufferTexture2D(G.GL_FRAMEBUFFER,G.GL_COLOR_ATTACHMENT0,G.GL_TEXTURE_2D,textures[0],0)
            if(G.glCheckFramebufferStatus(G.GL_FRAMEBUFFER)!=G.GL_FRAMEBUFFER_COMPLETE)return Probe(false,false)
            G.glViewport(0,0,4,4);G.glDisable(G.GL_DITHER);G.glDisable(G.GL_BLEND);G.glClearColor(.2f,.6f,.8f,1f);G.glClear(G.GL_COLOR_BUFFER_BIT)
            val bytes=ByteBuffer.allocateDirect(64);G.glReadPixels(0,0,4,4,G.GL_RGBA,G.GL_UNSIGNED_BYTE,bytes)
            colorGood=G.glGetError()==G.GL_NO_ERROR && (0 until 16).all{i->kotlin.math.abs((bytes.get(i*4).toInt()and 255)-51)<=2 && kotlin.math.abs((bytes.get(i*4+1).toInt()and 255)-153)<=2 && kotlin.math.abs((bytes.get(i*4+2).toInt()and 255)-204)<=2}
            if(colorGood && testDepth){
                // Same UINT depth allocation as the post chain; reject drivers that merely accept the enum.
                bind(textures[1]);G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_DEPTH_COMPONENT,4,4,0,G.GL_DEPTH_COMPONENT,G.GL_UNSIGNED_INT,null)
                G.glFramebufferTexture2D(G.GL_FRAMEBUFFER,G.GL_DEPTH_ATTACHMENT,G.GL_TEXTURE_2D,textures[1],0)
                if(G.glCheckFramebufferStatus(G.GL_FRAMEBUFFER)==G.GL_FRAMEBUFFER_COMPLETE && G.glGetError()==G.GL_NO_ERROR){
                    G.glDepthMask(true);G.glClearDepthf(.375f);G.glClear(G.GL_DEPTH_BUFFER_BIT)
                    // Detach before sampling: no framebuffer/texture feedback, even for the calibration.
                    G.glFramebufferTexture2D(G.GL_FRAMEBUFFER,G.GL_DEPTH_ATTACHMENT,G.GL_TEXTURE_2D,0,0)
                    shader=makeProgram("attribute vec2 aPos;void main(){gl_Position=vec4(aPos,0.0,1.0);}","precision highp float;uniform highp sampler2D uDepth;void main(){float d=texture2D(uDepth,vec2(.5)).r;gl_FragColor=vec4(d,d,d,1.0);}")
                    G.glUseProgram(shader);G.glUniform1i(G.glGetUniformLocation(shader,"uDepth"),0)
                    val vertices=ByteBuffer.allocateDirect(24).order(ByteOrder.nativeOrder()).asFloatBuffer().apply{put(floatArrayOf(-1f,-1f,3f,-1f,-1f,3f));position(0)}
                    val a=G.glGetAttribLocation(shader,"aPos");G.glBindBuffer(G.GL_ARRAY_BUFFER,0);G.glEnableVertexAttribArray(a);G.glVertexAttribPointer(a,2,G.GL_FLOAT,false,0,vertices)
                    G.glDisable(G.GL_DEPTH_TEST);G.glDrawArrays(G.GL_TRIANGLES,0,3);G.glDisableVertexAttribArray(a)
                    bytes.clear();G.glReadPixels(0,0,4,4,G.GL_RGBA,G.GL_UNSIGNED_BYTE,bytes)
                    depthGood=G.glGetError()==G.GL_NO_ERROR && (0 until 16).all{i->(0..2).all{c->kotlin.math.abs((bytes.get(i*4+c).toInt()and 255)-96)<=2}}
                }
            }
        }catch(e:Exception){android.util.Log.w("DreamCore","Comprobación gráfica compatible",e)}finally{
            if(shader!=0)G.glDeleteProgram(shader)
            G.glDeleteFramebuffers(1,fb,0);G.glDeleteTextures(2,textures,0)
            G.glBindFramebuffer(G.GL_FRAMEBUFFER,0);G.glActiveTexture(G.GL_TEXTURE0);G.glBindTexture(G.GL_TEXTURE_2D,0)
            G.glClearDepthf(1f);G.glClearColor(0f,0f,0f,1f);G.glDepthMask(true);G.glEnable(G.GL_DEPTH_TEST);G.glEnable(G.GL_DITHER);G.glUseProgram(0);drain()
        }
        return Probe(colorGood,depthGood)
    }
    data class Probe(val color:Boolean,val depth:Boolean)
}
