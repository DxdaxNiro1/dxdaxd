# DreamCore 1.8 — Ecos del sueño

Actualización para Android Studio: Kotlin, OpenGL ES 2.0, Android 5 o posterior. Continúa el proyecto existente; no requiere Unity.

## Instalar en tu proyecto actual

1. Cierra el juego y extrae **todo** el ZIP a una carpeta nueva.
2. Ejecuta `Instalar_Windows.cmd`. La ruta sugerida es `C:\Users\Servidor Dxdax\AndroidStudioProjects\DreamCore`; puedes pegar otra ruta cuando lo pida. El instalador crea una copia de `app/src/main` y de `app/build.gradle.kts` antes de copiar.
3. Abre tu proyecto en Android Studio, sincroniza Gradle y usa **Build → Build APK(s)** para probarlo. La versión debe mostrar **1.8**, código **18**.
4. Para publicar una actualización instalable sobre tu app, conserva el mismo `applicationId` y firma con **tu misma clave** en **Generate Signed Bundle / APK**. Este paquete contiene fuentes, no una clave de firma ni un APK publicado. Android usa APK o AAB.

Instalación manual: copia los `.kt` de `app/src/main/java/com/dreamcore/game` a la carpeta que ya contiene tu `DreamRenderer.kt`; copia `res/raw` y `res/drawable-nodpi` conservando las carpetas. Actualiza `versionCode = 18` y `versionName = "1.8"` en `app/build.gradle.kts`. No borres los demás recursos originales.

Si la copia falla, no compiles una actualización incompleta: restaura `DreamCore_backup_1_8_.../main` sobre `app/src/main` y el `build.gradle.kts` respaldado sobre `app/build.gradle.kts`. El instalador no se ha ejecutado en Windows en esta sesión.

También se entrega un **proyecto completo**: extrae su ZIP y abre la carpeta **DreamCore**, donde está `settings.gradle.kts`, desde Android Studio. Usa JDK 17 o compatible y SDK 34. El wrapper Gradle 8.7 y las versiones AGP 8.5.2/Kotlin 1.9.24 están incluidos; el SDK lo instala Android Studio. No se incluyen rutas locales, cachés ni claves privadas.

## Qué cambia

