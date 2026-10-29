package com.dreamcore.game

import android.content.Context
import android.opengl.GLES20 as G
import android.opengl.Matrix
import kotlin.math.*

/** Single material atlas, cached indexed meshes, frustum rejection and two authored geometric LODs. */
class DreamEntityRenderer32(context:Context,compile:(String,String)->Int){
    private val program=compile(DreamEntityShaders32.vertex,DreamEntityShaders32.fragment)
    private val texture=DreamArtTextures.load(context,R.drawable.dream_entities_32,1024,1024)
    private val attrs=arrayOf("aPos","aColor","aNormal","aKind").map{G.glGetAttribLocation(program,it)}.toIntArray()
    private val pending=HashMap<String,java.util.concurrent.Future<DreamIndexed28.Mesh>>()
    private val motion33=DreamMotion33()
    private val models=HashMap<String,DreamGpuMesh28>();private val m=FloatArray(16);private var world=""
    private val locations=HashMap<String,Int>()
    private fun loc(n:String)=locations.getOrPut(n){G.glGetUniformLocation(program,n)}
    fun ready35(model:String)=models.containsKey(model+0)||models.containsKey(model+1)
    fun draw(vp:FloatArray,eye:FloatArray,id:String,time:Float,approach:Float=0f,clip:Float?=null,extra:List<DreamEntities32.Presence> = emptyList(),mirror:Boolean=false,gesture:Float=0f,attack:DreamFatal35.Frame=DreamFatal35.Frame()){
        if(world!=id){for(mesh in models.values)mesh.dispose();models.clear();for(f in pending.values)f.cancel(true);pending.clear();world=id}
        motion33.clear(id);val list=DreamEntities32.placements(id)+extra;var uploaded=false;if(list.isEmpty())return
        G.glUseProgram(program);G.glDisable(G.GL_BLEND);G.glEnable(G.GL_DEPTH_TEST);G.glDepthMask(true)
        G.glUniformMatrix4fv(loc("uVP"),1,false,vp,0);G.glUniform3f(loc("uEye"),eye[0],eye[1],eye[2]);G.glUniform1i(loc("uAtlas"),0)
        G.glActiveTexture(G.GL_TEXTURE0);G.glBindTexture(G.GL_TEXTURE_2D,texture)
        G.glUniform1f(loc("uClipY"),clip?:-10000f)
        G.glUniform1f(loc("uNight"),if(DreamLook25.light(id).night>.5f||id==DreamHorror32.WITNESSES||id==DreamHorror32.FOREST)1f else 0f)
        for(a in attrs)if(a>=0)G.glEnableVertexAttribArray(a)
        for((index,p)in list.withIndex()){
            val caught=attack.held&&attack.target?.let{it.model==p.model&&abs(it.x-p.x)<.1f&&(abs(it.z-p.z)<.1f||p.model=="mask")}==true
            val z=if(caught)attack.target!!.z else p.z+if(p.model=="mask"&&id==DreamHorror32.WITNESSES)approach else 0f
            val y=DreamJourney.surface(id,p.x,z)+p.y
            val d=hypot(eye[0]-p.x,eye[2]-z)
            if(d>420f||!DreamPerformance.sphereVisible(p.x,y+(if(p.model=="observatory")6.5f else 3f)*p.scale,z,(if(p.model=="observatory")11f else 7f)*p.scale,vp))continue
            val lod=if(d/p.scale>40f)1 else 0;val key=p.model+lod
            var mesh=models[key]
            if(mesh==null){
                val future=pending.getOrPut(key){workers.submit<DreamIndexed28.Mesh>{DreamIndexed28.encode(DreamEntities32.model(p.model,lod).vertices,includeShadow=false)}}
                if(!future.isDone||future.isCancelled||uploaded)continue
                val scene=try{future.get()}catch(e:Exception){pending.remove(key);continue}
                mesh=DreamGpuMesh28(scene);models[key]=mesh;pending.remove(key);uploaded=true
            }
            val rawGaze=atan2(eye[0]-p.x,eye[2]-z)+p.yaw
            val delayed=motion33.pose(p.model+index,time,p.x,y,z,rawGaze,mirror);val gaze=if(caught)rawGaze else delayed.yaw
            val hover=if(caught)0f else if(p.model in listOf("choir","mask","crimson","gold"))sin(time*.35f+p.phase)*.12f else 0f
            Matrix.setIdentityM(m,0);Matrix.translateM(m,0,delayed.x,delayed.y+hover,delayed.z);Matrix.rotateM(m,0,gaze*180f/PI.toFloat(),0f,1f,0f);Matrix.scaleM(m,0,p.scale,p.scale,p.scale)
            G.glUniformMatrix4fv(loc("uModel"),1,false,m,0);G.glUniform1f(loc("uTime"),(delayed.time+p.phase)%512f)
            val grip=if(caught)DreamFatal35.grip(p.model,attack.time)else 0f
            val rage=if(caught)((attack.progress-.40f)/.12f).coerceIn(0f,1f)else 0f
            G.glUniform4f(loc("uAttack35"),grip,rage,if(caught)attack.style.toFloat()else 0f,attack.time)
            val dx=(eye[0]-p.x)/p.scale;val dz=(eye[2]-z)/p.scale
            G.glUniform3f(loc("uGrip35"),cos(gaze)*dx-sin(gaze)*dz,(eye[1]-y)/p.scale,sin(gaze)*dx+cos(gaze)*dz)
            val limb=when(p.model){"observatory"->8f to 2.8f;"root-eye"->12f to 2f;"water-eye","mushroom"->2f to .7f;"cone"->3.5f to 1.5f;"gentleman"->3f to 2f;"tenant"->3.6f to .7f;"star"->3.2f to 1.2f;"window"->3.5f to 1.7f;else->3.65f to 2.1f};G.glUniform2f(loc("uLimb35"),limb.first,limb.second)
            G.glUniform1f(loc("uFace35"),if(p.model=="tenant")4.34f else 4.8f)
            val articulation=DreamMotion33.gestures(p.model,delayed.time,p.phase);G.glUniform4f(loc("uArt33"),articulation[0],articulation[1],articulation[2],articulation[3]);G.glUniform1f(loc("uGesture33"),if(p.model=="archivist")gesture else 0f)
            G.glUniform1f(loc("uOpen"),if(p.model=="witness")maxOf(rage,((sin(time*.12f+p.phase)-.2f)/.8f).coerceIn(0f,1f))else 0f)
            mesh.draw(attrs)
        }
        for(a in attrs)if(a>=0)G.glDisableVertexAttribArray(a)
        G.glBindBuffer(G.GL_ARRAY_BUFFER,0);G.glBindBuffer(G.GL_ELEMENT_ARRAY_BUFFER,0)
    }
    companion object {
        private val workers=java.util.concurrent.Executors.newSingleThreadExecutor{r->Thread(r,"dream-entities").apply{isDaemon=true;priority=Thread.NORM_PRIORITY-1}}
    }
}
