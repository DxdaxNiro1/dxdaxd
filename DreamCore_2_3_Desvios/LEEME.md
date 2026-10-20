# DreamCore 2.3 · Desvíos

## Actualizar tu proyecto

Extrae TODO el ZIP de actualización y ejecuta **Instalar_Windows.cmd**. Indica tu carpeta `C:\Users\Servidor Dxdax\AndroidStudioProjects\DreamCore`. Se crea una copia `DreamCore_backup_2_3_...`, se copian los archivos Kotlin y recursos, y Gradle pasa a **versionCode 23 / versionName 2.3**. Abre Android Studio y compila el APK. Si algo falla durante la copia, no compiles: restaura `main` y `app/build.gradle.kts` desde esa copia.

También puedes abrir el proyecto completo desde la carpeta **DreamCore**, donde está `settings.gradle.kts`. Necesita SDK 34 y JDK 17 o compatible; Gradle 8.7, AGP 8.5.2 y Kotlin 1.9.24; Android mínimo API 21 y OpenGL ES 2.0. Android Studio crea tu `local.properties`.

Para conservar la partida al instalar, usa tu mismo identificador `com.dreamcore.game` y tu misma clave de firma. No desinstales la app anterior. Los guardados 1.8 a 2.2 se aceptan en el esquema 23. Continúa tu partida; no hace falta Nueva partida. Las decisiones nuevas se incorporan cuando investigas. El instalador Windows se revisó pero no se ejecutó en esta máquina Linux.

Instalación manual: copia TODOS los `.kt` junto a MainActivity.kt, y `res/raw` / `res/drawable-nodpi`, manteniendo recursos anteriores. Cambia ambas versiones de Gradle. No se incluyen SDK, cachés, APK firmado con otra clave, claves privadas ni videos originales.

## Empezar a jugar lo nuevo

**Ajustes → Rutas y destinos → El pueblo · investigar la llamada** lleva al inicio de la investigación. También puedes llegar al pueblo por el recorrido Dreamcore habitual. El teléfono del camino está cerca de la entrada. Puedes dejar la investigación y volver después; no se impide continuar tu viaje.

**Ajustes → Diario de pistas** registra lo que descubriste y conserva el historial aunque cambies de decisión. El diario no dibuja una ruta ni revela pistas que todavía no encontraste. Los contadores de recuerdos y símbolos anteriores siguen en Ajustes.

El selector incluye **Backrooms**, **Las aberturas del cielo**, **Castillo y corredores de nubes**, **La ciudad de las flores** y **La pradera de las grietas**. En el Umbral hay dos puertas adicionales detrás de la zona de llegada: Backrooms y Desvíos. Las puertas llevan nombres. Los escenarios anteriores, el refugio y la pradera de ojos sin ataques siguen disponibles.

## El pueblo: varias soluciones y consecuencias

El misterio es una llamada que parece contestarse desde una casa con el teléfono desconectado. Puedes investigar en cualquier orden:

- Seguir el cable y decidir cómo alimentar o silenciar la casa. La caja cambia entre tres lugares coherentes según la semilla de la partida; el cable conduce a ella.
- Fotografiar el cristal de la fachada cuando lo ves de frente y sin obstáculos. La pista se concede después de guardar correctamente la foto.
- Escuchar al cartero y responder a su melodía. El personaje recuerda tus encuentros y la decisión que tomaste en la casa.

Cada camino permite entrar por sí mismo. No hace falta completar los tres ni contestar primero el teléfono. La lámpara del jardín existente también está integrada en la investigación. Dentro puedes escuchar el auricular y decidir si cuelgas o mantienes la línea abierta.

Dar corriente atrae la mirada de una presencia existente; silenciar retira esa atención y apaga la luz. Dejar la línea abierta añade un lugar preparado para alguien; colgar cambia el ambiente y lo que recuerda el cartero. Las presencias mantienen distancia y no causan daño. Los objetos, fotos, cartas y secretos de versiones anteriores se conservan.

## Escenarios con reglas observables

