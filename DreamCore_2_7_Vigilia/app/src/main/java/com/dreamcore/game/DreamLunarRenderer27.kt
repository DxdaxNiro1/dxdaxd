package com.dreamcore.game

import android.content.Context
import android.opengl.GLES20 as GL
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Two immutable VBOs and one atlas; no per-frame mesh construction or extra scene samplers. */
class DreamLunarRenderer27(context:Context,compile:(String,String)->Int) {
    private val program=compile(DreamLunarShaders27.vertexShader,DreamLunarShaders27.fragmentShader)
    private val texture=DreamArtTextures.load(context,R.drawable.dream_lunar_faces_27,1024,2048)
    private val uniforms=listOf("uVP","uCamera","uDirection","uDistance","uRadius","uMood","uEyeAim","uRed","uVariant","uAtlas").associateWith{GL.glGetUniformLocation(program,it)}
    private val attributes=listOf("aPosition","aColor","aNormal","aKind").map{GL.glGetAttribLocation(program,it)}
    private val buffers=IntArray(2)
    private val counts=IntArray(2)
    init {
        GL.glGenBuffers(2,buffers,0)
        for(i in 0..1){val data=DreamMoonMesh27.create(i==1).vertices;counts[i]=data.size/10
            val memory=ByteBuffer.allocateDirect(data.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer();memory.put(data).position(0)
            GL.glBindBuffer(GL.GL_ARRAY_BUFFER,buffers[i]);GL.glBufferData(GL.GL_ARRAY_BUFFER,data.size*4,memory,GL.GL_STATIC_DRAW)
        };GL.glBindBuffer(GL.GL_ARRAY_BUFFER,0)
    }
    private fun u(name:String)=uniforms.getValue(name)
    fun draw(vp:FloatArray,eye:FloatArray,world:String,life:DreamMoonLife){
        if(!DreamVigilia27.dark(world))return
        GL.glUseProgram(program);GL.glEnable(GL.GL_DEPTH_TEST);GL.glDepthMask(true);GL.glDisable(GL.GL_BLEND);GL.glDisable(GL.GL_CULL_FACE)
        GL.glUniformMatrix4fv(u("uVP"),1,false,vp,0);GL.glUniform3fv(u("uCamera"),1,eye,0)
        GL.glActiveTexture(GL.GL_TEXTURE0);GL.glBindTexture(GL.GL_TEXTURE_2D,texture);GL.glUniform1i(u("uAtlas"),0)
        fun moon(variant:Int,x:Float,y:Float,z:Float,distance:Float,radius:Float,blink:Float,smile:Float,ex:Float,ey:Float){
            val i=if(variant==6)1 else 0
            GL.glBindBuffer(GL.GL_ARRAY_BUFFER,buffers[i]);val offsets=intArrayOf(0,12,24,36);val sizes=intArrayOf(3,3,3,1)
            for(a in 0..3)if(attributes[a]>=0){GL.glEnableVertexAttribArray(attributes[a]);GL.glVertexAttribPointer(attributes[a],sizes[a],GL.GL_FLOAT,false,40,offsets[a])}
            GL.glUniform3f(u("uDirection"),x,y,z);GL.glUniform1f(u("uDistance"),distance);GL.glUniform1f(u("uRadius"),radius)
            GL.glUniform2f(u("uMood"),blink,smile);GL.glUniform2f(u("uEyeAim"),ex,ey);GL.glUniform1f(u("uVariant"),variant.toFloat())
            GL.glUniform1f(u("uRed"),if(variant in listOf(0,4,5))life.redness else 0f)
            GL.glDrawArrays(GL.GL_TRIANGLES,0,counts[i])
        }
        val variants=DreamVigilia27.moons(world)
        moon(variants.first,DreamMoonLife.MAIN_X,DreamMoonLife.MAIN_Y,DreamMoonLife.MAIN_Z,320f,86f,life.mainBlink,life.mainSmile,life.eyeX,life.eyeY)
        moon(variants.second,DreamMoonLife.SMALL_X,DreamMoonLife.SMALL_Y,DreamMoonLife.SMALL_Z,340f,34f,life.smallBlink,life.smallSmile,life.smallEyeX,life.smallEyeY)
        if(life.giantVisible)moon(7,life.giantX,life.giantY,life.giantZ,210f,210f*life.giantRadius*life.giantReveal,life.giantBlink,1f,life.eyeX,life.eyeY)
        for(a in attributes)if(a>=0)GL.glDisableVertexAttribArray(a)
        GL.glBindBuffer(GL.GL_ARRAY_BUFFER,0)
    }
}
