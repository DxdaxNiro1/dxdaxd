package com.dreamcore.game

import android.opengl.GLES20 as G

/** At most two world program groups (scene, sky, compact) plus the compatible fallback. */
class DreamPrograms28(private val compile:(String,String)->Int) {
    data class Pairing(val scene:Int,val sky:Int,val surface:Int=0)
    var onEvict:((Int)->Unit)?=null
    private val pairs=LinkedHashMap<Int,Pairing>(4,.75f,true)
    fun world(code:Float,sceneVertex:String,sceneFragment:String,skyVertex:String,skyFragment:String):Pairing {
        val key=code.toInt();pairs[key]?.let{return it}
        val scene=compile(DreamShader28.specialize(sceneVertex,code),DreamShader28.specialize(DreamAtmosphere32.grade(sceneFragment),code))
        val sky=try{compile(DreamShader28.specialize(skyVertex,code),DreamShader28.specialize(DreamAtmosphere32.grade(skyFragment),code))}catch(e:Exception){G.glDeleteProgram(scene);throw e}
        val surface=try{compile(DreamShader28.specialize(DreamSurface32.vertex(sceneVertex),code),DreamShader28.specialize(DreamAtmosphere32.grade(DreamSurface32.fragment(sceneFragment)),code))}catch(e:Exception){0}
        val result=Pairing(scene,sky,surface);pairs[key]=result
        if(pairs.size>2){val oldest=pairs.entries.iterator();val entry=oldest.next();G.glDeleteProgram(entry.value.scene);G.glDeleteProgram(entry.value.sky);if(entry.value.surface!=0)G.glDeleteProgram(entry.value.surface);onEvict?.invoke(entry.value.scene);onEvict?.invoke(entry.value.sky);if(entry.value.surface!=0)onEvict?.invoke(entry.value.surface);oldest.remove()}
        return result
    }
}
