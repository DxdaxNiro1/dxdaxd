package com.dreamcore.game

import kotlin.math.*

object DreamPoolsDetails {
    fun create():DreamWorlds.Scene {
        val b=DreamMesh();val cream=DreamShapes.ivory;val rose=DreamShapes.rose
        for(z in floatArrayOf(-4f,-12f,-20f,-28f)){
            val path=(0..32).map{val a=it*PI.toFloat()/32f;floatArrayOf(14.6f*cos(a),3.4f+6f*sin(a),z)}
            DreamShapes.tube(b,path,.20f,cream,26f,10)
            for(x in floatArrayOf(-14.9f,14.9f)){b.box(x,4.8f,z,.06f,2.2f,1.5f,floatArrayOf(.65f,.77f,.89f),34f);b.box(x,1.4f,z,.10f,.05f,1.5f,rose,26f)}
        }
        // Broad sky apertures and suspended sculpture give the first room its own silhouette.
        for(z in floatArrayOf(-8f,-20f,-31f)){
            b.box(0f,10.04f,z,5.2f,.025f,2.2f,floatArrayOf(.40f,.55f,.74f),34f)
            for(s in floatArrayOf(-1f,1f))b.box(s*5.3f,9.95f,z,.12f,.12f,2.4f,cream,26f)
        }
        for(s in floatArrayOf(-1f,1f)){
            b.box(s*14.92f,2.6f,-33f,.04f,2.5f,2f,floatArrayOf(.60f,.79f,.81f),51f)
            b.box(s*14.90f,5.3f,-33f,.10f,.15f,2.2f,rose,26f)
            for(i in 0..4)b.box(s*14.84f,2.8f,-34.6f+i*.8f,.07f,2.3f,.025f,cream,50f)
            for(i in 0..4)b.box(s*14.8f,.08f,-28f+i*6f,.15f,.06f,2.3f,rose,26f)
        }
        return b.finish()
    }
}
