package com.dreamcore.game

import android.opengl.GLES20 as G

/** At most two world-specialized program pairs plus the original compatible programs. */
class DreamPrograms28(private val compile:(String,String)->Int) {
    data class Pairing(val scene:Int,val sky:Int)
    private val pairs=LinkedHashMap<Int,Pairing>(4,.75f,true)
    fun world(code:Float,sceneVertex:String,sceneFragment:String,skyVertex:String,skyFragment:String):Pairing {
        val key=code.toInt();pairs[key]?.let{return it}
        val scene=compile(DreamShader28.specialize(sceneVertex,code),DreamShader28.specialize(sceneFragment,code))
        val sky=try{compile(DreamShader28.specialize(skyVertex,code),DreamShader28.specialize(skyFragment,code))}catch(e:Exception){G.glDeleteProgram(scene);throw e}
        val result=Pairing(scene,sky);pairs[key]=result
        if(pairs.size>2){val oldest=pairs.entries.iterator();val entry=oldest.next();G.glDeleteProgram(entry.value.scene);G.glDeleteProgram(entry.value.sky);oldest.remove()}
        return result
    }
}
