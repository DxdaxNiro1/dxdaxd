package com.dreamcore.game

import kotlin.math.*

object DreamObjects {
    data class Item(val key:String,val x:Float,val z:Float,val kind:String)
    data class Result(val text:String,val sound:String="chime",val pocket:String?=null,val warp:DreamJourney.Point?=null,val world:String?=null)
    private val cache=HashMap<String,List<Item>>()
    fun invalidate(id:String){cache.remove(id)}
    fun items(id:String):List<Item> = cache.getOrPut(id){(when(id) {
        DreamWorlds.MEADOW->listOf(Item("postman",-26f,-180f,"postman"),Item("hidden:lantern",-35f,-320f,"hidden"),Item("lamp:meadow",-40f,-43f,"lamp"),Item("phone:meadow",35f,-244f,"phone"),Item("street:garden",-150f,-289f,"street"),Item("water:night",1600f,-3f,"transition"))
        DreamVoyage.REFUGE->listOf(Item("tool:lantern",-5f,-8f,"tool"),Item("tool:compass",0f,-8f,"tool"),Item("tool:prism",5f,-8f,"tool"),Item("door:gravity",9f,-2f,"door"),Item("door:echoes",-9f,-2f,"door"),Item("door:eyes",-5f,2f,"door"),Item("door:island",5f,2f,"door"))
        DreamVoyage.LETTER->listOf(Item("reply:absent",0f,-22f,"reply"))
        DreamWorlds.HOUSE_MEADOW->listOf(Item("drawer:house",-5f,-92f,"drawer"))
        DreamExpansion.CLOUD_HOTEL->listOf(Item("mail:1",0f,-160f,"mailbox"),Item("radio:hotel",12f,-100f,"radio"),Item("phone:hotel",0f,-342f,"phone"),Item("door:hotel",-12f,-175f,"door"),Item("lift:rain",15f,-410f,"transition"))
        DreamExpansion.TUNNELS->listOf(Item("symbol:OJO",-8f,-100f,"symbol"))
        DreamWorlds.GARDEN->listOf(Item("symbol:ESPIRAL",8f,-170f,"symbol"),Item("door:dawn",-12f,-390f,"door"))
        DreamExpansion.CHESS->listOf(Item("symbol:CASA",9f,-290f,"symbol"))
        DreamExpansion.ROAD->listOf(Item("symbol:SOL",9f,-340f,"symbol"))
        DreamWorlds.AQUARIUM->listOf(Item("door:train",12f,-245f,"door"),Item("mail:0",-8f,-220f,"mailbox"))
        DreamWaterpark.ID->listOf(Item("phone:park",DreamWaterpark.centre(-200f)+11f,-209f,"phone"))
        DreamWorlds.HOUSE_CRATER->listOf(Item("choice:awaken",-5f,-244f,"choice"),Item("choice:stay",5f,-244f,"choice"),Item("drawer:house",-5f,-92f,"drawer"))
        DreamHorizons.CELESTIAL->listOf(Item("mail:2",DreamMail.addresses[2].x,-290f,"mailbox"),Item("door:cloudbed",DreamHorizons.curve(-115f)+12f,-115f,"door"),Item("door:tower",DreamHorizons.curve(-465f)-12f,-465f,"tower"))
        DreamHorizons.CITY->listOf(Item("memory:city",7f,-410f,"memory"))
        DreamHorizons.TOWER->listOf(Item("tower:remember",4f,-140f,"memory"))
        DreamPocket.LAUNDRY->listOf(Item("washer",4f,-12f,"radio"))
        DreamPocket.TRAIN->listOf(Item("radio:train",1.4f,-80f,"radio"))
        DreamPocket.DAWN->listOf(Item("drawer:dawn",4f,-20f,"drawer"))
        else->emptyList()
    })+DreamMystery.items(id)+DreamRules23.items(id)}
    fun nearest(id:String,x:Float,z:Float,feet:Float):Item? {
        var result:Item?=null;var best=6.25f
        for(item in items(id)){val dx=item.x-x;val dz=item.z-z;val square=dx*dx+dz*dz
            if(square<best&&abs(feet-DreamJourney.surface(id,item.x,item.z))<2.4f){best=square;result=item}}
        return result
    }
    fun doorTitle(key:String)=when(key){
        "door:eyes"->"Pradera de ojos · VISTA PREVIA"
        "door:island"->"La isla de la casa rosa"
        "door:echoes"->"Los lugares imposibles"
        "door:gravity"->"El suelo que se volvió cielo"
        "door:cloudbed"->"La cama entre nubes"
        "door:train"->"El tren bajo el agua"
        "door:dawn"->"El amanecer que no termina"
        "door:tower"->"La torre de las fotografías"
        else->"Un sueño escondido"
    }
    fun label(item:Item,story:DreamChronicle)=when(item.kind) {
        "investigation"->DreamMystery.label(item.key)
        "rule"->DreamRules23.label(item.key)
        "door"->"Entrar · "+doorTitle(item.key)
        "tool"->"Recoger "+when(item.key.substringAfter(':')){"lantern"->"linterna";"compass"->"brújula";else->"prisma"}
        "postman"->"Acercarte al cartero sin rostro"
        "mailbox"->"Entregar una carta"
        "hidden"->"La linterna revela una puerta"
        "reply"->"Leer la respuesta"
        "lamp"->if(item.key in story.flags)"Apagar lámpara" else "Encender lámpara"
        "phone"->"Contestar el teléfono"
        "drawer"->if(item.key in story.flags)"Examinar el cajón abierto" else "Abrir el cajón"
        "radio"->if(item.key in story.flags)"Apagar" else if(item.key=="washer")"Encender lavadora" else "Escuchar la radio"
        "symbol"->"Leer el símbolo"
        "street"->"Seguir la calle que antes no estaba"
        "tower"->if(DreamPhotography.unlocked(story))"Entrar en la torre" else "La torre espera cuatro fotografías"
        "transition"->if(item.key.startsWith("lift"))"Tomar el ascensor" else "Atravesar el agua"
        "memory"->"Recordar este lugar"
        "choice"->if(item.key.endsWith("awaken"))"Devolver el billete · despertar" else "Guardar el billete · permanecer"
        else->"Abrir la puerta"
    }
    fun use(item:Item,story:DreamChronicle):Result=when(item.kind) {
        "investigation"->DreamMystery.use(item.key,story)
        "rule"->DreamRules23.use(item.key,story)
        "tool"->{story.flags.add(item.key);DreamTools.equip(story,item.key.substringAfter(':'));Result("Objeto guardado. Puedes cambiarlo en Objetos, dentro de Ajustes.")}
        "postman"->Result(DreamMail.accept(story))
        "mailbox"->{val i=item.key.substringAfter(':').toInt();if(DreamMail.deliver(i,story))Result(DreamMail.addresses[i].reply,pocket=if(i==0)DreamVoyage.LETTER else null)else Result("El buzón espera una carta para esta dirección. Busca al cartero en el pueblo.")}
        "reply"->{story.flags.add("reply:absent");Result("No llegaste tarde. La mesa estaba preparada para ti. Al volver al refugio habrá otra taza.")}
        "hidden"->if(DreamTools.equipped(story)=="lantern")Result("La luz descubre una habitación que no tenía dirección.",pocket=DreamVoyage.LETTER)else Result("Parece una pared sin puerta.")
        "tower"->if(DreamPhotography.unlocked(story))Result("La torre por fin tiene una puerta. Dentro reconoces tus fotografías.",pocket=DreamHorizons.TOWER)else Result("Fotografía la casa, el mar del cielo, la ventana del hotel y la señal entre planetas. ${DreamPhotography.count(story)}/4.")
        "transition"->if(item.key.startsWith("lift")){story.flags.add("rain:arrival");Result("El ascensor estaba seco. Al abrirse, solo llueve donde tú estás.",world=DreamHorizons.CITY)}else Result("Al atravesar el agua, el otro lado es un jardín bajo las estrellas.",world=DreamWorlds.GARDEN)
        "memory"->{story.found.add(item.key+":memory");Result(if(item.key.startsWith("tower"))"Desde arriba, los cuatro lugares parecen partes de la misma casa." else "El último día de clases sigue esperando a que alguien vuelva.","tone3")}
        "lamp"->{val on=story.toggle(item.key);if(item.key=="lamp:meadow"){DreamMystery.record(story,if(on)"power" else "hush");story.flags.remove("investigation:"+(if(on)"hush" else "power"))};Result(if(on)"La lámpara se enciende. En otra casa, una ventana también responde." else "Apagas la lámpara. La otra casa recuerda la oscuridad.")}
        "phone"->{if(item.key=="phone:meadow")DreamMystery.record(story,"call");Result(story.answer(item.key),"tone"+((story.calls.size-1).coerceIn(0,3)))}
        "drawer"->{story.flags.add(item.key);story.flags.add("key:blue");Result("El cajón guarda una llave azul y una nota: responde, recuerda y devuelve el billete.","tone1")}
        "radio"->{val on=story.toggle(item.key);Result(if(on)"Una melodía incompleta llena el silencio." else "La habitación vuelve a quedarse vacía.",if(on)"tone2" else "chime")}
        "symbol"->{val glyph=item.key.substringAfter(':');story.symbols.add(glyph);Result("$glyph · ${story.symbols.size}/4. Las cuatro notas pertenecen a la misma melodía.","tone"+listOf("OJO","ESPIRAL","CASA","SOL").indexOf(glyph).coerceAtLeast(0))}
        "street"->Result("La calle termina ahora en un jardín. El camino de vuelta sigue abierto.",warp=if(story.layout(DreamWorlds.MEADOW)==0)DreamJourney.Point(-172f,-320f)else DreamJourney.Point(-210f,-390f))
        "choice"->{story.choose(item.key.endsWith("awaken"));Result(if(item.key.endsWith("awaken"))"Dejas el billete. Si recuerdas la melodía, la última puerta puede despertarte." else "Guardas el billete. Algo en el sueño te prepara un sitio.")}
        else->when(item.key) {
            "door:eyes"->Result("Vista previa: aquí las presencias observan. No hay ataques.",pocket=DreamNewDreams.EYES)
            "door:island"->Result("Una casa espera en lo alto. El tobogán baja hasta el océano.",pocket=DreamNewDreams.ISLAND)
            "door:echoes"->Result("La puerta reúne lugares que parecían existir solo en una imagen.",pocket=DreamReferences.ids.first())
            "door:gravity"->Result("El camino dobla el suelo. Camina despacio: la ciudad quedará debajo de ti.",pocket=DreamGravity.ID)
            "door:cloudbed"->Result("Una cama espera donde termina la habitación y empieza el universo.",pocket=DreamHorizons.BED)
            "door:train"->Result("La puerta abre al interior de un tren. El agua queda al otro lado del cristal.",pocket=DreamPocket.TRAIN)
            "door:dawn"->Result("El amanecer sigue esperando detrás de la puerta.",pocket=DreamPocket.DAWN)
            else->Result("La puerta reconoce lo que dejaste encendido.",pocket=if("radio:hotel" in story.flags && "key:blue" in story.flags)DreamPocket.DAWN else DreamPocket.LAUNDRY)
        }
    }
    private val nonSolid=setOf("postman","hidden","tool","reply","investigation","rule","door","street","tower","transition")
    fun blocked(id:String,x:Float,z:Float)=items(id).any {
        it.kind !in nonSolid && abs(x-it.x)<.82f && abs(z-(it.z-1.5f))<.70f
    }
    fun lamp(id:String)=if(id==DreamWorlds.MEADOW)DreamJourney.Point(-40f,-44.5f)else DreamJourney.Point(-6f,-92f)
    fun radioNear(id:String,x:Float,z:Float,story:DreamChronicle)=items(id).any{it.kind=="radio" && it.key in story.flags && hypot(x-it.x,z-it.z)<16f}
    fun geometry(id:String,story:DreamChronicle):DreamWorlds.Scene {
        val b=DreamMesh();val cream=floatArrayOf(.78f,.76f,.69f);val wood=floatArrayOf(.50f,.36f,.27f)
        if(id==DreamWorlds.STATION && story.flags.any{it.startsWith("ending:")}) {
            val awake="ending:DESPERTAR" in story.flags;val stay="ending:PERMANECER" in story.flags
            val tint=if(awake)floatArrayOf(.95f,.77f,.48f)else if(stay)floatArrayOf(.30f,.47f,.76f)else floatArrayOf(.68f,.60f,.76f)
            for(i in 0..11){val a=i*PI.toFloat()/6f;b.ellipsoid(15f*cos(a),5f+2f*sin(a),-3f+15f*sin(a),.25f,.25f,.25f,tint,6,9,36f)}
            if(awake)for(s in floatArrayOf(-1f,1f))b.box(s*12f,6f,-24f,4f,6f,.12f,tint,34f)
            if(stay)for(s in floatArrayOf(-1f,1f))b.box(s*13f,3f,-25f,3f,3f,.13f,tint,43f)
        }
        if(id==DreamWorlds.MEADOW) {
            for((x,z)in listOf(-172f to -320f,-210f to -390f)) {
                for(i in 0..7){val a=i*PI.toFloat()*.25f;b.ellipsoid(x+10f*cos(a),DreamJourney.surface(id,x,z)+.9f,z+10f*sin(a),1.4f,.9f,1.4f,floatArrayOf(.38f,.48f,.39f),5,8,4f)}
                b.box(x,DreamJourney.surface(id,x,z)+.8f,z-4f,2.1f,.15f,.6f,cream,43f)
            }
        }
        for(o in items(id)) {
            val y=DreamJourney.surface(id,o.x,o.z);val z=o.z-1.5f
            when(o.kind) {
                "rule"->{}
                "investigation"->{
                    if(o.key.endsWith(":call")||o.key.endsWith(":answer")){
                        DreamSculpt.cylinder(b,o.x,y,z,.08f,1.35f,wood,43f,8)
                        DreamSculpt.rounded(b,o.x,y+1.5f,z,.5f,.15f,.30f,.08f,cream,15f)
                        DreamShapes.tube(b,listOf(floatArrayOf(o.x-.42f,y+1.72f,z),floatArrayOf(o.x,y+1.81f,z),floatArrayOf(o.x+.42f,y+1.72f,z)),.095f,wood,40f,9)
                    }else if(o.key.endsWith(":power")||o.key.endsWith(":hush")){DreamSculpt.rounded(b,o.x,y+1.2f,z,.42f,.55f,.17f,.06f,cream,15f);b.box(o.x,y+1.25f,z+.19f,.08f,.20f,.03f,o.key.endsWith(":power").let{if(it)floatArrayOf(.53f,.71f,.43f)else floatArrayOf(.58f,.32f,.29f)},15f)}
                    else if(o.key.endsWith(":wire")){b.box(o.x,y+.12f,z,.5f,.12f,.4f,cream,43f)}
                    else if(o.key.endsWith(":visit")){b.box(o.x,y+1.6f,z,1f,1.6f,.04f,cream,34f)}
                    else if(o.key.endsWith(":leave")||o.key.endsWith(":stay")){b.box(o.x,y+1.25f,z,.4f,.025f,.25f,cream,43f)}
                }
                "postman"->{}
                "hidden"->{b.box(o.x,y+1.6f,z,1.2f,1.6f,.06f,cream,79f)}
                "tool"->if(o.key !in story.flags){
                    when(o.key.substringAfter(':')){
                        "lantern"->{b.ellipsoid(o.x,y+1.8f,z,.24f,.4f,.24f,cream,8,12,24f);for(d in floatArrayOf(-.42f,.42f))b.box(o.x,y+1.8f+d,z,.32f,.06f,.32f,wood,40f);DreamShapes.tube(b,(0..12).map{val a=it*PI.toFloat()/12f;floatArrayOf(o.x+.29f*cos(a),y+2.22f+.30f*sin(a),z)},.04f,wood,40f,7)}
                        "compass"->{b.ellipsoid(o.x,y+1.55f,z,.36f,.09f,.36f,cream,8,16,25f);b.box(o.x,y+1.66f,z,.025f,.018f,.27f,floatArrayOf(.71f,.25f,.28f),40f)}
                        else->{val c=floatArrayOf(.48f,.72f,.84f);fun pt(x:Float,h:Float,d:Float)=floatArrayOf(o.x+x,y+h,z+d)
                            b.triangle(pt(-.38f,1.35f,.3f),pt(.38f,1.35f,.3f),pt(0f,2.1f,.3f),c,25f);b.triangle(pt(.38f,1.35f,-.3f),pt(-.38f,1.35f,-.3f),pt(0f,2.1f,-.3f),c,25f)
                            b.quad(pt(-.38f,1.35f,-.3f),pt(-.38f,1.35f,.3f),pt(0f,2.1f,.3f),pt(0f,2.1f,-.3f),c,25f);b.quad(pt(0f,2.1f,-.3f),pt(0f,2.1f,.3f),pt(.38f,1.35f,.3f),pt(.38f,1.35f,-.3f),c,25f)}
                    }
                }
                "reply"->b.box(o.x,y+1.35f,z,.7f,.025f,.4f,cream,43f)
                "mailbox"->{b.box(o.x,y+.7f,z,.10f,.7f,.10f,wood,40f);b.box(o.x,y+1.5f,z,.6f,.4f,.4f,cream,26f);b.box(o.x,y+1.6f,z+.41f,.35f,.035f,.015f,wood)}
                "street"->{for(s in floatArrayOf(-1f,1f))b.box(o.x+s*2f,y+2f,z,.13f,2f,.15f,cream,26f)}
                "door","tower","transition"->{b.box(o.x,y+1.6f,z,1.1f,1.6f,.07f,cream,34f);for(s in floatArrayOf(-1f,1f))b.box(o.x+s*1.25f,y+1.7f,z,.1f,1.7f,.13f,wood,43f)}
                "lamp"->{b.box(o.x,y+1.3f,z,.08f,1.3f,.08f,wood,40f);b.ellipsoid(o.x,y+2.7f,z,.5f,.35f,.5f,cream,8,12,46f)}
                "symbol"->{b.box(o.x,y+1.6f,z,.75f,1.2f,.15f,wood,43f);b.ring(o.x,y+1.8f,z+.17f,.5f,.08f,cream);b.box(o.x,y+1.8f,z+.20f,.06f,.34f,.03f,cream,24f)}
                else->{
                    b.box(o.x,y+.9f,z,.48f,.9f,.42f,wood,43f)
                    b.box(o.x,y+1.8f,z,.68f,.08f,.56f,cream,26f)
                    val k=when(o.kind){"drawer"->47f;"radio"->48f;else->25f}
                    b.box(o.x,y+2.15f,z,.48f,.26f,.34f,if(o.kind=="phone")floatArrayOf(.26f,.29f,.29f)else wood,k)
                    if(o.kind=="phone")b.box(o.x,y+2.48f,z,.62f,.10f,.14f,wood,40f)
                }
            }
        }
        return b.finish()
    }
}
