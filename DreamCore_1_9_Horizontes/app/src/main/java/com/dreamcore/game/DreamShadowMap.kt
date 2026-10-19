package com.dreamcore.game

import android.opengl.GLES20
import android.opengl.Matrix
import kotlin.math.*

/** GLES2 colour-encoded 16-bit directional shadow map, with a real depth buffer. */
class DreamShadowMap(private val makeProgram:(String,String)->Int) {
    val matrix=FloatArray(16)
    var texture=0;private set
    var ready=false;private set
    private var framebuffer=0
    private var depth=0
    private var program=0
    private var quality=-1
    private var size=512
    private var last=-100f
    private val view=FloatArray(16);private val projection=FloatArray(16)
    fun configure(level:Int) {
        if(level==quality)return
        dispose();quality=level
        if(level==0)return
        size=if(level==2)1024 else 512
        program=try{makeProgram(DreamShaders.shadowVertexShader,DreamShaders.shadowFragmentShader)}catch(problem:Exception){android.util.Log.w("DreamCore","Sombras no disponibles",problem);quality=0;return}
        val id=IntArray(1);GLES20.glGenTextures(1,id,0);texture=id[0];GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,size,size,0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,null)
        for(p in intArrayOf(GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_TEXTURE_MAG_FILTER))GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,p,GLES20.GL_NEAREST)
        for(p in intArrayOf(GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_TEXTURE_WRAP_T))GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,p,GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glGenRenderbuffers(1,id,0);depth=id[0];GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER,depth)
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,GLES20.GL_DEPTH_COMPONENT16,size,size)
        GLES20.glGenFramebuffers(1,id,0);framebuffer=id[0];GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,framebuffer)
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,GLES20.GL_TEXTURE_2D,texture,0)
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,GLES20.GL_DEPTH_ATTACHMENT,GLES20.GL_RENDERBUFFER,depth)
        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)!=GLES20.GL_FRAMEBUFFER_COMPLETE){dispose();quality=0}
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);last=-100f
    }
    fun render(time:Float,x:Float,y:Float,z:Float,night:Boolean,width:Int,height:Int,draw:(Int)->Unit) {
        if(framebuffer==0 || time-last<(if(quality==2).08f else .16f))return
        last=time
        val cx=round(x*2f)/2f;val cz=round(z*2f)/2f;val cy=y.coerceIn(-170f,45f)
        val sx=if(night).10f else -.40f;val sy=if(night).65f else .82f;val sz=if(night)-.80f else .40f
        Matrix.setLookAtM(view,0,cx+sx*110f,cy+sy*110f,cz+sz*110f,cx,cy,cz,0f,1f,0f)
        val radius=if(quality==2)78f else 60f
        Matrix.orthoM(projection,0,-radius,radius,-radius,radius,1f,250f);Matrix.multiplyMM(matrix,0,projection,0,view,0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,framebuffer);GLES20.glViewport(0,0,size,size)
        GLES20.glClearColor(1f,1f,1f,1f);GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program);GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program,"uLightVP"),1,false,matrix,0)
        GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);GLES20.glPolygonOffset(1.2f,2f)
        draw(program)
        GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL);GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0)
        GLES20.glViewport(0,0,width,height);GLES20.glClearColor(0f,0f,0f,1f);ready=true
    }
    fun invalidate(){last=-100f;ready=false}
    fun dispose() {
        if(texture!=0)GLES20.glDeleteTextures(1,intArrayOf(texture),0)
        if(depth!=0)GLES20.glDeleteRenderbuffers(1,intArrayOf(depth),0)
        if(framebuffer!=0)GLES20.glDeleteFramebuffers(1,intArrayOf(framebuffer),0)
        if(program!=0)GLES20.glDeleteProgram(program)
        texture=0;depth=0;framebuffer=0;program=0;ready=false
    }
}
