package com.dreamcore.game

import kotlin.math.*

/** A persistent investigation with three independent solutions, not a sequence of mandatory clues. */
object DreamMystery {
    const val HOUSE="pocket_call_house"
    private var locked=false
    fun sync(s:DreamChronicle){locked=!unlocked(s)}
    fun blocked(x:Float,z:Float)=locked&&abs(x-70f)<2.2f&&abs(z+61.5f)<.8f
    private var layoutSeed=7319
    fun configure(seed:Int){if(seed!=layoutSeed){layoutSeed=seed;DreamObjects.invalidate(DreamWorlds.MEADOW)}}
    fun circuit()=when(layoutSeed%3){0->DreamJourney.Point(-12f,-40f);1->DreamJourney.Point(18f,-105f);else->DreamJourney.Point(-18f,-135f)}
    private const val PREFIX="investigation:"
    val notes=linkedMapOf(
        "call" to "La llamada: alguien contesta desde una casa cuyo teléfono está desconectado.",
        "wire" to "El cable: el timbre responde cuando cambia la luz del jardín.",
        "photo" to "La fotografía: el cristal muestra una entrada que no está en la fachada.",
        "listen" to "La parada: el cartero tararea tres notas y espera que alguien le responda.",
        "power" to "La corriente: la casa se iluminó. Otra ventana también se encendió.",
        "hush" to "El silencio: la puerta se abrió al detener el timbre. La figura se alejó.",
        "tune" to "La melodía: contesté al cartero y me mostró el acceso a la casa.",
        "visitor" to "La visita: alguien volvió a la ventana iluminada. Reconoce que fui yo.",
        "answer" to "El destinatario: la llamada iba dirigida a alguien que todavía no había llegado.",
        "leave" to "Dejé el auricular colgado. La casa quedó tranquila al volver.",
        "stay" to "Dejé el auricular descolgado. Al volver, la mesa tenía un sitio más.",
        "backrooms" to "Las luces: en las salas amarillas las lámparas estables forman un recorrido.",
        "city" to "La ciudad: el tren vuelve a su parada; esperar cambia lo que puedo descubrir.",
        "sky" to "La abertura: el suelo iluminado responde a la sombra de las nubes.",
        "cloud" to "Las ventanas: el castillo aparece al mirar por un marco sin cristal.",
        "edge" to "El borde: la señal sonríe hacia un camino seguro entre las grietas."
    )
    fun has(s:DreamChronicle,key:String)=PREFIX+key in s.flags
    fun record(s:DreamChronicle,key:String):Boolean {require(key in notes);s.found.add("clue:"+key);return s.flags.add(PREFIX+key)}
    private fun encounters32(s:DreamChronicle):String = s.flags.filter{it.startsWith("v32:witness:")}.sorted().joinToString("\n\n",postfix=if(s.flags.any{it.startsWith("v32:witness:")})"\n\n" else ""){"ENCUENTRO · "+DreamJourney.title(it.removePrefix("v32:witness:"))+"\nLa presencia orientó su mirada hacia mí. No sé qué espera."}
    fun diary(s:DreamChronicle):String=encounters32(s)+notes.filterKeys{has(s,it)||"clue:"+it in s.found}.values.joinToString("\n\n").ifBlank{"No has anotado pistas todavía. Puedes investigar sin seguir un orden. Mira, escucha y prueba los objetos."}
    fun variant(s:DreamChronicle)=s.universeSeed%3
    fun unlocked(s:DreamChronicle)=has(s,"power")||has(s,"photo")||has(s,"tune")||has(s,"hush")
    fun hint(s:DreamChronicle)=when{
        has(s,"answer")->"El pueblo recuerda tu decisión. Puedes volver a explorar o seguir el viaje."
        unlocked(s)->"La casa del timbre tiene una entrada. Decide cuándo visitarla."
        has(s,"call")->"Puedes investigar el cable, fotografiar el cristal o escuchar en la parada."
        else->"Un teléfono suena cerca del camino. También puedes explorar libremente."
    }
    fun items(id:String):List<DreamObjects.Item> = when(id){
        DreamWorlds.MEADOW->listOf(
            DreamObjects.Item("mystery:call",8f,65f,"investigation"),
            DreamObjects.Item("mystery:wire",-10f,15f,"investigation"),
            DreamObjects.Item("mystery:power",circuit().x,circuit().z,"investigation"),
            DreamObjects.Item("mystery:hush",circuit().x+4f,circuit().z,"investigation"),
            DreamObjects.Item("mystery:listen",-24f,-173f,"investigation"),
            DreamObjects.Item("mystery:tune",-20f,-173f,"investigation"),
            DreamObjects.Item("mystery:visit",70f,-59f,"investigation"),
            DreamObjects.Item("mystery:friend",-26f,-176f,"investigation"))
        HOUSE->listOf(DreamObjects.Item("mystery:answer",0f,-14f,"investigation"),DreamObjects.Item("mystery:leave",-3f,-14f,"investigation"),DreamObjects.Item("mystery:stay",3f,-14f,"investigation"))
        else->emptyList()
    }
    fun label(key:String)=when(key.substringAfter(':')){
        "call"->"Contestar el teléfono del camino";"wire"->"Examinar el cable";"power"->"Dar corriente a la casa";"hush"->"Desconectar el timbre"
        "listen"->"Escuchar al cartero";"tune"->"Responder con tres notas";"visit"->"Llamar a la casa del timbre";"friend"->"Hablar con el cartero"
        "leave"->"Colgar y dejar descansar la casa";"stay"->"Dejar la línea abierta";else->"Escuchar el teléfono desconectado"
    }
    fun use(key:String,s:DreamChronicle):DreamObjects.Result {
        val action=key.substringAfter(':')
        fun out(text:String,sound:String="chime")=DreamObjects.Result(text,sound)
        return when(action){
            "call"->{record(s,"call");out("La voz dice: alguien acaba de contestar desde la casa al otro lado del jardín. Puedes seguir el cable, mirar su ventana con la cámara o preguntar al cartero.","tone1")}
            "wire"->{record(s,"wire");out("El cable termina en una caja junto al jardín. Hay dos circuitos: alimentar la casa o silenciar su timbre. También parece haber otra entrada en el cristal.")}
            "power"->{record(s,"wire");record(s,"power");s.flags.remove(PREFIX+"hush");s.flags.add("lamp:meadow");out("La casa se ilumina y su cerradura cede. Una figura se detiene en otra ventana. Has llamado su atención.","tone2")}
            "hush"->{record(s,"wire");record(s,"hush");s.flags.remove(PREFIX+"power");s.flags.remove("lamp:meadow");out("El timbre se apaga y la puerta se abre. La figura se retira, pero el jardín pierde su luz. La cámara sigue funcionando.","tone0")}
            "listen"->{record(s,"listen");out(when(variant(s)){0->"El cartero tararea: una nota baja, una alta y otra baja. Dice que el cristal recuerda a quien lo fotografía.";1->"El cartero tararea tres notas. Dice que una casa iluminada también se puede ver desde fuera del sueño.";else->"El cartero tararea despacio. Dice que algunas puertas se abren cuando dejan de llamarte."},"tone2")}
            "tune"->if(has(s,"listen")){record(s,"tune");out("Reconoce la melodía y te entrega una llave. Recuerda que lo escuchaste. La casa del timbre ya puede visitarse.","tone3")}else out("El cartero espera. Podrías escuchar antes de responder.")
            "friend"->{val met=s.flags.add("person:traveller:spoken");out(if(met)"El cartero pregunta si buscas una casa o a quien la recuerda. Permanece junto a la parada." else if(has(s,"stay"))"El cartero te reconoce: dejaste la línea abierta. Alguien sigue esperando que regreses." else if(has(s,"leave"))"El cartero te reconoce: gracias por dejarla descansar. Esta vez parece menos inquieto." else if(has(s,"tune"))"Recuerda tus tres notas. Ya te dio la llave; no necesita que repitas el recorrido." else "El cartero recuerda haberte visto. Escucha primero; ninguna casa exige una única entrada.")}
            "visit"->if(unlocked(s)){s.flags.add("investigation:entered");DreamObjects.Result("El timbre suena desde dentro, aunque has dejado el teléfono fuera.",pocket=HOUSE)}else{record(s,"call");out("La puerta principal está cerrada. El cable, el reflejo y el cartero ofrecen maneras distintas de entrar.")}
            "answer"->{record(s,"answer");out("El auricular está desconectado. Oyes tu llegada al pueblo antes de haber ocurrido. Puedes colgar para dejar descansar la casa o mantener la línea abierta.","tone1")}
            "leave","stay"->if(has(s,"answer")){s.flags.remove(PREFIX+if(action=="leave")"stay" else "leave");record(s,action);out(if(action=="leave")"Cuelgas. Las ventanas del pueblo recuperan su luz tranquila." else "Dejas la línea abierta. Al regresar al pueblo, alguien reconocerá tu llamada.","tone0")}else out("El auricular espera. Escucha lo que dice antes de decidir.")
            else->out("El pueblo guarda silencio.")
        }
    }
    fun photoCandidate(id:String,x:Float,y:Float,z:Float,yaw:Float,pitch:Float,clear:(Float,Float,Float,Float)->Boolean):Boolean{
        if(id!=DreamWorlds.MEADOW)return false
        val tx=70f;val ty=DreamJourney.surface(id,tx,-60f)+3f;val tz=-60f
        val dx=tx-x;val dy=ty-y;val dz=tz-z;val d=sqrt(dx*dx+dy*dy+dz*dz)
        return d in 3f..70f && (sin(yaw)*cos(pitch)*dx+sin(pitch)*dy-cos(yaw)*cos(pitch)*dz)/d>.96f && clear(dx/d,dy/d,dz/d,d-.6f)
    }
    fun visual(s:DreamChronicle)=listOf("power","hush","photo","stay","leave").map{has(s,it)}
    fun observer(s:DreamChronicle)=has(s,"power")&&!has(s,"leave")
    fun geometry(id:String,s:DreamChronicle):DreamWorlds.Scene {
        val b=DreamMesh();val cream=DreamShapes.ivory;val rose=DreamShapes.rose
        if(id==DreamWorlds.MEADOW){
            val pts=listOf(8f to 63.5f,8f to 15f,-10f to 15f,circuit().x to circuit().z-1.5f,65f to -60.5f)
            DreamShapes.tube(b,pts.map{floatArrayOf(it.first,DreamJourney.surface(id,it.first,it.second)+.06f,it.second)},.045f,floatArrayOf(.15f,.13f,.12f),40f,6)
            val y=DreamJourney.surface(id,70f,-60f)
            if(!unlocked(s))b.box(70f,y+1.6f,-61.3f,1.5f,1.6f,.08f,rose,43f)
            b.box(70f,y+3f,-60.2f,1.4f,1f,.06f,if(unlocked(s))floatArrayOf(.58f,.77f,.72f)else floatArrayOf(.24f,.32f,.36f),if(observer(s))24f else 34f)
            if(has(s,"stay")){DreamSculpt.chair(b,5f,DreamJourney.surface(id,5f,59f),59f);DreamShapes.cup(b,5f,DreamJourney.surface(id,5f,59f)+.8f,59f)}
        }
        if(id==HOUSE){
            b.box(0f,-.15f,-8f,9f,.15f,19f,cream,43f);b.box(0f,6f,-8f,9f,.18f,19f,cream,0f)
            for(x in floatArrayOf(-9f,9f))b.box(x,3f,-8f,.18f,3f,19f,cream,0f)
            for(z in floatArrayOf(-27f,11f))b.box(0f,3f,z,9f,3f,.18f,cream,0f)
            DreamSculpt.rounded(b,0f,1f,-15.5f,4f,.15f,1.1f,.1f,rose,43f)
            DreamShapes.chair(b,0f,0f,-19f);DreamShapes.cup(b,-2f,1.2f,-15.5f)
            b.box(0f,3.5f,-26.7f,4f,2f,.03f,floatArrayOf(.36f,.48f,.54f),34f)
            if(has(s,"stay")){DreamShapes.chair(b,3f,0f,-19f);DreamShapes.cup(b,2f,1.2f,-15.5f)}
            b.box(0f,1.6f,8f,1.4f,1.6f,.03f,rose,29f)
        }
        return b.finish()
    }
}
