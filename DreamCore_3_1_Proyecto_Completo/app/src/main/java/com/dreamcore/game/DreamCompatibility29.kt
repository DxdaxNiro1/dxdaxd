package com.dreamcore.game

import kotlin.math.*

/** Capability decisions are separate from saved artistic quality and world content. */
object DreamCompatibility29 {
    data class Caps(val renderer:String,val version:String,val fragmentPrecision:Int,val textureUnits:Int,val varyingVectors:Int,val maxTexture:Int,val maxTarget:Int,val viewportWidth:Int,val viewportHeight:Int,val depthTexture:Boolean,val depth24:Boolean)
    data class Policy(val basic:Boolean,val depthSampling:Boolean,val stable:Boolean,val label:String)
    fun policy(c:Caps,mode:Int):Policy {
        val adreno=Regex("adreno[^0-9]*(\\d+)",RegexOption.IGNORE_CASE).find(c.renderer)?.groupValues?.get(1)?.toIntOrNull()
        val conservative=adreno!=null && (adreno<600 || adreno in setOf(610,612,616,618,619))
        val basic=mode==2 || c.fragmentPrecision<16 || c.varyingVectors<9
        val stable=mode!=0 || conservative || basic
        return Policy(basic,!stable && c.depthTexture && c.fragmentPrecision>=16,stable,if(basic)"Esencial" else if(stable)"Estable" else "Completo")
    }
    fun fit(w:Int,h:Int,scale:Float,limit:Int):Pair<Int,Int>{
        val safe=limit.coerceAtLeast(1);val s=minOf(scale.coerceIn(.1f,1f),safe.toFloat()/w.coerceAtLeast(1),safe.toFloat()/h.coerceAtLeast(1))
        return max(1,(w*s).toInt()) to max(1,(h*s).toInt())
    }
    /** No GC, timer queries or glFinish. Respond only to sustained missed frame budgets. */
    class Budget {
        var scale=1f;private set
        private var pressure=0f;private var calm=0f
        fun reset(){pressure=0f;calm=0f}
        fun sample(drawNanos:Long,gapNanos:Long,fps:Int,automatic:Boolean):Float{
            if(!automatic){scale=1f;reset();return scale}
            val draw=drawNanos/1e9f;val gap=gapNanos/1e9f
            // Loading, pause/resume and portal construction do not count as sustained rendering load.
            if(gap<=0f || gap>.25f || draw>.25f){reset();return scale}
            val target=1f/fps.coerceIn(15,60);val duration=max(draw,gap)
            if(duration>target*1.38f){pressure+=gap;calm=0f}else{pressure=max(0f,pressure-gap*.5f);if(duration<target*1.16f)calm+=gap else calm=0f}
            if(pressure>2.5f){scale=max(.80f,scale-.05f);reset()}
            if(calm>12f){scale=min(1f,scale+.025f);reset()}
            return scale
        }
    }
}
