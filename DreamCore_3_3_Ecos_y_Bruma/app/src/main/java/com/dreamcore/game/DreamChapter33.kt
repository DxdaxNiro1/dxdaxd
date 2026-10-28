package com.dreamcore.game

import kotlin.math.*

/** Chapter, side stories, cross-world consequences and cooperative locks share the existing CRC save. */
class DreamChapter33(val story:DreamChronicle){
    data class Node(val key:String,val title:String,val x:Float,val z:Float,val model:String="c33-letter",val range:Float=2.8f)
    data class Event(val text:String="",val sound:String="",val sourceX:Float=0f,val sourceY:Float=1f,val sourceZ:Float=0f,val target:String?=null,val capture:Boolean=false)
    private val state33=story.flags.filter{it.startsWith("v33:")&&'=' in it}.associate{it.substringAfter("v33:").substringBefore('=') to it.substringAfter('=')}.toMutableMap()
    private val specialNodes33=HashMap<String,List<Node>>()
    private val cache=HashMap<String,List<Node>>()
    private var world="";private var clock=number("clock").toFloat();private var storedClock=clock.toInt();private var focus=0f;private var listen=0f;private var lastTrailCheck=0f
    private var footClock=0f
    private var sequence="";private var giantClock=0f;private var giantPass=false
    var past=false;private set
    var hidden="";private set
    var giant=0f;private set
    var trackerX=0f;private set
    var trackerZ=-57f;private set
    var pursuerX=0f;private set
    var pursuerZ=-70f;private set
    var chaseTime=0f;private set
    var chaseState=0;private set // 0 resting, 1 warning, 2 investigating, 3 following, 4 escaped
    var catchPending=false;private set
    var revision=0;private set
    var trainEvent=0;private set
    var echoAim=0f;private set
    private var sampledTrace:DreamPlay30.Mark?=null
    private var soundPulse:Pair<Float,Float>?=null
    fun value(key:String,default:String="")=state33[key]?:default
    fun number(key:String,default:Int=0)=value(key).toIntOrNull()?:default
    fun flag(key:String)=value(key)=="1"
    fun set(key:String,value:String){state33[key]=value.take(240);val prefix="v33:$key=";story.flags.removeAll{it.startsWith(prefix)};story.flags.add(prefix+value.take(240))}
    fun mark(key:String,on:Boolean=true){set(key,if(on)"1" else "0");revision++}
    val goal get()=number("goal")
    val trust get()=number("trust").coerceIn(-20,20)
    val active get()=goal in 1..3
    val completed get()=value("ending").isNotBlank()
    private val almaRoute=listOf(DreamWorlds.STATION,DreamExpansion.CLOUD_HOTEL,DreamWorlds.POOLS,DreamWorld30.LIBRARY,DreamWorlds.MEADOW,DreamArchive33.ID,DreamVoyage.REFUGE)
    private val memoryRoute=listOf(DreamWorlds.STATION,DreamWorlds.POOLS,DreamVoyage.REFUGE,DreamExpansion.CLOUD_HOTEL,DreamWorld30.LIBRARY,DreamWorlds.MEADOW,DreamArchive33.ID)
    private val exitRoute=listOf(DreamWorlds.STATION,DreamWorlds.POOLS,DreamWorld30.LIBRARY,DreamArchive33.ID,DreamExpansion.CLOUD_HOTEL,DreamWorlds.MEADOW,DreamVoyage.REFUGE)
    val evidenceWorlds get()=when(goal){2->memoryRoute;3->exitRoute;else->almaRoute}
    val stage get()=if(!active)0 else evidenceWorlds.indexOfFirst{!flag("evidence:$it")}.let{if(it<0)8 else it+1}
    fun enter(id:String){if(world==id)return;world=id;focus=0f;listen=0f;hidden="";past=false;sequence="";giantClock=0f;giantPass=false;giant=0f;chaseTime=0f;chaseState=if(flag("escaped"))4 else if(flag("alarm")&&flag("cylinder"))1 else 0;catchPending=false
        pursuerX=0f;pursuerZ=-70f;trackerX=-13f;trackerZ=-90f;trainEvent=0}
    fun goalTitle()=listOf("Elegir un propósito","Encontrar a Alma","Reconstruir mi recuerdo","Buscar una salida")[goal.coerceIn(0,3)]
    fun hint():String {
        if(!active)return "Una carta en el Umbral permite elegir qué buscas."
        if(completed)return "Capítulo terminado · "+value("ending")+" · Puedes seguir explorando."
        if(stage==8)return "Vuelve al reloj del Umbral. Ahora puedes decidir qué hacer con el archivo."
        if(evidenceWorlds.getOrNull(stage-1)==DreamArchive33.ID)return "El depósito de las voces · recupera el cilindro y responde a la archivista para abrir la salida."
        if(evidenceWorlds.getOrNull(stage-1)==DreamVoyage.REFUGE)return "El refugio · examina la taza junto a la puerta y decide cómo recibir a la visitante."
        return goalTitle()+" · "+DreamJourney.title(evidenceWorlds[stage-1])+" · Examina su testimonio."
    }
    private fun safe(id:String,x:Float,z:Float):DreamJourney.Point{
        if(DreamWorlds.canWalk(id,x,z))return DreamJourney.Point(x,z)
        for(r in 1..12)for(i in 0..15){val a=i*PI.toFloat()/8;val xx=x+cos(a)*r*.7f;val zz=z+sin(a)*r*.7f;if(DreamWorlds.canWalk(id,xx,zz))return DreamJourney.Point(xx,zz)}
        val p=DreamJourney.arrival(id,DreamWorlds.STATION);return DreamJourney.Point(p.x,p.z)
    }
    fun nodes(id:String):List<Node> { val base=baseNodes(id);return when{
        goal==3&&id==DreamWorlds.STATION&&flag("read:${DreamWorld30.LIBRARY}")->specialNodes33.getOrPut("exit"){base+listOf(Node("c33:goal-shortcut","El plano de salida abre el depósito",5f,-8f,"c33-door"))}
        goal==2&&past&&id==DreamVoyage.REFUGE->specialNodes33.getOrPut("memory"){base+listOf(Node("c33:memory-door","La puerta de una casa anterior",5f,-16f,"c33-door"))}
        else->base
    }}
    private fun baseNodes(id:String):List<Node> = cache.getOrPut(id){
        if(id==DreamArchive33.ID)return@getOrPut listOf(Node("c33:alarm","Leer el aviso del depósito",0f,-12f),Node("c33:hide:table","Refugiarme bajo la mesa",-9f,-30f,"c33-table"),Node("c33:hide:wardrobe","Entrar en el armario",9f,-48f,"c33-wardrobe"),Node("c33:echo","Recoger el cilindro de voces",0f,-88f,"c33-cylinder"),Node("c33:cooperate","Responder a la archivista",0f,-107f,"c33-clock"),Node("c33:escape","Abrir la puerta del archivo",0f,-120f,"c33-door"),Node("c33:cross:"+DreamWorld30.LIBRARY,"Volver a la biblioteca",6f,6f,"c33-door"))
        val a=if(id==DreamWorlds.HOUSE_MEADOW)DreamJourney.Arrival(0f,4f,0f)else DreamJourney.arrival(id,DreamWorlds.STATION);val first=safe(id,a.x+3.5f,a.z-2f)
        val target=when{id==DreamRainward34.ID->DreamJourney.Point(0f,-200f);DreamReferences.contains(id)->DreamReferences.exit(id);id in DreamJourney.sequence->DreamJourney.exit(id);DreamHorror32.contains(id)->DreamJourney.Point(0f,-DreamHorror32.end(id)+9f);id==DreamWorld30.LIBRARY->DreamJourney.Point(6f,-60f);id==DreamVoyage.REFUGE->DreamJourney.Point(5f,-11f);else->DreamJourney.Point(a.x-3f,a.z-15f)}
        val second=safe(id,target.x+2.8f,target.z+4f)
        val result=arrayListOf(Node("c33:examine",DreamStories33.get(id).title,first.x,first.z,if(DreamStories33.get(id).rule=="rhythm")"c33-clock" else "c33-letter"),Node("c33:resolve","Investigar la respuesta del lugar",second.x,second.z,"c33-reliquary"))
        if(id==DreamWorlds.STATION)result.add(Node("c33:chapter","La carta del Umbral",-4f,1f,"c33-clock"))
        if(id==DreamVoyage.REFUGE){result.add(Node("c33:visitor","Leer lo que dejó el visitante",3f,-16f,"c33-cup"));result.add(Node("c33:cooperate","Hablar con la archivista",-3f,-8f,"c33-clock"))}
        DreamCrossroads33.links(id).forEachIndexed{i,link->val p=safe(id,first.x+(if(i%2==0)3f else -3f),first.z-6f-i*5f);result.add(Node("c33:cross:${link.target}",link.label,p.x,p.z,"c33-door"))}
        result
    }
    fun nearest(id:String,x:Float,z:Float):Node? = nodes(id).filter{!it.key.startsWith("c33:hide:")||hidden.isBlank()}.minByOrNull{hypot(x-it.x,z-it.z)}?.takeIf{hypot(x-it.x,z-it.z)<it.range}
    fun chapterPanel()=DreamPlay30.Panel("La memoria del Umbral",goalTitle()+"\n\n"+hint()+"\n\nEl reloj no construyó estos lugares: guarda las decisiones de quienes los visitan.",if(!active)listOf(DreamPlay30.Choice("c33:goal:1","Buscar a Alma"),DreamPlay30.Choice("c33:goal:2","Reconstruir un recuerdo"),DreamPlay30.Choice("c33:goal:3","Encontrar una salida"))else listOf(DreamPlay30.Choice("c33:journal","Ver testimonios y próximos pasos"),DreamPlay30.Choice("c33:memory","Superponer el recuerdo"),DreamPlay30.Choice("c33:route","Seguir la pista del capítulo"),DreamPlay30.Choice("c33:rechoose","Reconsiderar mi propósito")))
    fun panel(key:String,id:String):DreamPlay30.Panel?{
        if(key=="c33:chapter")return chapterPanel()
        if(key=="c33:rechoose")return DreamPlay30.Panel("Tu propósito", "Cambiar de propósito conserva todas tus decisiones y descubrimientos.",listOf(DreamPlay30.Choice("c33:goal:1","Buscar a Alma"),DreamPlay30.Choice("c33:goal:2","Reconstruir un recuerdo"),DreamPlay30.Choice("c33:goal:3","Encontrar una salida")))
        if(key=="c33:journal")return DreamPlay30.Panel("El archivo que te recuerda",diary(),emptyList())
        if(key=="c33:examine"){val s=DreamStories33.get(id);return DreamPlay30.Panel(s.title,s.situation+"\n\n"+s.clue+"\n\n"+if(active)goalClue(id)else "La carta del Umbral te permite elegir un propósito.",listOf(DreamPlay30.Choice("c33:read","Registrar esta pista")))}
        if(key=="c33:resolve"){val s=DreamStories33.get(id);return DreamPlay30.Panel(s.title,s.request+"\n\n"+if(flag("resolved:$id"))"Recuerdas tu decisión: "+value("choice:$id")else "El lugar espera una respuesta.",listOf(DreamPlay30.Choice("c33:answer","Ayudar al lugar"),DreamPlay30.Choice("c33:keep","Guardar lo encontrado"),DreamPlay30.Choice("c33:memory","Comparar con el recuerdo")))}
        if(key=="c33:cooperate")return DreamPlay30.Panel("La archivista",if(flag("cooperation"))"Reconoce tu respuesta. El ritmo queda en el diario." else "Un golpe. Dos golpes. Silencio. Ella responde después de cada uno. Espera su respuesta antes de continuar.",listOf(DreamPlay30.Choice("c33:beat:1","Un golpe"),DreamPlay30.Choice("c33:beat:2","Dos golpes"),DreamPlay30.Choice("c33:beat:0","Guardar silencio")))
        if(key=="c33:visitor")return DreamPlay30.Panel("La taza junto a la puerta",visitorText(),listOf(DreamPlay30.Choice("c33:welcome","Dejar una taza para la visitante"),DreamPlay30.Choice("c33:close","Cerrar su cajón")))
        if(key=="c33:alarm")return DreamPlay30.Panel("Antes de abrir el depósito","El cilindro conserva una voz. Al retirarlo se despertará su custodio. La mesa del ala izquierda y el armario del ala derecha ofrecen resguardo. Hay dos corredores para rodearlo. La salida de entrada siempre permite regresar.",listOf(DreamPlay30.Choice("c33:prepared","He observado las rutas")))
        if(key=="c33:final"&&stage==8)return DreamPlay30.Panel("¿Quién construyó el Umbral?","Las firmas del plano son distintas versiones de la misma persona. El archivo guarda voces que alguien temía perder. Alma es la archivista: puedes devolverle su nombre, preservar lo que recuerdas o dejar libre el camino.",listOf(DreamPlay30.Choice("c33:end:alma","Devolver el nombre a Alma"),DreamPlay30.Choice("c33:end:memory","Conservar el recuerdo"),DreamPlay30.Choice("c33:end:exit","Abrir el camino hacia la mañana")))
        return null
    }
    private fun goalClue(id:String)=when(goal){1->"Alma dejó una señal en "+DreamJourney.title(id)+". Sus notas usan el mismo reloj.";2->"Este lugar conserva una parte de tu recuerdo. El dibujo cambia al comparar sus dos épocas.";else->"El plano marca una conexión desde este lugar. Las salidas no tienen por qué seguir el orden del recorrido."}
    private var beatWait=0f
    fun use(key:String,id:String,x:Float,z:Float,play:DreamPlay30):DreamPlay30.Effect {
        if(key=="c33:chapter"&&stage==8)return DreamPlay30.Effect(panel=panel("c33:final",id))
        panel(key,id)?.let{return DreamPlay30.Effect(panel=it)}
        when{
            key.startsWith("c33:goal:")->{val n=key.substringAfterLast(':').toIntOrNull()?:return DreamPlay30.Effect();if(n !in 1..3)return DreamPlay30.Effect();set("goal",n.toString());return DreamPlay30.Effect("Tu propósito queda en el diario: "+goalTitle(),"paper30",panel=chapterPanel())}
            key=="c33:read"->{mark("read:$id");if(id==DreamWorlds.STATION||id==DreamExpansion.CLOUD_HOTEL)mark("evidence:$id");return DreamPlay30.Effect(DreamStories33.get(id).clue+"\n"+hint(),"paper30")}
            key=="c33:answer"||key=="c33:keep"->{if(!flag("read:$id"))return DreamPlay30.Effect("Examina primero el testimonio junto a la llegada.")
                if(flag("resolved:$id"))return DreamPlay30.Effect("Tu decisión ya forma parte de este lugar. "+value("choice:$id"))
                val s=DreamStories33.get(id)
                if(key=="c33:answer"){
                    if(s.rule=="sound"&&!flag("listened:$id")&&play.value("record").isEmpty()){listen=2.5f;return DreamPlay30.Effect("Permanece junto al objeto y escucha su respuesta.","room30")}
                    if(s.rule=="memory"&&!flag("past:$id"))return DreamPlay30.Effect("Compara primero las dos épocas desde OBJETOS → Superponer el recuerdo.")
                    if(id==DreamWorld30.LIBRARY&&!flag("read:${DreamWorlds.POOLS}"))return DreamPlay30.Effect("El plano tiene una marca de agua. Una pista de las piscinas permite reconocerla.")
                    if(s.rule=="rhythm"&&!flag("observed:$id"))return DreamPlay30.Effect("Observa el reloj de la llegada durante tres segundos. Su lámpara marca el ritmo.")
                }
                mark("resolved:$id");set("choice:$id",if(key=="c33:answer")s.answer else s.keep);set("trust",(trust+if(key=="c33:answer")1 else -1).toString())
                if(key=="c33:answer")mark("restored:$id")
                if(id in evidenceWorlds&&id!=DreamVoyage.REFUGE)mark("evidence:$id")
                if(id==DreamWorlds.MEADOW){mark("lamp-returned",key=="c33:answer");mark("visitor-moved",key=="c33:answer")}
                return DreamPlay30.Effect(if(key=="c33:answer")s.answer else s.keep,"tone2",refresh=true)
            }
            key=="c33:memory"->{past=!past;if(past)mark("past:$id");revision++;return DreamPlay30.Effect(if(past)"El recuerdo se superpone: busca las marcas ámbar y la puerta que conserva su antigua posición." else "La habitación vuelve al presente.","projector30",refresh=true)}
            key.startsWith("c33:cross:")->{val target=key.removePrefix("c33:cross:");if(target==DreamArchive33.ID&&!flag("evidence:${DreamWorld30.LIBRARY}"))return DreamPlay30.Effect("El plano de la biblioteca permite reconocer esta puerta. Su marca de agua viene de las piscinas.");if(DreamCrossroads33.links(id).none{it.target==target})return DreamPlay30.Effect();mark("cross:$id");return DreamPlay30.Effect("La puerta reconoce una habitación de otro mundo.","wood",target)}
            key=="c33:goal-shortcut"&&goal==3&&id==DreamWorlds.STATION&&flag("read:${DreamWorld30.LIBRARY}")->return DreamPlay30.Effect("Tu plano reconoce el acceso de servicio.",target=DreamArchive33.ID)
            key=="c33:memory-door"&&goal==2&&past&&id==DreamVoyage.REFUGE->return DreamPlay30.Effect("Reconoces la casa antes de que cerrara su puerta.",target=DreamVoyage.HOUSE)
            key=="c33:route"->{val target=if(stage in 1..7)evidenceWorlds[stage-1]else DreamWorlds.STATION;return DreamPlay30.Effect("Sigues la dirección anotada en el archivo.",target=target)}
            key=="c33:welcome"||key=="c33:close"->{if(flag("visitor-seen"))return DreamPlay30.Effect(visitorText(),"wood");mark("visitor-welcome",key=="c33:welcome");mark("visitor-seen");mark("evidence:${DreamVoyage.REFUGE}");set("trust",(trust+if(key=="c33:welcome")2 else -1).toString());return DreamPlay30.Effect(visitorText(),"wood",refresh=true)}
            key.startsWith("c33:beat:")->{if(beatWait>0f)return DreamPlay30.Effect("Su mano sigue levantada. Espera su respuesta.");val n=key.substringAfterLast(':');if(n !in listOf("0","1","2"))return DreamPlay30.Effect();sequence=(sequence+n).takeLast(3);beatWait=1.4f;echoAim=n.toFloat();
                if(sequence=="120"){mark("cooperation");return DreamPlay30.Effect("La archivista completa el ritmo. La cerradura del depósito reconoce ambas respuestas.","tone3",refresh=true)}
                return DreamPlay30.Effect(if(sequence=="1"||sequence=="12")"Ella repite el golpe y espera tu siguiente respuesta." else "Inclina la cabeza. Vuelve a empezar: uno, dos, silencio.",if(n=="0")"room30" else "piano$n")}
            key=="c33:prepared"->{mark("prepared");return DreamPlay30.Effect("Reconoces los dos escondites y el camino de regreso.")}
            key=="c33:echo"->{if(!flag("prepared"))return DreamPlay30.Effect("Lee primero el aviso de la entrada: explica los refugios y los corredores.");mark("cylinder");if(!flag("escaped")){chaseState=1;chaseTime=0f;mark("alarm")};return DreamPlay30.Effect("La voz de Alma despierta en el cilindro. El custodio responde desde la otra sala. Tienes tiempo para apartarte.","train30",refresh=true)}
            key.startsWith("c33:hide:")->{hidden=key.substringAfterLast(':');mark("hide:$hidden");return DreamPlay30.Effect(if(hidden=="table")"Te agachas bajo la mesa. Los pasos atraviesan la madera." else "Cierras el armario y observas por la rendija.","wood")}
            key=="c33:unhide"->{hidden="";return DreamPlay30.Effect("Sales del escondite.")}
            key=="c33:escape"->{if(!flag("cylinder"))return DreamPlay30.Effect("La puerta espera el cilindro de voces del depósito.");if(!flag("cooperation"))return DreamPlay30.Effect("La cerradura tiene dos manos. La archivista conoce la segunda respuesta.");mark("escaped");mark("evidence:${DreamArchive33.ID}");chaseState=4;return DreamPlay30.Effect("Abres el archivo y vuelves al refugio. El custodio deja de seguirte.","tone3",DreamVoyage.REFUGE)}
            key.startsWith("c33:end:")->{if(stage!=8)return DreamPlay30.Effect("Todavía faltan testimonios: "+hint());val choice=key.substringAfterLast(':');if(choice !in listOf("alma","memory","exit"))return DreamPlay30.Effect();val ending=when{choice=="alma"&&goal==1&&trust>=1&&flag("cooperation")->"ALMA RECUERDA";choice=="exit"&&goal==3&&story.flags.count{it.startsWith("v33:resolved:")&&it.endsWith("=1")}>=5->"LA MAÑANA COMPARTIDA";choice=="memory"&&goal==2&&flag("past:${DreamVoyage.REFUGE}")->"LA CASA QUE ELEGISTE";else->"EL ARCHIVO ABIERTO"};set("ending",ending);story.endings.add("v33:"+ending);return DreamPlay30.Effect(ending+" · "+when(ending){"ALMA RECUERDA"->"La visitante reconoce su nombre y deja una última taza.";"LA MAÑANA COMPARTIDA"->"Las puertas se abren hacia una mañana que también recuerda a los demás.";"LA CASA QUE ELEGISTE"->"Aceptas el recuerdo y encuentras un lugar para él.";else->"El archivo permanece abierto. Puedes volver a completar otras historias."},"tone3",refresh=true)}
        }
        return DreamPlay30.Effect()
    }
    fun visitorText():String=when{
        !flag("visitor-seen")->"Una taza húmeda y una llave con forma de reloj. La nota dice: «No cierres la puerta que dejé abierta»."
        !flag("visitor-welcome")->"El cajón quedó cerrado. La taza aparece al otro lado de la habitación; la visitante espera fuera. Recuerda tu primera respuesta."
        flag("escaped")->"Alma te reconoce por la voz que devolviste al depósito. Ahora espera en el refugio y deja dos tazas junto a la puerta."
        flag("visitor-moved")->"La archivista dejó su broche junto a tu taza. Ahora espera cerca de la biblioteca. Reconoce que devolviste la lámpara."
        (story.visits[DreamVoyage.REFUGE]?:0)>=3->"En tu tercera visita, la archivista ya reconoce tus pasos. Dejó un pequeño reloj junto a la taza; espera en el refugio."
        else->"La visitante acepta la taza y deja su broche con forma de reloj. Espera en el refugio, sin bloquear tu camino."
    }
    fun diary():String=goalTitle()+"\n"+hint()+"\n\n"+story.flags.filter{it.startsWith("v33:read:")&&it.endsWith("=1")}.map{it.substringAfter("v33:read:").substringBefore('=')}.sorted().joinToString("\n\n"){id->DreamStories33.get(id).title+"\n"+DreamStories33.get(id).clue+(if(flag("resolved:$id"))"\n"+value("choice:$id")else "")}
    fun trainAllowed(target:String):Boolean{val rival=value("train-rival");return rival!=target||clock-number("train-departed")>128f}
    fun board(target:String){val rival=if(target==DreamWorld30.LIBRARY)DreamHorizons.CITY else if(target==DreamHorizons.CITY)DreamWorld30.LIBRARY else "";set("train-rival",rival);set("train-departed",clock.toInt().toString());set("train-last",target);mark("train-journey")}
    fun trace(id:String,x:Float,z:Float){if(id!=DreamNewDreams.EYES)soundPulse=x to z}
    fun tick(id:String,x:Float,y:Float,z:Float,yaw:Float,pitch:Float,dt:Float,play:DreamPlay30):Event?{
        enter(id);val d=dt.coerceIn(0f,.1f);clock+=d;echoAim=(echoAim-d*1.2f).coerceAtLeast(0f);if(clock.toInt()-storedClock>=5){storedClock=clock.toInt();set("clock",storedClock.toString())};beatWait=(beatWait-d).coerceAtLeast(0f)
        if(hidden.isNotEmpty())return chaseTick(id,x,z,d,true)
        if(listen>0f){val n=nodes(id).firstOrNull{it.key=="c33:resolve"};if(n!=null&&hypot(x-n.x,z-n.z)<4f){listen-=d;if(listen<=0f){mark("listened:$id");return Event("Escuchas un nombre que no aparece en la carta.","tone1",n.x,y+2f,n.z)}}}
        val node=nodes(id).firstOrNull{it.key=="c33:examine"}
        if(node!=null&&!flag("observed:$id")){val dx=node.x-x;val dz=node.z-z;val dist=hypot(dx,dz);val looking=(sin(yaw)*dx-cos(yaw)*dz)/dist.coerceAtLeast(.1f)>.85f&&abs(pitch)<.65f;focus=if(dist<9f&&looking)focus+d else 0f;if(focus>3f){mark("observed:$id");return Event("La luz marca un golpe, dos golpes y una pausa. El diario conserva el ritmo.","tone1",node.x,y+2f,node.z)}}
        if(id==DreamPocket.TRAIN){val phase=play.trainProgress;val event=if(phase>.72f)3 else if(phase>.38f)2 else if(phase>.12f)1 else 0;if(event>trainEvent){trainEvent=event;revision++;return Event(when(event){1->"El tren entra en un túnel. Una puerta del vagón se ha cerrado.";2->"Hay una pasajera en el asiento que estaba vacío.";else->"El cartel muestra una estación orientada hacia el cielo."},"train30",0f,2f,-25f)}}
        if(id==DreamWorlds.AQUARIUM||id==DreamVigilia27.SKY_ISLAND||id==DreamHorror32.POOL){giantClock+=d;giant=if(giantClock in 12f..38f)sin((giantClock-12f)/26f*PI.toFloat()).coerceAtLeast(0f)else 0f;if(giant>.15f&&!giantPass){giantPass=true;return Event("Una sombra inmensa desplaza las nubes. Los cristales vibran durante su paso.","whale",40f,45f,-130f)}}else giant=0f
        if(id==DreamWorlds.MEADOW||id==DreamReferences.VILLAGE){lastTrailCheck+=d;if(lastTrailCheck>1f){lastTrailCheck=0f;val traces=play.marks().filter{it.world==id};sampledTrace=traces.lastOrNull();val target=soundPulse?:sampledTrace?.let{it.x to it.z};if(target!=null&&hypot(x-trackerX,z-trackerZ)>12f){val dx=target.first-trackerX;val dz=target.second-trackerZ;val len=hypot(dx,dz);val step=min(1.4f,len);val nx=trackerX+dx/len.coerceAtLeast(.1f)*step;val nz=trackerZ+dz/len.coerceAtLeast(.1f)*step;if(DreamWorlds.canWalk(id,nx,nz)){trackerX=nx;trackerZ=nz}}}}
        return chaseTick(id,x,z,d,false)
    }
    private fun chaseTick(id:String,x:Float,z:Float,dt:Float,hiding:Boolean):Event?{
        if(id!=DreamArchive33.ID||chaseState==0||chaseState==4)return null
        chaseTime+=dt
        if(chaseState==1&&chaseTime>10f){chaseState=2;return Event("El custodio entra en los corredores. Escucha sus pasos antes de salir.","wood",pursuerX,1.5f,pursuerZ)}
        if(chaseState>=2){val dist=hypot(x-pursuerX,z-pursuerZ);val visible=!hiding&&DreamArchive33.lineClear(pursuerX,pursuerZ,x,z);chaseState=if(visible)3 else 2
            val target=if(visible)x to z else if((chaseTime.toInt()/8)%2==0)-12f to -40f else 12f to -72f
            val p=DreamArchive33.advance(pursuerX,pursuerZ,target.first,target.second,dt*(if(visible)3.0f else 1.8f));pursuerX=p.first;pursuerZ=p.second
            if(visible&&dist<1.35f){catchPending=true;chaseState=1;chaseTime=0f;pursuerX=0f;pursuerZ=-70f;return Event("La voz te devuelve al vestíbulo. Conservas tus objetos; vuelve a observar las rutas.","room30")}
        }
        if(chaseState>=2){footClock+=dt;if(footClock>.68f){footClock=0f;return Event(sound="wood",sourceX=pursuerX,sourceY=1.2f,sourceZ=pursuerZ)}}
        return null
    }
    fun consumeCatch():Boolean{val out=catchPending;catchPending=false;return out}
    fun furnitureClear(id:String,x:Float,z:Float,feet:Float):Boolean{
        if(feet>1.4f)return true
        for(n in nodes(id)){
            if(n.model=="c33-door"||n.key=="c33:escape")continue
            val sx=when(n.model){"c33-table"->1.9f;"c33-wardrobe"->1.55f;else->.61f};val sz=when(n.model){"c33-table"->1.40f;"c33-wardrobe"->.92f;else->.61f}
            if(abs(x-n.x)<sx&&abs(z-n.z)<sz)return false
        }
        return true
    }
    fun extraPath(id:String,x:Float,z:Float)=!(id==DreamPocket.TRAIN&&trainEvent==1&&abs(z+36f)<.8f&&abs(x)<3.8f)
}
