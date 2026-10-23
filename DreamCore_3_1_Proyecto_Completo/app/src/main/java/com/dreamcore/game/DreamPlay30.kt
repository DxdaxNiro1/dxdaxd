package com.dreamcore.game

import kotlin.math.*

/** Bounded persistent play state; uses the existing CRC-protected save, without resetting older journeys. */
class DreamPlay30(val story:DreamChronicle) {
    data class Node(val key:String,val title:String,val x:Float,val z:Float,val model:String="",val range:Float=3.1f)
    data class Choice(val key:String,val title:String)
    data class Panel(val title:String,val body:String,val choices:List<Choice>)
    data class Effect(val text:String="",val sound:String="",val target:String?=null,val panel:Panel?=null,val refresh:Boolean=false)
    data class Pose(val x:Float,val y:Float,val z:Float,val yaw:Float=0f)
    data class Mark(val world:String,val x:Float,val y:Float,val z:Float,val yaw:Float,val chalk:Boolean)
    data class Sound(val name:String,val pan:Float,val inside:Boolean,val gain:Float)
    private val state=story.flags.filter{it.startsWith("v30:")&&'=' in it}.associate{it.substringAfter("v30:").substringBefore('=') to it.substringAfter('=')}.toMutableMap()
    private val nodeCache=HashMap<String,List<Node>>()
    private val trail=ArrayDeque<Pose>();private var sampleClock=0f;private var mimicClock=0f
    var mimic=Pose(0f,0f,0f);private set
    var mimicVisible=false;private set
    var time=0f;private set
    var eyes=0f;private set
    var closing=false;private set
    private var closeAge=0f;private var blackoutDone=false
    var darkAdapt=0f;private set
    var telescope=false;private set
    private var focus=0f;private var perspectiveFocus=0f
    var dragging:String?=null;private set
    private var dragClock=0f
    var revision=0;private set
    var marksRevision=0;private set
    var trainProgress=0f;private set
    private var trainSeconds=0f
    private var destination:String?=null
    var shadowRise=0f;private set
    var carousel=0f;private set
    private var carouselSpeed=0f
    private var recording=false;private var recordAge=0f
    private val recordingEvents=ArrayList<String>()
    private var playbackAge=-1f;private var playbackAt=0;private var playback=emptyList<String>()
    private val sounds=ArrayDeque<Sound>();private var responseDelay=-1f;private var responseNote="piano0"
    private var lastWorld="";private var lastFootX=Float.NaN;private var lastFootZ=0f
    private var pianoTail=""
    fun value(key:String,default:String="")=state[key]?:default
    fun set(key:String,value:String){state[key]=value;val prefix="v30:$key=";story.flags.removeAll{it.startsWith(prefix)};story.flags.add(prefix+value)}
    fun number(key:String,default:Int=0)=value(key).toIntOrNull()?.coerceIn(-999999,999999)?:default
    fun flag(key:String)=value(key)=="1"
    fun setFlag(key:String,on:Boolean=true){set(key,if(on)"1" else "0")}
    fun phase(id:String):Int=if(number("sleep")==0||DreamVigilia27.dark(id))0 else when(id){DreamWorlds.POOLS->2;DreamWorlds.MEADOW->1;DreamHorizons.CITY,DreamRoutes23.CITY->3;else->(number("sleep")+id.hashCode().ushr(2))%4}
    fun note(key:String,text:String){set("note:$key",text.take(220))}
    fun notes()=story.flags.filter{it.startsWith("v30:note:")||it.startsWith("v30:annotation:")}.sorted().map{it.substringAfter('=')}
    fun journalNodes()=listOf("tren" to "El último tren","ojos" to "El parpadeo","tiza" to "Las marcas","sonido" to "La grabación","piano" to "La melodía","maqueta" to "La casa pequeña","agua" to "El depósito","cielo" to "El telescopio","ventana" to "La ventana","boleto" to "Los dos billetes")
    fun toggleLink(a:String,b:String){if(a==b)return;val key="link:"+listOf(a,b).sorted().joinToString(":");setFlag(key,!flag(key))}
    fun links()=story.flags.filter{it.startsWith("v30:link:")&&it.endsWith("=1")}.sorted().map{it.substringAfter("v30:link:").substringBefore('=')}
    fun annotate(key:String,text:String){if(key in journalNodes().map{it.first})set("annotation:$key",text.take(180))}
    fun station(id:String)=nodes(id).firstOrNull{it.key=="train"}
    fun trainAvailable()=time%64f in 7f..36f
    fun nodes(id:String):List<Node> = nodeCache.getOrPut(id){when(id){
        DreamWorlds.STATION->listOf(Node("train","Esperar / subir al último tren",-12f,-2f,"station"),Node("library-door","Biblioteca de lugares olvidados",12f,-3f,"gate"))
        DreamWorlds.MEADOW->listOf(Node("train","Andén del pueblo",7f,-50f,"station"))
        DreamHorizons.CITY->listOf(Node("train","Andén de la ciudad",0f,15f,"station"))
        DreamVoyage.REFUGE->listOf(Node("sleep","Dormir y comenzar otro sueño",-5.4f,-17.5f),Node("ticket","Dos billetes sin fecha",8f,-6f,"tickets"),Node("chair","Mover la silla",10f,-13f,"chair"),Node("window","Abrir la ventana alta",12f,-12f,"window"),Node("kit","Tiza y grabadora",-10f,-7f,"kit"))
        DreamPocket.TRAIN->listOf(Node("train-route","Elegir estación",0f,2f,"board"),Node("train-bell","Tocar la campana del vagón",1f,-28f,"bell"))
        DreamWorld30.LIBRARY->listOf(Node("train","Andén de la biblioteca",-22f,5f,"station"),Node("piano","Tocar el piano",-12f,-18f,"piano"),Node("projector","Cambiar diapositiva",12f,-40f,"projector"),Node("model","Mover la puerta de la maqueta",-12f,-58f,"model"),Node("wardrobe","Mover el armario",15f,-76f,"wardrobe"),Node("window","Abrir ventana hacia otro cielo",25f,-26f,"window"),Node("book0","Leer: los andenes",-19f,-32f,"book"),Node("book1","Leer: lo que oye una casa",-19f,-44f,"book"),Node("book2","Leer: un dibujo del parque",18f,-55f,"book"),Node("hidden-door","Examinar detrás del armario",15f,-76.1f,"gate"),Node("perspective","Observar el marco incompleto",0f,-94f,"marker"),Node("perspective-door","Cruzar el marco alineado",0f,-124f,"gate"),Node("piano-reward","Escuchar tras el piano",-15f,-23f,"relic"),Node("memory-room","Entrar en la habitación proyectada",21f,-47f,"gate"))
        DreamWorld30.FAIR->listOf(Node("train","Andén de la feria",-28f,9f,"station"),Node("valve","Girar las válvulas del depósito",-12f,-32f,"valve"),Node("barter","Ofrecer un objeto a la presencia",27f,-44f,"barter"),Node("carousel","Escuchar el carrusel",0f,-59f,"carousel"),Node("gate","Examinar la puerta de la maqueta",0f,-95f,"gate"),Node("telescope","Mirar por el telescopio",12f,-133f,"telescope"),Node("water-reward","Recoger lo que guardaba el agua",-25f,-48f,"relic"))
        DreamWorld30.SECRET->listOf(Node("train","Regresar al tren",0f,5f,"station"),Node("secret-reward","Leer el reloj sin agujas",0f,-48f,"clock"),Node("telescope","Observar el horizonte",8f,-65f,"telescope"))
        else->emptyList()
    }}
    fun pose(node:Node,id:String):Pose{
        if(node.key!="chair"&&node.key!="wardrobe")return Pose(node.x,DreamJourney.surface(id,node.x,node.z),node.z)
        val bits=value("pos:${node.key}:$id").split(',');val x=bits.getOrNull(0)?.toIntOrNull()?.div(100f)?:node.x;val z=bits.getOrNull(1)?.toIntOrNull()?.div(100f)?:node.z
        return Pose(x,DreamJourney.surface(id,x,z),z)
    }
    fun nearest(id:String,x:Float,z:Float):Node?=nodes(id).filter{(it.key!="water-reward"||number("water",2)==0)&&(it.key!="piano-reward"||flag("piano:tune"))&&(it.key!="perspective-door"||flag("perspective"))}.minByOrNull{val p=pose(it,id);hypot(x-p.x,z-p.z)}?.takeIf{val p=pose(it,id);hypot(x-p.x,z-p.z)<it.range}
    fun extraFloor(id:String,x:Float,z:Float):Float {
        val chair=nodes(id).firstOrNull{it.key=="chair"}?:return 0f
        val p=pose(chair,id);return if(dragging!="chair"&&abs(x-p.x)<.58f&&abs(z-p.z)<.58f)1.05f else 0f
    }
    fun clearPath(id:String,x:Float,z:Float):Boolean{
        if(id==DreamWorld30.FAIR){if(z in -96f..-94f && abs(x)<6f&&!flag("model:open"))return false
            if(x in -32f..-18f&&z in -54f..-40f&&number("water",2)>0)return false}
        if(id==DreamWorld30.LIBRARY){if(x in 20f..23f&&z in -51f..-43f&&number("slide")<3)return false
            for(n in nodes(id).filter{it.key=="wardrobe"}){val p=pose(n,id);if(abs(x-p.x)<1.45f&&abs(z-p.z)<.55f)return false}}
        return true
    }
    fun marks():List<Mark> = story.flags.filter{it.startsWith("v30:mark:")}.sortedBy{it.substringAfter("v30:mark:").substringBefore('=').toIntOrNull()?:0}.mapNotNull{row->
        val p=row.substringAfter('=').split(',');if(p.size!=6)return@mapNotNull null
        val a=p.drop(1).map{it.toFloatOrNull()?:return@mapNotNull null};if(a.any{!it.isFinite()})return@mapNotNull null
        Mark(p[0],a[0]/100f,a[1]/100f,a[2]/100f,a[3]/100f,a[4]>0f)
    }
    private fun mark(id:String,x:Float,y:Float,z:Float,yaw:Float,chalk:Boolean){
        val serial=number("mark-seq")+1;set("mark-seq",serial.toString())
        val existing=story.flags.filter{it.startsWith("v30:mark:")}.sortedBy{it.substringAfter("v30:mark:").substringBefore('=').toIntOrNull()?:0}
        if(existing.size>=72){val old=existing.first();story.flags.remove(old);state.remove(old.substringAfter("v30:").substringBefore('='))}
        set("mark:$serial",listOf(id,(x*100).roundToInt(),(y*100).roundToInt(),(z*100).roundToInt(),(yaw*100).roundToInt(),if(chalk)1 else 0).joinToString(","));marksRevision++
    }
    fun closeEyes():Effect {if(closing)return Effect();closing=true;closeAge=0f;blackoutDone=false;telescope=false;return Effect("Cierras los ojos. El mundo sigue sonando.")}
    fun observeSound(name:String,pan:Float,inside:Boolean,gain:Float){if(recording&&recordingEvents.size<32)recordingEvents.add("${(recordAge*10).toInt()},$name,${(pan*100).toInt()},${if(inside)1 else 0},${(gain*100).toInt()}")}
    fun drainSounds():List<Sound>{val out=sounds.toList();sounds.clear();return out}
    private fun sound(name:String,pan:Float=0f,inside:Boolean=true,gain:Float=.15f){sounds.add(Sound(name,pan,inside,gain))}
    fun toolsPanel()=Panel("Objetos del viaje",if(recording)"Grabando los sonidos del juego: ${recordAge.toInt()}/12 s." else "Graba sonidos del sueño, deja una marca o cierra los ojos. No se usa el micrófono.",listOf(Choice("eyes","Cerrar los ojos"),Choice("chalk","Dibujar una flecha con tiza"),Choice("record",if(recording)"Guardar grabación" else "Grabar 12 segundos"),Choice("play","Reproducir grabación"),Choice("release","Soltar objeto / telescopio")))
    fun panel(node:Node,id:String):Panel?=when(node.key){
        "train-route"->Panel("El último tren",if(destination!=null)"El vagón ya está en marcha. Espera la llegada." else "Elige dónde bajar. El viaje dura doce segundos; puedes caminar por el vagón.",if(destination!=null)emptyList()else listOf(Choice("ride:${DreamWorlds.MEADOW}","El pueblo"),Choice("ride:${DreamHorizons.CITY}","La ciudad"),Choice("ride:${DreamWorld30.LIBRARY}","Biblioteca"),Choice("ride:${DreamWorld30.FAIR}","Feria detenida"))+if(number("ticket")>0&&!flag("ticket:spent"))listOf(Choice("ride:${DreamWorld30.SECRET}",if(number("ticket")==1)"Parada sin cartel · observatorio" else "Parada sin cartel · estación pálida"))else emptyList())
        "ticket"->Panel("Dos destinos",if(flag("ticket:spent"))"Este billete ya hizo su viaje. Dormir inicia otro recorrido; tus descubrimientos permanecen." else "Puedes llevar un solo billete en este recorrido. El otro esperará al próximo sueño.",if(flag("ticket:spent"))emptyList()else listOf(Choice("ticket:1","Billete azul · observatorio"),Choice("ticket:2","Billete rojo · estación pálida")))
        "piano"->Panel("Piano de la biblioteca","Las teclas responden. La inscripción del libro habla de casa, sol, casa, lluvia.",(0..6).map{Choice("piano:$it",listOf("Do","Re","Mi","Fa","Sol","La","Si")[it])})
        "projector"->Panel("Proyector","Cada diapositiva ocupa un rincón de la habitación.",listOf(Choice("slide:1","Una casa al anochecer"),Choice("slide:2","Una piscina"),Choice("slide:3","El amanecer interminable")))
        "valve"->Panel("Depósito de la feria","Nivel actual: ${number("water",2)}. Las escaleras bajan hasta una compuerta.",listOf(Choice("water:0","Vaciar el depósito"),Choice("water:1","Nivel intermedio"),Choice("water:2","Llenar el depósito")))
        "barter"->Panel("La presencia espera","No se acerca. Extiende una mano y espera tu decisión.",listOf(Choice("trade:chalk","Ofrecer una marca de tiza"),Choice("trade:record","Ofrecer una grabación"),Choice("trade:ticket","Ofrecer el billete sin usar")))
        else->null
    }
    fun use(key:String,id:String,x:Float,y:Float,z:Float,yaw:Float):Effect{
        when {
            key=="tools"->return Effect(panel=toolsPanel())
            key=="eyes"->return closeEyes()
            key=="release"->{dragging=null;telescope=false;revision++;return Effect("Vuelves a tener las manos libres.")}
            key=="chalk"->{if(!flag("kit"))return Effect("La tiza espera sobre la mesa del refugio.");mark(id,x,y-1.63f,z- .4f,yaw,true);note("tiza","Una marca registra dónde estuviste; comprueba su dirección cuando regreses.");return Effect("Dejas una flecha sobre el suelo.","chalk30")}
            key=="record"->{if(!flag("kit"))return Effect("Recoge la grabadora en el refugio.");if(recording)return finishRecording();recording=true;recordAge=0f;recordingEvents.clear();set("record-world",id);sound(if(DreamPower.indoor(id))"room30" else "wind30",0f,DreamPower.indoor(id),.1f);return Effect("La grabadora escucha los sonidos del sueño durante doce segundos.")}
            key=="play"->{val data=value("record");if(data.isEmpty())return Effect("La cinta todavía está vacía.");playback=data.split(';').take(32);playbackAt=0;playbackAge=0f
                if(id==DreamWorld30.LIBRARY&&value("record-world")!=id){setFlag("sound:response");responseDelay=3.4f;note("sonido","La biblioteca respondió a un sonido traído desde otro lugar.")};return Effect("La cinta reproduce ${playback.size} sonidos de ${DreamJourney.title(value("record-world",DreamWorlds.STATION))}.")}
            key.startsWith("piano:")->{val n=key.substringAfter(':').toIntOrNull()?.coerceIn(0,6)?:0;sound("piano$n");pianoTail=(pianoTail+n.toString()).takeLast(4);set("piano:last",pianoTail);responseDelay=2.2f;responseNote="piano${(n+2)%7}"
                if(pianoTail=="0401"){setFlag("piano:tune");note("piano","La melodía tuvo una respuesta al otro lado del muro.");revision++;return Effect("Una cerradura suena detrás del escenario. La habitación sigue vacía.")};return Effect()}
            key.startsWith("ride:")->{if(id!=DreamPocket.TRAIN||destination!=null)return Effect("Espera a que el vagón llegue.");val target=key.substringAfter(':');if(target !in listOf(DreamWorlds.MEADOW,DreamHorizons.CITY,DreamWorld30.LIBRARY,DreamWorld30.FAIR,DreamWorld30.SECRET))return Effect()
                if(target==DreamWorld30.SECRET){if(number("ticket")==0||flag("ticket:spent"))return Effect("Necesitas elegir un billete sin usar.");setFlag("ticket:spent");set("secret-variant",number("ticket").toString())}
                destination=target;trainSeconds=0f;trainProgress=0f;set("train-target",target);sound("train30",0f,true,.14f);return Effect("El tren parte hacia ${DreamJourney.title(target)}. Puedes caminar por el vagón.")}
            key.startsWith("ticket:")->{if(flag("ticket:spent")||(number("ticket")>0&&number("ticket").toString()!=key.substringAfter(':')))return Effect("El próximo billete espera a otro sueño.");set("ticket",key.substringAfter(':'));note("boleto","Dos billetes, un recorrido. Dormir vuelve a abrir la elección.");return Effect("Guardas un billete. El otro queda sobre la mesa.","paper30",refresh=true)}
            key.startsWith("slide:")->{set("slide",key.substringAfter(':'));revision++;note("proyector","La luz de una diapositiva ocupa un lugar real en la biblioteca.");return Effect("La imagen deja una forma dentro de la habitación.","projector30",refresh=true)}
            key.startsWith("water:")->{if(id!=DreamWorld30.FAIR)return Effect();set("water",key.substringAfter(':'));revision++;note("agua","La línea de humedad recuerda hasta dónde llegaba el agua.");return Effect("Las tuberías responden. El agua cambia de nivel.","valve30",refresh=true)}
            key.startsWith("trade:")->{val item=key.substringAfter(':');val allowed=when(item){"chalk"->flag("kit");"record"->value("record").isNotBlank();"ticket"->number("ticket")>0&&!flag("ticket:spent");else->false};if(!allowed)return Effect("No llevas ese objeto.")
                if(item=="record")set("record","");if(item=="ticket"){set("ticket","0");setFlag("ticket:spent")};setFlag("gift:glass");setFlag("trade:$item");note("presencia","La presencia devolvió un cristal. Después empezó a mirar la estación.");revision++;return Effect("Deja un cristal en el suelo. Una luz se enciende en otro lugar.","tone3",refresh=true)}
        }
        val node=nodes(id).firstOrNull{it.key==key}?:return Effect()
        panel(node,id)?.let{return Effect(panel=it)}
        return when(key){
            "train"->if(trainAvailable()){set("train-origin",id);Effect("Subes al último tren. La tabla junto a la entrada permite elegir estación.","train30",DreamPocket.TRAIN)}else Effect("El andén está vacío. El tren vuelve en ${(if(time%64f<7f)7f-time%64f else 71f-time%64f).toInt()+1} segundos.")
            "library-door"->Effect("Una biblioteca espera detrás del Umbral.",target=DreamWorld30.LIBRARY)
            "kit"->{setFlag("kit");setFlag("tool:chalk");setFlag("tool:recorder");revision++;Effect("Recoges tiza y grabadora. Usa OBJETOS durante la exploración.","paper30",refresh=true)}
            "sleep"->{set("sleep",(number("sleep")+1).toString());set("ticket","0");setFlag("ticket:spent",false);closing=true;closeAge=0f;blackoutDone=true;revision++;note("tren","El sueño cambia la hora de algunos lugares y devuelve los billetes a la mesa.");Effect("Al despertar, el pueblo conserva el anochecer. Otros lugares también cambiaron de hora.","room30",refresh=true)}
            "chair","wardrobe"->{dragging=if(dragging==key)null else key;revision++;Effect(if(dragging==null)"Dejas el mueble en su sitio." else "Sujetas el mueble. Camina despacio para moverlo; vuelve a interactuar para soltarlo.")}
            "window"->{if(id==DreamVoyage.REFUGE){val chair=nodes(id).first{it.key=="chair"};val p=pose(chair,id);if(hypot(p.x-12f,p.z+12f)>2.2f)return Effect("El pestillo está demasiado alto. La silla puede ayudarte.")};setFlag("window:$id",!flag("window:$id"));revision++;note("ventana","El aire de la ventana no corresponde con el exterior de la casa.");Effect(if(flag("window:$id"))"Abres el marco. Entra aire de un cielo distinto." else "Cierras la ventana. La habitación recupera su eco.",if(flag("window:$id"))"wind30" else "wood",refresh=true)}
            "model"->{setFlag("model:open",!flag("model:open"));revision++;note("maqueta","La puerta de la maqueta y la puerta del parque comparten una bisagra.");Effect("La pequeña puerta gira. Un edificio de la feria repite el movimiento.","wood",refresh=true)}
            "hidden-door"->{val p=pose(nodes(id).first{it.key=="wardrobe"},id);if(hypot(p.x-15f,p.z+76f)<2f)return Effect("El armario tapa la entrada. Puedes moverlo.");setFlag("hidden:wardrobe");note("armario","Tras el armario había una nota dibujada del depósito.");Effect("Encuentras el plano del depósito y un espacio para entrar detrás del armario.","paper30",refresh=true)}
            "memory-room"->if(number("slide")>=3)Effect("La diapositiva ya tiene suelo. La habitación está abierta.",refresh=true)else Effect("Todavía falta la diapositiva del amanecer.")
            "book0","book1","book2"->{val text=when(key){"book0"->"PLANO DE ANDENES: Umbral — Pueblo — Ciudad — Biblioteca — Feria. Hay un nombre borrado. Los billetes se eligen en el refugio.";"book1"->"LO QUE OYE UNA CASA: una voz no necesita pertenecer a este lugar. Cuatro notas: casa, sol, casa, lluvia. En el dibujo, Do es la casa, Sol es el sol y Re es la lluvia.";else->"DIBUJO DEL PARQUE: una maqueta frente a una puerta; agua hasta la segunda raya. Un círculo representa algo que solo se ve ampliado."};note(key,text);Effect(panel=Panel("Biblioteca de lugares olvidados",text,emptyList()))}
            "telescope"->{telescope=!telescope;focus=0f;Effect(if(telescope)"Apoyas la mirada en el telescopio. Busca el pequeño anillo junto al planeta." else "Te apartas del telescopio.")}
            "gate"->Effect(if(flag("model:open"))"La puerta está abierta, como en la maqueta." else "La bisagra no se mueve desde aquí. En la biblioteca hay una copia de este edificio.")
            "carousel"->{note("feria","El carrusel gira cuando no lo miras; al volver la cabeza tarda en detenerse.");Effect("El motor suena detrás de ti. Una montura ocupa ahora otro lugar.","carousel30")}
            "water-reward"->{if(number("water",2)>0)Effect("El agua todavía cubre la compuerta.")else{setFlag("water:relic");revision++;Effect("Encuentras una llave de cristal y el dibujo de un ojo cerrado.","tone2",refresh=true)}}
            "secret-reward"->{val v=number("secret-variant",1);setFlag("secret:seen:$v");note("destino:$v",if(v==1)"El observatorio tenía un reloj que miraba al cielo." else "La estación pálida esperaba una llegada que nunca aparecía en su horario.");Effect("El reloj guarda este destino en tu diario.","tone3")}
            "train-bell"->Effect("La campana responde desde otro vagón.","train30")
            "perspective-door"->if(flag("perspective"))Effect("El marco conduce a una mañana que no termina.",target=DreamPocket.DAWN)else Effect("Las piezas todavía están separadas.")
            "piano-reward"->{setFlag("piano:heard");note("respuesta","Detrás del piano no había nadie. La respuesta queda en la cinta.");sound("piano4",.7f,true,.16f);Effect("Una nota regresa desde la pared vacía.")}
            "perspective"->Effect("El marco parece disperso. Retrocede hasta la baldosa circular y mira hacia el fondo.")
            else->Effect()
        }
    }
    private fun finishRecording():Effect{recording=false;set("record",recordingEvents.joinToString(";"));note("sonido","Una cinta conserva sonidos del lugar donde la grabaste.");return Effect("Grabación guardada: ${recordingEvents.size} sonidos.","tape30")}
    fun enter(id:String){telescope=false;dragging=null;trail.clear();lastFootX=Float.NaN;lastWorld=id
        if(id==DreamPocket.TRAIN&&destination==null){val saved=value("train-target");if(saved.isNotEmpty()){destination=saved;trainSeconds=0f}}
        if(id!=DreamPocket.TRAIN){destination=null;set("train-target","")}}
    fun step(id:String,x:Float,y:Float,z:Float,yaw:Float,pitch:Float,moving:Boolean,dt:Float):Effect?{
        if(lastWorld!=id)enter(id)
        time+=dt
        if(closing){closeAge+=dt;eyes=when{closeAge<.45f->closeAge/.45f;closeAge<2.5f->1f;else->(1f-(closeAge-2.5f)/.65f).coerceAtLeast(0f)}
            if(closeAge>1f&&!blackoutDone){blackoutDone=true;set("blink",(number("blink")+1).toString());revision++
                if(flag("kit")&&number("blink")%3==0){val last=marks().lastOrNull{it.chalk&&it.world==id};if(last!=null)mark(id,last.x+1.2f,last.y,last.z-2f,last.yaw+PI.toFloat(),true)}}
            if(closeAge>3.15f){closing=false;eyes=0f;return Effect("Al abrir los ojos, algo cercano parece haber cambiado.",refresh=true)}}
        val dark=DreamPower.indoor(id)||phase(id)==3||DreamVigilia27.dark(id)
        darkAdapt+=(if(dark)dt/18f else -dt/2f);darkAdapt=darkAdapt.coerceIn(0f,1f)
        if(recording){recordAge+=dt;if(recordAge<dt*1.1f)set("record-world",id);if(recordAge>=12f)return finishRecording()}
        if(playbackAge>=0f){playbackAge+=dt
            while(playbackAt<playback.size){val p=playback[playbackAt].split(',');val at=p.getOrNull(0)?.toIntOrNull()?.div(10f)?:0f;if(at>playbackAge)break
                if(p.size==5)sound(p[1],(p[2].toIntOrNull()?:0)/100f,p[3]=="1",((p[4].toIntOrNull()?:10)/100f).coerceIn(0f,.25f));playbackAt++};if(playbackAt>=playback.size)playbackAge=-1f}
        if(responseDelay>0f){responseDelay-=dt;if(responseDelay<=0f)sound(responseNote,-.75f,true,.14f)}
        if(id==DreamPocket.TRAIN&&destination!=null){trainSeconds+=dt;trainProgress=(trainSeconds/12f).coerceIn(0f,1f);if(trainSeconds>=12f){val target=destination!!;destination=null;set("train-target","");note("tren","Bajaste en ${DreamJourney.title(target)}. El tren permite elegir otro recorrido.");return Effect("Próxima estación: ${DreamJourney.title(target)}.","train30",target)}}
        sampleClock+=dt;mimicClock+=dt
        if(sampleClock>=.15f){sampleClock=0f;trail.addLast(Pose(x,y,z,yaw));while(trail.size>18)trail.removeFirst();mimic=trail.first().let{Pose(it.x-22f,it.y-1.65f,it.z-26f,it.yaw)};mimicVisible=id==DreamWorlds.MEADOW&&mimicClock>3f&&DreamWorlds.canWalk(id,mimic.x,mimic.z)}
        dragging?.let{key->dragClock+=dt;if(dragClock>.12f){dragClock=0f;val n=nodes(id).firstOrNull{it.key==key};if(n!=null){val px=x+sin(yaw)*1.25f;val pz=z-cos(yaw)*1.25f
                    if(DreamWorlds.canWalk(id,px,pz)){set("pos:$key:$id","${(px*100).roundToInt()},${(pz*100).roundToInt()}");revision++}}}}
        if(!lastFootX.isFinite()){lastFootX=x;lastFootZ=z}
        if(moving&&hypot(x-lastFootX,z-lastFootZ)>2.6f){lastFootX=x;lastFootZ=z;mark(id,x,y-1.63f,z,yaw,false)}
        val chair=nodes(id).firstOrNull{it.key=="chair"}
        if(chair!=null){val p=pose(chair,id);val a=atan2(p.x-x,-(p.z-z));val seen=cos(a-yaw)>.5f;shadowRise+=(if(seen)-dt*.18f else dt*.35f);shadowRise=shadowRise.coerceIn(0f,1f)}
        if(id==DreamWorld30.FAIR){val seen=cos(atan2(-x,-(-69f-z))-yaw)>.60f;carouselSpeed+=(if(seen)-dt*.35f else dt*.2f);carouselSpeed=carouselSpeed.coerceIn(0f,.55f);carousel+=carouselSpeed*dt}
        if(telescope){val aim=cos(pitch)*cos(yaw)*.95f+sin(pitch)*.30f;focus=if(aim>.996f)focus+dt else 0f;if(focus>2f&&!flag("sky:sign")){setFlag("sky:sign");note("cielo","El telescopio reveló un anillo que no se ve a simple vista.");revision++;return Effect("Entre los planetas aparece un anillo que antes no podías distinguir.","tone3",refresh=true)}}
        if(id==DreamWorld30.LIBRARY&&!flag("perspective")){val good=hypot(x,z+94f)<1.3f&&cos(yaw)>.99f&&abs(pitch-.012f)<.05f;perspectiveFocus=if(good)perspectiveFocus+dt else 0f
            if(perspectiveFocus>2.2f){setFlag("perspective");note("perspectiva","Los fragmentos formaban una puerta al mirarlos desde la baldosa.");revision++;return Effect("Las piezas encajan desde aquí. El marco completo deja ver una entrada.","tone2",refresh=true)}}
        return null
    }
}
