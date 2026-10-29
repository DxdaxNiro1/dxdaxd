package com.dreamcore.game

/** Optional, explicit directions, available only when the player asks for a hint. */
object DreamRouteHelp24 {
    fun hint(world:String)=when(world){
        DreamRainward34.ID->"La calle mojada conduce al corredor rojo. El paso central continúa hasta la puerta del fondo; las islas laterales son paisaje. No necesitas saltar al vacío. El marco de la entrada permite volver en cualquier momento."
        DreamHorror32.ATRIUM->"Sigue el suelo continuo entre las puertas torcidas. El coro queda a los lados. La puerta del fondo continúa hacia el jardín; la de atrás vuelve al lugar anterior."
        DreamHorror32.GARDEN->"El sendero pasa junto al gran observatorio. El ojo está sobre la torre. Busca el marco del fondo para llegar a la avenida nocturna."
        DreamHorror32.WITNESSES->"Los faroles marcan la avenida. Hay dos presencias, no una multitud. El marco del fondo sigue hacia el bosque; puedes volver por la entrada."
        DreamHorror32.FOREST->"El camino de tierra bajo el eclipse es transitable de principio a fin. Las figuras de los lados observan. La puerta del fondo conduce al campo de pantallas."
        DreamHorror32.HERBARIUM->"La franja central lleva al siguiente marco. Puedes explorar el vagón cubierto de vegetación y los relojes del lateral sin bloquear la salida."
        DreamHorror32.HOUSE->"El puente estrecho llega a la isla suspendida. Hay barandas y suelo continuo: no hace falta saltar al vacío. Bordea la casa para encontrar la salida detrás de ella."
        DreamHorror32.POOL->"Las escaleras del centro permiten bajar a la piscina. Los andadores laterales llevan al último marco, que vuelve al Umbral."

        DreamWorld30.LIBRARY->"El piano, los libros y el proyector ocupan las salas laterales. La maqueta cambia una puerta de la feria. La baldosa circular al fondo permite alinear los marcos. El andén está junto a la entrada."
        DreamWorld30.FAIR->"La puerta repite el movimiento de su maqueta en la biblioteca. Las válvulas vacían el depósito y descubren escaleras. El telescopio está después de la puerta. El carrusel se mueve cuando miras hacia otro lado."
        DreamWorld30.SECRET->"Busca el reloj al fondo y mira el cielo con el telescopio. Dormir en el refugio permite elegir el otro billete sin borrar tus descubrimientos."
        DreamPocket.TRAIN->"La tabla junto a la entrada permite elegir dónde bajar. El viaje dura doce segundos. Los billetes del refugio abren una parada sin cartel."
        DreamVoyage.REFUGE->"La tiza y la grabadora están en la mesa izquierda. OBJETOS permite usarlas. Mueve la silla cerca de la ventana alta; dormir permite elegir un nuevo billete."

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
