package com.dreamcore.game

import android.content.Context
import android.opengl.GLES20 as GL

/** Two immutable VBOs and one atlas; no per-frame mesh construction or extra scene samplers. */
class DreamLunarRenderer27(context:Context,compile:(String,String)->Int) {
    private val program=compile(DreamLunarShaders27.vertexShader,DreamLunarShaders27.fragmentShader)
    private val texture=DreamArtTextures.load(context,R.drawable.dream_lunar_faces_27,1024,2048)
    private val uniforms=listOf("uVP","uCamera","uDirection","uDistance","uRadius","uMood","uEyeAim","uRed","uVariant","uAtlas").associateWith{GL.glGetUniformLocation(program,it)}
    private val attributes=listOf("aPosition","aColor","aNormal","aKind").map{GL.glGetAttribLocation(program,it)}.toIntArray()
    private val meshes=Array(2){i->DreamGpuMesh28(DreamIndexed28.encode(DreamMoonMesh27.create(i==1).vertices,includeShadow=false))}
    fun dispose(){for(mesh in meshes)mesh.dispose();GL.glDeleteProgram(program);GL.glDeleteTextures(1,intArrayOf(texture),0)}
    private fun u(name:String)=uniforms.getValue(name)
    fun draw(vp:FloatArray,eye:FloatArray,world:String,life:DreamMoonLife){
        if(!DreamVigilia27.dark(world))return
        GL.glUseProgram(program);GL.glEnable(GL.GL_DEPTH_TEST);GL.glDepthMask(true);GL.glDisable(GL.GL_BLEND);GL.glDisable(GL.GL_CULL_FACE)
        GL.glUniformMatrix4fv(u("uVP"),1,false,vp,0);GL.glUniform3fv(u("uCamera"),1,eye,0)
        GL.glActiveTexture(GL.GL_TEXTURE0);GL.glBindTexture(GL.GL_TEXTURE_2D,texture);GL.glUniform1i(u("uAtlas"),0)
        for(a in attributes)if(a>=0)GL.glEnableVertexAttribArray(a)
        fun moon(variant:Int,x:Float,y:Float,z:Float,distance:Float,radius:Float,blink:Float,smile:Float,ex:Float,ey:Float){
            if(!DreamPerformance.sphereVisible(eye[0]+x*distance,eye[1]+y*distance,eye[2]+z*distance,radius*1.6f,vp))return
            val i=if(variant==6)1 else 0
            GL.glUniform3f(u("uDirection"),x,y,z);GL.glUniform1f(u("uDistance"),distance);GL.glUniform1f(u("uRadius"),radius)
            GL.glUniform2f(u("uMood"),blink,smile);GL.glUniform2f(u("uEyeAim"),ex,ey);GL.glUniform1f(u("uVariant"),variant.toFloat())
            GL.glUniform1f(u("uRed"),if(variant==0 || variant==4 || variant==5)life.redness else 0f)
            meshes[i].draw(attributes)
        }
        val variants=DreamVigilia27.moons(world)
        moon(variants.first,DreamMoonLife.MAIN_X,DreamMoonLife.MAIN_Y,DreamMoonLife.MAIN_Z,320f,86f,life.mainBlink,life.mainSmile,life.eyeX,life.eyeY)
        moon(variants.second,DreamMoonLife.SMALL_X,DreamMoonLife.SMALL_Y,DreamMoonLife.SMALL_Z,340f,34f,life.smallBlink,life.smallSmile,life.smallEyeX,life.smallEyeY)
        if(life.giantVisible)moon(7,life.giantX,life.giantY,life.giantZ,210f,210f*life.giantRadius*life.giantReveal,life.giantBlink,1f,life.eyeX,life.eyeY)
        for(a in attributes)if(a>=0)GL.glDisableVertexAttribArray(a)
        GL.glBindBuffer(GL.GL_ARRAY_BUFFER,0);GL.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER,0)
    }
}