| Lugar | Espacio y regla |
| --- | --- |
| Backrooms: las luces que no descansan | 49 salas conectadas con bucles y distribución sembrada por partida, papel amarillo, alfombra, fluorescentes y zumbido. Las lámparas estables forman un recorrido hacia la salida; las inestables pueden llevar por otros caminos. Restablecer corriente facilita la lectura de la zona y llama la atención de una figura. Hay retorno junto a la entrada y en la salida lejana. |
| Las aberturas del cielo | Campo bajo aberturas luminosas, nubes, rayos de colores y postes. Detenerte bajo un círculo claro y observar el cielo hace responder una ventana. |
| Los corredores del castillo de nubes | Corredores acristalados entre nubes, habitaciones laterales, puente y patio de castillo con torres. Un marco sin cristal cambia la distancia hasta el castillo. Puedes recorrerlo andando o descubrir ese acceso. |
| La ciudad al otro lado de las flores | Campo florido, arco de hormigón, torres acristaladas, piscina y tren con parada periódica. Esperar junto a la parada cuando llega permite viajar; puedes seguir explorando y regresar después. |
| La pradera de las grietas | Meseta verde, grietas verticales profundas, dos puentes, casa rosa y sendero de baldosas con cartel sonriente. Hay caminos por ambos lados y el cartel explica la conexión de la casa. |

Las conexiones descubiertas permiten circular **aberturas → nubes → ciudad → grietas → aberturas**. Conservan el retorno al lugar desde el que entraste; dar vueltas por ese circuito no llena la pila de retornos. Cada lugar también se puede visitar directamente desde el selector. Backrooms mantiene su rama aparte. La casa del misterio es un sexto espacio interior accesible desde el pueblo.

Se adaptaron motivos de los cuatro videos y las imágenes en escenarios estables que se pueden recorrer. La imagen del hoyo corresponde al abismo de casas ya presente y se conserva; no se añadió un duplicado del mismo mapa. No son reconstrucciones fotográficas idénticas a los videos.

## Menos trabajo y protección térmica

En **Ajustes → Energía** puedes elegir:

- **Automática:** conserva resolución completa mientras Android no comunica calor; reduce el tamaño del búfer a 88% por lado con señal moderada y 75% con señal severa. Se conservan los modelos, mapas, texturas, iluminación y efectos; disminuye la definición de la imagen mientras se protege el dispositivo.
- **Resolución completa:** respeta el tamaño nativo, conservando el límite de fotogramas por temperatura.
- **Fresco · 24 FPS:** reduce la resolución a 85% por lado, o 75% con calor, y limita a 24 FPS. Es la opción para probar primero si tu Infinix NOTE 50s sigue calentándose.

Android informa temperatura a partir de API 29. En teléfonos anteriores permanece disponible el modo Fresco manual. El objetivo normal sigue siendo 30 FPS, con opción 60; con calor moderado el límite es 30 y con señal severa 24. Son objetivos de planificación, no FPS medidos en el teléfono.

Además, se omiten los pases de sombras que no aportan a interiores completamente cerrados; se evitan lecturas del mapa de relieve lejano en materiales mates; los espejos se refrescan menos bajo calor; la búsqueda de objetos usa una pasada sin listas temporales ni raíces cuadradas; se reutiliza el conjunto de objetos no sólidos; y los menús detienen también las actualizaciones repetidas de la interfaz. Siguen el descarte por cámara, sombras reutilizadas, reflejos por visibilidad y límite de escenas en caché.

Estos cambios reducen trabajo concreto del motor. No se ha medido temperatura, consumo o FPS en un teléfono físico y no se garantiza compatibilidad con todos los dispositivos. La resolución adaptativa tiene un coste visible de nitidez; no elimina contenido ni rebaja las texturas guardadas.

## Música, comprobaciones y siguientes pasos

Seis piezas originales distintas acompañan Backrooms, el campo luminoso, las nubes, la ciudad, las grietas y la casa del teléfono; hay un zumbido eléctrico original suave para Backrooms. No se extrajo música de las referencias.

`VALIDACION.json` registra compilación, pruebas, shaders, audio y vistas. Las imágenes de `vistas/` son renders del motor en Mesa GLES2, sin interfaz Android; no son capturas del teléfono. La interacción real del menú, los controles y la temperatura requieren prueba en tu equipo.

`PLAN_DEL_JUEGO.md` conserva el rumbo y las decisiones de esta conversación. Las cinco mecánicas se implementan primero en esta investigación y los nuevos escenarios, para valorar su funcionamiento antes de extenderlas a los mapas antiguos restantes. El combate de la pradera de ojos, multijugador y otras ampliaciones siguen pendientes de una decisión futura.

Para repetir la lógica: Java 17, compilador Kotlin 1.9.24 en PATH y `python tools/run_core_checks_23.py`. Para compilar: `gradlew.bat :app:assembleDebug`. Las herramientas de vistas necesitan Python, numpy, Pillow y EGL/GLES2; no son dependencias del juego.
