package com.dreamcore.game

import kotlin.math.*

/** Local clocks only advance while playing; discoveries and choices live in the save. */
class DreamRules23 {
    private var world="";private var time=0f;private var waiting=0f;private var ring=3f
    val trainOffset:Float get(){val t=time%60f;return when{t<10f->-80f*(1f-t/10f);t<30f->0f;t<50f->-(t-30f)*5f;else->-100f}}
    fun step(id:String,x:Float,z:Float,pitch:Float,moving:Boolean,dt:Float,s:DreamChronicle):String?{
        if(world!=id){world=id;time=0f;waiting=0f;ring=3f};time+=dt
        ring-=dt
        val field=id==DreamWorlds.MEADOW&&!DreamMystery.has(s,"call")&&!DreamMystery.has(s,"answer")&&hypot(x-8f,z-65f)<65f
        val house=id==DreamMystery.HOUSE&&!DreamMystery.has(s,"answer")&&hypot(x,z+14f)<30f
        if((field||house)&&ring<=0f){ring=11f;return if(house)"@ring:house" else "@ring:field"}
        val sky=id==DreamRoutes23.SKY&&hypot(x-50f,z+145f)<10f&&pitch>.2f&&!moving
        val city=id==DreamRoutes23.CITY&&hypot(x-24f,z+230f)<10f&&abs(trainOffset)<.1f&&!moving
        waiting=if(sky||city)waiting+dt else 0f
        if(sky&&waiting>=6f&&DreamMystery.record(s,"sky"))return "Al detenerte bajo la abertura, la luz dibuja una ventana en el campo. La puerta cercana responde."
        if(city&&waiting>=6f&&DreamMystery.record(s,"city"))return "El tren se detiene junto a ti. Una puerta permanece abierta; también puedes dejarlo marchar y seguir explorando."
        return null
    }
    companion object {
        fun items(id:String)=when(id){
            DreamBackrooms.ID->listOf(DreamObjects.Item("rule:lamps",0f,0f,"rule"),DreamObjects.Item("rule:breaker",0f,-36f,"rule"),DreamObjects.Item("rule:found",36f,-69f,"rule"))
            DreamRoutes23.SKY->listOf(DreamObjects.Item("rule:sky",0f,-25f,"rule"),DreamObjects.Item("rule:cloud",50f,-165f,"rule"))
            DreamRoutes23.CLOUD->listOf(DreamObjects.Item("rule:window",36f,-50f,"rule"),DreamObjects.Item("rule:city",0f,-237f,"rule"))
            DreamRoutes23.CITY->listOf(DreamObjects.Item("rule:train",24f,-226f,"rule"),DreamObjects.Item("rule:board",24f,-236f,"rule"))
            DreamRoutes23.EDGE->listOf(DreamObjects.Item("rule:sign",24f,-186f,"rule"),DreamObjects.Item("rule:field",-35f,-146f,"rule"))
            else->emptyList()
        }
        fun label(key:String)=when(key.substringAfter(':')){"lamps"->"Leer la hoja junto a la lámpara";"breaker"->"Restablecer la corriente del sector";"found"->"Examinar la salida";"sky"->"Leer las marcas del campo";"cloud"->"Tocar la ventana de luz";"window"->"Atravesar el marco sin cristal";"city"->"Tomar el ascensor del castillo";"train"->"Leer el horario de la parada";"board"->"Subir al tren que espera";"sign"->"Observar la sonrisa del cartel";else->"Abrir la puerta de la casa rosa"}
        fun use(key:String,s:DreamChronicle):DreamObjects.Result=when(key.substringAfter(':')){
            "lamps"->{DreamMystery.record(s,"backrooms");DreamObjects.Result("Las luces inestables dibujan círculos. Las que permanecen encendidas se acercan a la salida. El zumbido también puede orientarte.")}
            "breaker"->{val on=s.toggle("rule:breaker");DreamObjects.Result(if(on)"Las lámparas se estabilizan un poco. Algo que estaba escuchando reconoce tu paso." else "Desconectas el sector. La presencia se retira; las lámparas que guían siguen encendidas.")}
            "found"->{DreamMystery.record(s,"backrooms");s.flags.add("discovery:backrooms_exit");DreamObjects.Result("La hoja lleva tu letra. Encontraste un recorrido estable. La puerta cercana vuelve al lugar desde el que entraste.")}
            "sky"->DreamObjects.Result("En el suelo se lee: la abertura responde a quien deja de caminar. Busca el círculo claro, detente y observa el cielo.")
            "cloud"->if(DreamMystery.has(s,"sky"))DreamObjects.Result("El marco muestra un corredor entre nubes.",pocket=DreamRoutes23.CLOUD)else DreamObjects.Result("El cristal todavía refleja el campo. Quizá la luz del círculo cambie algo cuando te detengas a observar.")
            "window"->{DreamMystery.record(s,"cloud");DreamObjects.Result("Tras el marco, el castillo que veías a lo lejos está a pocos pasos.",warp=DreamJourney.Point(0f,-190f))}
            "city"->{DreamMystery.record(s,"cloud");DreamObjects.Result("El ascensor se abre en un campo de flores rodeado de torres.",pocket=DreamRoutes23.CITY)}
            "train"->DreamObjects.Result("El horario dice: cada minuto, la parada espera a quien también espera. Puedes detenerte aquí o explorar los edificios y regresar después.")
            "board"->if(DreamMystery.has(s,"city"))DreamObjects.Result("El pasajero reconoce que esperaste. El tren cruza hacia una pradera abierta en grietas.",pocket=DreamRoutes23.EDGE)else DreamObjects.Result("El tren pasará por la parada. Detente junto al banco cuando llegue; todavía puedes recorrer la ciudad.")
            "sign"->{DreamMystery.record(s,"edge");DreamObjects.Result("La sonrisa apunta a los puentes. Desde ambos lados puedes llegar a la casa: el camino no exige una sola dirección.")}
            else->if(DreamMystery.has(s,"edge"))DreamObjects.Result("La casa pequeña contiene un campo bajo un cielo abierto. El recorrido vuelve a encontrarse.",pocket=DreamRoutes23.SKY)else DreamObjects.Result("La casa espera. El cartel junto al borde podría explicar los caminos.")
        }
        fun geometry(id:String):DreamWorlds.Scene {val b=DreamMesh();for(i in items(id)){val y=DreamJourney.surface(id,i.x,i.z);if(i.key in listOf("rule:cloud","rule:city","rule:field")){
            b.box(i.x,y+1.6f,i.z-1.5f,1.2f,1.6f,.04f,DreamShapes.rose,34f);for(s in floatArrayOf(-1f,1f))b.box(i.x+s*1.4f,y+1.7f,i.z-1.5f,.12f,1.7f,.12f,DreamShapes.ivory,26f)
        }else{DreamSculpt.cylinder(b,i.x,y,i.z-1.5f,.06f,1.3f,DreamSculpt.wood,43f,7);DreamSculpt.rounded(b,i.x,y+1.5f,i.z-1.5f,.55f,.3f,.05f,.035f,DreamShapes.ivory,0f)}};return b.finish()}
    }
}
