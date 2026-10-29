package com.dreamcore.game

import kotlin.math.*

/** One registry drives positions, collisions, colour, signs and the travel map. */
object DreamHub35 {
    data class Sector(val group:DreamCatalogue34.Group,val number:Int,val colourName:String,val rgb:Int,val x:Float){
        val colour get()=floatArrayOf(((rgb shr 16)and 255)/255f,((rgb shr 8)and 255)/255f,(rgb and 255)/255f)
        val label get()="$number · ${group.title} · $colourName"
    }
    val sectors=listOf(
        Sector(DreamCatalogue34.Group.DREAMCORE,1,"Verde",0x63CBA6,-32f),
        Sector(DreamCatalogue34.Group.BACKROOMS,2,"Ámbar",0xE5BC60,-16f),
        Sector(DreamCatalogue34.Group.DARK,3,"Violeta",0xB198F2,0f),
        Sector(DreamCatalogue34.Group.TERROR,4,"Carmesí",0xDE7780,16f),
        Sector(DreamCatalogue34.Group.REFUGE,5,"Azul",0x82BFF2,32f))
    fun sector(group:DreamCatalogue34.Group)=sectors.first{it.group==group}
    fun colour(id:String)=sector(DreamCatalogue34.group(id)).rgb
    data class Gate(val target:String,val x:Float,val z:Float,val title:String,val node:String=""){
        val sector get()=sector(DreamCatalogue34.group(target))
        val label get()="${sector.number} · $title"
    }
    val gates=listOf(
        Gate(DreamJourney.sequence.first(),-32f,-9f,"Piscinas y sueños"),
        Gate(DreamReferences.ids.first(),-32f,-23f,"El barrio inclinado"),
        Gate(DreamRoutes23.SKY,-32f,-37f,"Aberturas del cielo"),
        Gate(DreamVigilia27.SKY_ISLAND,-25f,-37f,"El ojo entre nubes"),
        Gate(DreamBackrooms.ID,-16f,-25f,"Las luces"),
        Gate(DreamJourney.DARK,0f,-25f,"Las lunas dormidas"),
        Gate(DreamHorror32.ATRIUM,16f,-18f,"Los testigos"),
        Gate(DreamRainward34.ID,16f,-34f,"La ciudad que escucha llover"),
        Gate(DreamPocket.TRAIN,32f,-18f,"El último tren","train"),
        Gate(DreamWorld30.LIBRARY,32f,-34f,"Biblioteca","library-door"))
    fun portalAt(x:Float,z:Float)=gates.firstOrNull{it.node.isEmpty()&&hypot(x-it.x,z-it.z)<2.5f}?.target
    fun canWalk(x:Float,z:Float):Boolean{
        if(x !in -42f..42f||z !in -44f..18f)return false
        return gates.none{it.node.isEmpty()&&abs(z-it.z)<.55f&&abs(abs(x-it.x)-1.36f)<.46f}
    }
    fun create():DreamWorlds.Scene{
        val b=DreamMesh();val floor=floatArrayOf(.57f,.61f,.64f)
        fun ground(x:Float,z:Float)=DreamWorlds.groundHeight(x,z)
        for(x in -42 until 42 step 3)for(z in -45 until 18 step 3){
            fun p(xx:Float,zz:Float)=floatArrayOf(xx,ground(xx,zz)-.045f,zz)
            b.quad(p(x.toFloat(),z.toFloat()),p(x.toFloat(),z+3f),p(x+3f,z+3f),p(x+3f,z.toFloat()),floor,25f)
        }
        for(s in sectors){
            // Segmented paths follow the existing rolling floor; no raised obstacles.
            for(i in 0..16){val t=i/16f;val x=s.x*t;val z=3f-10f*t;b.box(x,ground(x,z)+.018f,z,.44f,.016f,.30f,s.colour,0f)}
            for(i in 0..30){val z=-7f-i;b.box(s.x,ground(s.x,z)+.018f,z,.55f,.016f,.29f,s.colour,0f)}
            // Number glyph made of in-world geometry, readable without relying on colour.
            val digits=arrayOf("010111010010111","111001111100111","111001111001111","101101111001001","111100111001111")
            for((i,c)in digits[s.number-1].withIndex())if(c=='1'){val x=s.x+(i%3-1)*.24f;val z=-4f-(i/3)*.24f;b.box(x,ground(x,z)+.023f,z,.09f,.02f,.09f,s.colour,0f)}
        }
        for(g in gates){
            val y=ground(g.x,g.z);val tint=g.sector.colour
            b.box(g.x,y+.02f,g.z+1.1f,2.1f,.025f,1.2f,tint,0f)
            if(g.node.isNotEmpty())continue
            val trim=floatArrayOf(.36f,.40f,.44f)
            for(side in floatArrayOf(-1f,1f))b.box(g.x+side*1.36f,y+1.6f,g.z,.18f,1.6f,.19f,trim,26f)
            b.box(g.x,y+3.3f,g.z,1.6f,.15f,.19f,tint,0f)
            // Literal colour material: all devices show the same category, no hue animation.
            b.box(g.x,y+1.6f,g.z,1.12f,1.6f,.035f,tint,0f)
        }
        return b.finish()
    }
}
