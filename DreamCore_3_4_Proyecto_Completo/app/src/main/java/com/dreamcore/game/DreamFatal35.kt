package com.dreamcore.game

import kotlin.math.*

/** Deterministic encounter timeline; no Android, audio or rendering ownership. */
class DreamFatal35 {
    data class Style(val id:Int,val title:String,val reach:Float,val lift:Float,val duration:Float,val sound:String)
    companion object {
        val styles=linkedMapOf(
            "choir" to Style(1,"El coro cierra sus alas",2.8f,1.7f,4.8f,"choir"),
            "mask" to Style(2,"La máscara te levanta",2.3f,.9f,4.5f,"mask"),
            "witness" to Style(3,"El testigo abre el pecho",3f,1.4f,5f,"witness"),
            "crimson" to Style(4,"La presencia carmesí despliega sus garras",3f,1.5f,4.4f,"crimson"),
            "antler" to Style(5,"Las astas se inclinan sobre ti",2.8f,.7f,4.9f,"antler"),
            "gold" to Style(6,"El guardián cierra la corona",3f,2.1f,5.2f,"gold"),
            "star" to Style(7,"El ojo de la estrella se contrae",2.2f,.5f,4.6f,"star"),
            "observatory" to Style(8,"El observatorio baja la mirada",3.3f,1.2f,5.4f,"observatory"),
            "mushroom" to Style(9,"El herbario sujeta tus manos",2.7f,.5f,5.1f,"mushroom"),
            "water-eye" to Style(10,"El agua tira de ti hacia abajo",3.1f,-.7f,4.7f,"water_eye"),
            "tenant" to Style(11,"El inquilino te toma del cuello",2.2f,1.1f,4.8f,"tenant"),
            "curious" to Style(12,"El observador se dobla hacia ti",2.2f,.6f,4.5f,"curious"),
            "mourner" to Style(13,"La figura del duelo te abraza",2.4f,.8f,5.2f,"mourner"),
            "window" to Style(14,"La figura sale del marco",2.1f,.4f,4.6f,"window"),
            "cone" to Style(15,"El vigilante estira sus brazos",3.4f,1.2f,5f,"cone"),
            "root-eye" to Style(16,"Las raíces se cierran",3.6f,1.6f,5.3f,"root_eye"),
            "gentleman" to Style(17,"El caballero despliega las manos",3.4f,.8f,4.9f,"gentleman"),
            "seraph" to Style(18,"El serafín abre sus seis alas",3.4f,1.8f,5.5f,"seraph"))
        fun hostile(world:String,model:String)=world!=DreamVoyage.REFUGE&&world!=DreamWorlds.STATION&&model in styles
        private fun smooth(t:Float):Float{val v=t.coerceIn(0f,1f);return v*v*(3f-2f*v)}
        fun grip(model:String,t:Float):Float{val style=styles[model]?:return 0f;return smooth(t/style.duration/.24f)}
    }
    data class Frame(val model:String="",val style:Int=0,val time:Float=0f,val progress:Float=0f,val warning:Float=0f,val held:Boolean=false,val dead:Boolean=false,val blood:Float=0f,val fade:Float=0f,val lift:Float=0f,val scream:String="",val caption:String="",val target:DreamEntities32.Presence?=null)
    var frame=Frame();private set
    private var world="";private var target:DreamEntities32.Presence?=null;private var age=0f;private var warning=0f;private var warned="";private var grace=3f;private var screamed=false
    fun reset(id:String,protection:Float=7f){world=id;target=null;age=0f;warning=0f;warned="";grace=protection;screamed=false;frame=Frame()}
    fun step(id:String,x:Float,y:Float,z:Float,dt:Float,bodies:List<DreamEntities32.Presence>,clear:(Float,Float,Float)->Boolean={_,_,_->true}):Frame{
        if(world!=id)reset(id,3f)
        val d=dt.coerceIn(0f,.1f);grace=(grace-d).coerceAtLeast(0f)
        if(frame.dead)return frame.copy(scream="").also{frame=it}
        var current=target
        if(current==null){
            if(grace>0f)return Frame().also{frame=it}
            var nearest:DreamEntities32.Presence?=null;var gap=Float.POSITIVE_INFINITY
            for(p in bodies){
                if(!hostile(id,p.model))continue
                val floor=DreamJourney.surface(id,p.x,p.z)+p.y
                // Volumetric bodies, not horizontal-only tests through ceilings or floors.
                val low=floor-.6f;val high=floor+(if(p.model=="observatory")13f else if(p.model=="water-eye")3f else 6f)*p.scale
                if(y<low-1f||y>high+1f)continue
                val distance=hypot(x-p.x,z-p.z)-minOf(p.scale*.55f,2.7f)
                val reach=styles.getValue(p.model).reach
                if(distance<reach+1.8f&&distance<gap&&clear(p.x,y.coerceIn(low,high),p.z)){nearest=p;gap=distance}
            }
            if(nearest==null){warning=0f;warned="";return Frame().also{frame=it}}
            val key="${nearest.model}:${nearest.x}:${nearest.z}"
            if(warned!=key){warning=0f;warned=key}
            val style=styles.getValue(nearest.model)
            if(gap>style.reach){warning=0f;return Frame(model=nearest.model,warning=.25f,caption="${DreamCatalogue34.creatureName(nearest.model)} · mantén distancia").also{frame=it}}
            warning+=d
            if(warning<.85f)return Frame(model=nearest.model,warning=warning/.85f,caption="Retrocede · está reaccionando a tu presencia").also{frame=it}
            current=nearest;target=current;age=0f;screamed=false
        }
        val p=current;val s=styles.getValue(p.model);age+=d;val progress=(age/s.duration).coerceIn(0f,1f)
        val roar=if(progress>=.46f&&!screamed){screamed=true;"attack_${s.sound}35"}else ""
        val blood=smooth((progress-.70f)/.13f);val fade=smooth((progress-.84f)/.15f)
        frame=Frame(p.model,s.id,age,progress,held=true,dead=progress>=1f,blood=blood,fade=fade,lift=s.lift*smooth(progress/.27f),scream=roar,caption=when{progress<.28f->"Te sujeta · ${DreamCatalogue34.creatureName(p.model)}";progress<.46f->"${DreamCatalogue34.creatureName(p.model)} te observa";progress<.72f->s.title;else->"El sueño se rompe"},target=p)
        return frame
    }
    /** Model-specific pose channels: grip, rage, asymmetry and eye opening. */
    fun articulation(model:String):FloatArray{
        val f=frame;if(!f.held||f.model!=model)return floatArrayOf(0f,0f,0f,0f)
        val reach=grip(model,f.time);val rage=smooth((f.progress-.40f)/.12f)
        return floatArrayOf(reach,rage,sin(f.time*(1.2f+f.style*.23f))*.13f*rage,f.style.toFloat())
    }
}
