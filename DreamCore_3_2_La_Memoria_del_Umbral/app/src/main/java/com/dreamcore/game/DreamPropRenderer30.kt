package com.dreamcore.game

import android.opengl.GLES20 as G
import android.opengl.Matrix
import kotlin.math.*

/** One GPU mesh per model, bounded marks, and local transforms instead of per-frame mesh uploads. */
class DreamPropRenderer30 {
    private val models=HashMap<String,DreamGpuMesh28>()
    private val matrix=FloatArray(16)
    private var marks:DreamGpuMesh28?=null;private var markRevision=-1;private var world=""
    private var use=-1;private var transform=-1;private var boundProgram=-1
    fun draw(program:Int,attrs:IntArray,id:String,play:DreamPlay30,x:Float,z:Float,chapter:DreamChapter33?=null){
        if(world!=id){dispose();world=id}
        if(boundProgram!=program){use=G.glGetUniformLocation(program,"uUseModel30");transform=G.glGetUniformLocation(program,"uModel30");boundProgram=program}
        if(markRevision!=play.marksRevision){marks?.dispose();val raw=DreamProps30.marks(play,id).vertices;marks=if(raw.isEmpty())null else DreamGpuMesh28(DreamIndexed28.encode(raw,includeShadow=false));markRevision=play.marksRevision}
        G.glEnable(G.GL_BLEND);G.glBlendFunc(G.GL_SRC_ALPHA,G.GL_ONE_MINUS_SRC_ALPHA)
        G.glUniform1f(use,0f);marks?.draw(attrs)
        G.glUniform1f(use,1f)
        fun instance(name:String,px:Float,py:Float,pz:Float,angle:Float=0f,tilt:Float=0f,scale:Float=1f){
            if(hypot(px-x,pz-z)>105f&&name!="sky-sign")return
            val mesh=models.getOrPut(name){DreamGpuMesh28(DreamIndexed28.encode((if(name.startsWith("c33-"))DreamChapterProps33.model(name)else DreamProps30.model(name)).vertices,includeShadow=false))}
            Matrix.setIdentityM(matrix,0);Matrix.translateM(matrix,0,px,py,pz);Matrix.rotateM(matrix,0,angle,0f,1f,0f);if(tilt!=0f)Matrix.rotateM(matrix,0,tilt,1f,0f,0f);Matrix.scaleM(matrix,0,scale,scale,scale)
            G.glUniformMatrix4fv(transform,1,false,matrix,0);mesh.draw(attrs)
        }
        DreamProps30.instances(id,play,x,z){name,px,py,pz,angle,tilt,scale->instance(name,px,py,pz,angle,tilt,scale)}
        if(chapter!=null)DreamChapterProps33.instances(id,chapter,play){name,px,py,pz,angle,tilt,scale->instance(name,px,py,pz,angle,tilt,scale)}
        G.glUniform1f(use,0f);G.glDisable(G.GL_BLEND)
    }
    fun dispose(){for(m in models.values)m.dispose();models.clear();marks?.dispose();marks=null;markRevision=-1;world="";boundProgram=-1}
}
