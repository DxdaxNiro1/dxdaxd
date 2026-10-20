package com.dreamcore.game

import kotlin.math.*

/** Nine optional small encounters give the existing landscapes a reason to explore. */
object DreamMemories {
    data class Piece(val scene:DreamWorlds.Scene,val blocks:List<DreamLands.Rect>)
    fun point(id:String)=when(id) {
        DreamExpansion.TUNNELS->DreamJourney.Point(-14f,-210f)
        DreamWorlds.HOUSE_MEADOW,DreamWorlds.HOUSE_CRATER->DreamJourney.Point(0f,-210f)
        else->DreamJourney.Point(35f,-250f)
    }
    fun secret(id:String):DreamLands.Secret {
        val p=point(id)
        val text=when(id) {
            DreamWorlds.MEADOW->"Recuerdo: alguien te esperaba en esta parada. El autobús nunca llegó."
            DreamWorlds.GARDEN->"Recuerdo: las luces guardan una noche que no termina."
            DreamExpansion.TUNNELS->"Recuerdo: una campana suena debajo del agua."
            DreamWorlds.HOUSE_MEADOW->"Recuerdo: el reloj tiene la hora de otro sueño."
            DreamWorlds.AQUARIUM->"Recuerdo: el mar olvidó cuál era su lado del cielo."
            DreamExpansion.CHESS->"Recuerdo: alguien dejó una corona fuera del tablero."
            DreamExpansion.CLOUD_HOTEL->"Recuerdo: el teléfono todavía espera una voz conocida."
            DreamExpansion.ROAD->"Recuerdo: una luz vuelve a casa por un camino vacío."
            else->"Recuerdo: el billete dice que ya llegaste, pero todavía falta despertar."
        }
        return DreamLands.Secret("memory",p.x,p.z,text)
    }
    fun create(id:String):Piece {
        val b=DreamMesh();val blocks=ArrayList<DreamLands.Rect>()
        val p=point(id);val x=p.x;val z=p.z
        val cream=floatArrayOf(.82f,.80f,.74f);val pink=floatArrayOf(.79f,.59f,.72f)
        val blue=floatArrayOf(.47f,.68f,.77f)
        fun solid(xx:Float,yy:Float,zz:Float,sx:Float,sy:Float,sz:Float,c:FloatArray,k:Float=26f) {
            b.box(xx,yy,zz,sx,sy,sz,c,k);blocks+=DreamLands.Rect(xx,zz,sx,sz)
        }
        when(id) {
            DreamWorlds.MEADOW,DreamExpansion.ROAD->{
                for(s in floatArrayOf(-1f,1f))solid(x+s*6f,3.3f,z-3f,.15f,3.3f,.15f,cream)
                b.box(x,6.7f,z-3f,6.8f,.20f,2.7f,pink,25f)
                b.box(x,3.5f,z-5f,5.8f,2.9f,.04f,blue,34f)
                solid(x+3f,.7f,z-3f,1.6f,.7f,.7f,cream)
                b.box(x+3f,1.7f,z-3.55f,1.6f,.6f,.08f,pink)
            }
            DreamExpansion.CLOUD_HOTEL->{
                for(s in floatArrayOf(-1f,1f))solid(x+s*2.5f,3.5f,z-3f,.15f,3.5f,2f,cream)
                b.box(x,7f,z-3f,2.8f,.18f,2.3f,pink)
                b.box(x,3.5f,z-5f,2.4f,3.3f,.05f,blue,34f)
                solid(x,1.6f,z-4f,.65f,.8f,.3f,cream)
                b.box(x,2.7f,z-3.55f,.45f,.4f,.15f,pink,25f)
                b.box(x,2.8f,z-3.35f,.09f,.26f,.12f,cream)
            }
            DreamWorlds.HOUSE_MEADOW->{
                // An off-centre clock, leaving the nave and previous gates fully open.
                b.ellipsoid(x+5f,4f,z-3f,2.4f,2.4f,.13f,cream,9,20,25f)
                b.box(x+5.5f,4f,z-2.8f,.5f,.07f,.05f,pink)
                b.box(x+5f,4.7f,z-2.75f,.07f,.7f,.05f,blue)
                solid(x+5f,1f,z-3f,.25f,1f,.25f,cream)
            }
            DreamWorlds.HOUSE_CRATER->{
                solid(x+4f,1.8f,z-2f,1f,1.8f,.6f,pink)
                b.box(x+4f,2.2f,z-1.32f,.7f,.6f,.03f,blue,34f)
                b.box(x+4f,1.1f,z-1.3f,.55f,.045f,.05f,cream)
            }
            DreamExpansion.CHESS->{
                b.ring(x,4f,z-5f,3.5f,.25f,cream)
                for(i in -2..2)b.box(x+i*1.2f,6.5f+(.4f*cos(i.toFloat())),z-5f,.3f,.8f,.3f,pink,25f)
                for(s in floatArrayOf(-1f,1f))solid(x+s*4f,2f,z-5f,.23f,2f,.23f,cream)
            }
            else->{
                for(s in floatArrayOf(-1f,1f))solid(x+s*4.5f,3.2f,z-4f,.2f,3.2f,.2f,cream)
                b.box(x,6.5f,z-4f,4.8f,.15f,.25f,pink)
                b.ellipsoid(x,5.2f,z-4f,.65f,.8f,.65f,blue,6,10,37f)
            }
        }
        // A cluster of softly drifting paper-lights, visible along the approach.
        for(i in 0 until 12) {
            val a=i*2.4f
            b.ellipsoid(x+sin(a)*(3f+i*.35f),3.4f+(i%4)*.65f,z+cos(a)*5f,
                .10f+(i%3)*.035f,.18f,.10f,if(i%2==0)pink else blue,3,5,37f)
        }
        return Piece(b.finish(),blocks)
    }
}