- **Persistencia real:** mapa, posición, cámara, recuerdos, descubrimientos, llamadas únicas, símbolos, decisiones, visitas, finales y estado lunar. Guardado cada ocho segundos, al usar objetos/cambiar de mundo y al pausar; hay una copia anterior con CRC para recuperarse de datos dañados. Un sueño secreto conserva su puerta de retorno incluso después de cerrar la app. El guardado empieza con esta versión; la 1.7 no almacenaba ese progreso. No hay sincronización entre teléfonos y desinstalar/borrar datos elimina la partida.
- **Consecuencias:** la lámpara de la pradera deja una ventana cálida en otra casa; las llamadas reconocen la luz y los descubrimientos. Un cajón entrega una llave; combinarla con la radio cambia la habitación a la que abre la puerta del hotel. El cajón se desplaza visualmente al abrirse. Las calles llevan a jardines distintos según las visitas, los cielos de las ventanas cambian y se conserva el pasillo imposible.
- **Objetos y misterio:** lámpara, tres teléfonos, cajones, radios, cuatro símbolos y dos decisiones sobre el billete. Las cuatro notas reaparecen en distintos mundos. El diario cuenta recuerdos, símbolos, llamadas, sueños secretos y finales.
- **Tres sueños pequeños:** lavandería entre nubes, tren vacío sumergido y habitación del amanecer. Cada uno tiene música propia, ambientación y una puerta visible para volver a su origen. Las ventanas del tren muestran agua azul verdosa; la habitación del amanecer tiene luz cálida.
- **Tres epílogos:** El Eco, Permanecer y Despertar, según recuerdos, llamadas, símbolos y elección del billete. Cada uno modifica el cielo y el ambiente del Umbral; sus elementos permanecen en la partida. Se puede volver a explorar para encontrar otro final.
- **Presencias:** una figura curiosa se acerca cuando no la miras y mantiene distancia; otra avanza hacia la salida y emite notas direccionales; la observadora de las ventanas se cambia de lugar al distraerte y deja huellas temporales. Se mantienen los encuentros anteriores. No hay combate.
- **Fenómenos espaciados:** lluvia ascendente, sombra de nube fija, casas invertidas en el cielo o apagado lento de estrellas en mapas nocturnos. Ocurren durante ventanas cortas dentro de ciclos de unos 145 segundos en exteriores; no están activos todo el tiempo.
- **Nuevo nivel 11/12:** El parque de las lágrimas suspendidas, inspirado en las pasarelas, mosaicos, tuberías, pato gigante y rostros con agua del vídeo. Tiene un recorrido elevado curvo de unos 330 metros y un desvío hacia el pato. Las colisiones sostienen el recorrido y bloquean el vacío, la estatua y los bancos. Se mantienen piscina y abismo al comienzo y Dark Fantasy como portal separado.
- **Gráficos:** atlas original de 16 materiales detallados, relieve de iluminación derivado de la textura, rugosidad y brillo por material. Sombras direccionales con profundidad en calidad media/alta, cuatro muestras de suavizado y mapas de 512/1024 píxeles cerca del jugador. Espejos planos existentes, agua animada, ventanas de otros cielos y luz local cálida de la lámpara.
- **Luna:** textura original de roca con rostro esculpido, grietas y dientes; conserva parpadeos recurrentes, seguimiento de ojos, deformación progresiva de sonrisa y aparición gigante tras 120 segundos de observación. No cuenta detrás de la catedral ni durante la pausa. El nuevo rostro y las texturas son arte generado original inspirado en las referencias, no fotogramas extraídos.
- **Sonido:** cuatro composiciones originales nuevas con frases y timbres distintos (cristal/piano, piano aislado, órgano sumergido y arpa/coro), un ambiente interior hueco, radio con melodía y cuatro notas de interacción. Continúan las trece músicas anteriores, brisa exterior, pasos suaves con variantes por suelo, ecos discretos y transiciones entre capas. El eco es aproximado; no se calcula acústica física en tiempo real. No se extrajo música de los vídeos.
- **Comodidad:** toca **AJUSTES · DIARIO** arriba a la derecha. Sensibilidad de cámara, música y ambiente separados, pasos suaves/normales/silencio y calidad baja/media/alta. Las preferencias se conservan. El botón contextual ahora dice **USAR** tanto para objetos como para puertas.

## Alcance gráfico y comprobación

Se mantiene el motor propio GLES2 para abrir y compilar directamente este proyecto. La textura nueva aporta detalle, pero la geometría sigue siendo estilizada. No se afirma fotorrealismo completo, ray tracing, HDR, iluminación global ni fidelidad idéntica a la imagen. Las sombras son direccionales; la lámpara ilumina cerca sin una sombra puntual propia. Calidad baja usa atlas de 512, desactiva sombras y refresco de espejos; media/alta usan atlas de 1024. La calidad puede cambiarse jugando.

Un motor como Unity con URP permitiría herramientas más completas de materiales, iluminación, animación y edición de escenarios, a cambio de migrar código y ajustar consumo/tamaño. No se ha hecho esa migración. Esta versión mejora el renderizador del juego existente.

Se compiló un APK Android 1.8 real y se verificaron versión, firma de depuración y recursos. Las fuentes entregadas se cotejan con las compiladas. Hay pruebas de geometría, recorrido, colisiones, física, guardado y recuperación, consecuencias, secretos, finales, presencias y fenómenos; shaders GLES2 con precisión normal y mediump. Los diez OGG nuevos se decodificaron sin clipping. Las imágenes de `vistas` proceden del GLSL y geometría reales en Mesa/GLES2 de la nube, sin HUD; no son capturas de un teléfono.

**Pendiente de comprobar en dispositivo:** controles Android, rendimiento/FPS, sonido percibido y comportamiento de la GPU del teléfono. Si notas lentitud, cambia calidad a baja. No hay promesa de FPS ni de que todas las GPUs antiguas rendericen igual. Consulta `VALIDACION.json` para el detalle de las pruebas.

Pistas y condiciones completas: `RECORRIDO_CON_SECRETOS.md` (contiene spoilers).
