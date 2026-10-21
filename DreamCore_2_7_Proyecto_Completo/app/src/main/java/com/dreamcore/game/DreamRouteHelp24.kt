package com.dreamcore.game

/** Optional, explicit directions, available only when the player asks for a hint. */
object DreamRouteHelp24 {
    fun hint(world:String)=when(world){
        DreamVigilia27.DARK_THREE->"La carretera central cruza el pueblo y llega al cuarto mundo. Las lunas te siguen: mira una sin apartarte para descubrir sus cambios. El portal a tu espalda vuelve a la catedral."
        DreamVigilia27.DARK_FOUR->"Sigue el camino entre las ruinas y los estanques; el último portal regresa al Umbral. Puedes explorar los laterales sin abandonar la ruta central."
        DreamVigilia27.SKY_ISLAND->"Explora la casa y sus jardines. Desde un costado de la casa, mira alto hacia las nubes: el observador blanco permanece a distancia. El portal junto a la llegada vuelve al lugar anterior."
        DreamRoutes23.SKY->"Desde la entrada, busca el círculo de luz de la derecha, a media distancia. Los otros dos son paisaje. Entra en el centro, suelta el joystick y mira hacia arriba durante 6 segundos. Cuando responda la ventana, camina un poco más adelante y pulsa USAR junto al marco. Lleva al castillo de nubes."
        DreamRoutes23.CLOUD->"El corredor principal lleva a un puente y al castillo. También puedes explorar la habitación lateral de la derecha y usar su marco sin cristal para acortar la distancia. En el fondo del castillo, USAR en el ascensor lleva a la ciudad de las flores."
        DreamRoutes23.CITY->"La parada está a la derecha y al fondo del campo. Busca el banco junto al tren. Cuando el tren se detenga, suelta el joystick y espera 6 segundos junto al banco. Después acércate al punto de embarque, unos pasos más allá, y pulsa USAR. Si se marcha, vuelve cada minuto. Lleva a la pradera de las grietas."
        DreamRoutes23.EDGE->"Lee con USAR el cartel sonriente, al fondo del sendero de baldosas. Los dos puentes permiten cruzar la grieta. Después busca la casa rosa al otro lado y usa su puerta para volver a las aberturas del cielo."
        DreamBackrooms.ID->"Las luces que permanecen estables señalan puertas que acercan a la salida. Las inestables pueden llevarte a un bucle. También puedes escuchar el tono lejano que señala la siguiente puerta. Hay retorno junto a la entrada y en la salida."
        else->"Las puertas muestran el nombre de su destino al acercarte. Busca objetos que respondan a USAR, consulta el diario y escucha los sonidos lejanos. Si quieres cambiar de recorrido, abre Ajustes y elige Rutas."
    }
}
