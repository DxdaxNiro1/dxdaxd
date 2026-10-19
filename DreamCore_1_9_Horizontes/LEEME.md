# DreamCore 1.9 — Horizontes imposibles

Fuentes para Android Studio, Kotlin y OpenGL ES 2.0. Actualización grande sobre la 1.8, con arte original generado y modelado mediante geometría del juego.

## Instalar

Extrae **todo** el ZIP de actualización y ejecuta `Instalar_Windows.cmd`. Selecciona tu carpeta de DreamCore en Android Studio. El instalador respalda `app/src/main` y `app/build.gradle.kts` antes de copiar y cambia la versión a **19 / 1.9**. Android Studio → sincronizar → Build APK(s). El instalador no se ha ejecutado en Windows aquí.

Para actualizar la app instalada conservando la partida, mantén el mismo `applicationId` y **firma con tu misma clave**. No desinstales ni borres datos. No se entrega una clave privada ni un APK publicado. Para distribución usa Generate Signed Bundle / APK. El código usa minSdk 21, compile/targetSdk 34, Gradle 8.7, AGP 8.5.2 y Kotlin 1.9.24; usa JDK 17 o compatible.

Alternativa: extrae el ZIP del proyecto completo y abre **DreamCore**, donde está `settings.gradle.kts`, en Android Studio. Deja que Android Studio cree `local.properties` para tu SDK.

Manual: copia todos los `.kt` del parche junto a `DreamRenderer.kt`, y copia los recursos `res/raw` y `res/drawable-nodpi` conservando los recursos anteriores. Establece `versionCode = 19` y `versionName = "1.9"`. Para restaurar, devuelve la carpeta `main` y `build.gradle.kts` desde `DreamCore_backup_1_9_...`; no compiles después de una copia incompleta.

## Nuevos lugares y remodelaciones

- **El tobogán de los planetas:** recorrido de unos 600 metros sobre nubes, con suelo rosa y laterales curvos, miradores, plantas y una Tierra enorme. Se recorre con tus controles, sin imponer una cámara de caída. Planetas con continentes, océanos, hielo o bandas de gas; rotación, terminador de iluminación y atmósfera. Los tres tipos visibles en este mapa se diferencian y los demás mundos usan una selección derivada de una semilla persistente. Los planetas pasan detrás de las nubes. Eclipses suaves modifican la luz; una constelación dibuja una casa.
- **La ciudad del último recreo:** mundo original Dreamcore. Escuela vacía con pupitres, pizarra y ventanas; centro comercial con atrio y una nube interior; cancha iluminada aislada; una fiesta preparada sin invitados. El piano se escucha al acercarte a una habitación y calla al entrar. Un ascensor del hotel permite llegar bajo una lluvia localizada.
- **El jardín de las coronas vacías:** mundo original Dark Fantasy, conectado después de la catedral. Jardines circulares sobre agua oscura, arcadas, árboles imposibles y esculturas que lloran. Las presencias son más altas y tienen rostro, manos y una corona de ramas. No atacan. Se puede regresar a la catedral o terminar en el Umbral.
- **La cama al borde del universo:** sueño secreto inspirado en la imagen: cama modelada con colchón, almohadas y pliegues, nubes, abertura cósmica, portal de colores y lluvia solo sobre la cama. La habitación se ensancha visualmente despacio; el borde de nubes sigue delimitando el suelo transitable.
- **La torre que viste desde lejos:** su silueta aparece en varios mundos. Cuatro pistas fotográficas abren su entrada en el recorrido de planetas. Se sube por una rampa con peldaños que emergen lentamente; hay ventanas de otros sueños, pequeños planetas y agua que asciende por las paredes. Tiene una puerta de retorno al lugar de entrada.
- **Parque de las lágrimas ampliado:** de unos 330 a unos 610 metros de recorrido, más plataformas, desvío del pato y seis grandes tuberías curvas. Tres cabezas esculpidas con volumen, cejas, párpados, nariz, mejillas y labios reemplazan las caras planas. El agua sale de sus ojos. El pato tiene más segmentos y alas. Las colisiones sostienen los caminos y bloquean bancos, estatua y vacío; las tuberías siguen siendo decorativas y no transitables.
- **Mar del cielo remodelado:** superficie suspendida ondulada, reflejos de baja frecuencia, arcos, peces grandes con colas, bancos de peces pequeños, medusas con tentáculos animados, jardines y reflejos de luz suaves en el piso. Conserva su puerta al tren sumergido.
- **Interiores reunidos:** la casa del pasillo repetido deja la ruta principal. El reloj, el cajón/llave y los recuerdos se reúnen con la estación final, ahora un conservatorio ancho con cúpula y plantas. Se conserva un único tren sumergido, como sueño secreto. La piscina imposible de la casa del pueblo permanece.

## Cámara, encuentros y recorrido

**FOTO**, arriba a la derecha, guarda una imagen real del renderizado, sin el HUD. Algunas figuras solo se ven durante el breve instante de la foto y quedan en ella. Apunta a las señales de la casa, el mar, la ventana y el planeta. El HUD avisa cuando hay una señal fotográfica cerca; la dirección, distancia y oclusión por la geometría cuentan. No se gana la misma pista dos veces. En **AJUSTES · DIARIO → Álbum** se ven las últimas doce fotos, almacenadas dentro de la app sin permiso de galería ni subida a Internet. Las pistas permanecen aunque una foto antigua salga del álbum. Si falla el guardado de la imagen, se avisa y se puede repetir. El guardado/captura y controles todavía requieren comprobación en un teléfono real.

El compañero invisible deja una segunda taza cuando miras a otro lado, acompaña tu movimiento con pasos discretos que se detienen contigo y abre una puerta en la escuela. La figura de la parada espera, se acerca al autobús cuando llega y desaparece dentro; el autobús se marcha y el encuentro queda recordado. Estos encuentros son escenificados, sin combate ni transporte controlable.

Las presencias anteriores tienen ahora más detalle y miran hacia tu posición. Las nuevas mantienen distancia. El comportamiento sigue siendo de observación e incomodidad, sin daño al jugador.

Se mantienen los objetos, llamadas, cuatro símbolos, nueve recuerdos principales y tres finales de la 1.8. Los dos recuerdos del interior retirado y la estación se obtienen juntos en la estación nueva; el progreso anterior se respeta. Hay descubrimientos adicionales en la ciudad y la torre. La ruta principal tiene **13 etapas**, con piscina y abismo primero; Dark Fantasy sigue entrando por su portal propio y ahora tiene dos mundos conectados. Hay cinco sueños secretos opcionales.

Las transiciones tienen una entrada visual suave. El ascensor seco desemboca bajo lluvia en la ciudad; atravesar el agua de la piscina imposible lleva al jardín nocturno. Se puede continuar desde allí por la ruta y volver mediante sus portales. En la estación cae nieve, en el centro comercial flota una nube y sobre la cama cae lluvia localizada. Continúan los fenómenos espaciados anteriores.

## Música, guardado y límites

Cinco composiciones originales nuevas, con frases y timbres propios, para planetas, ciudad, jardín oscuro, torre y cama cósmica. Una frase de piano sirve al encuentro de la habitación vacía. Se conservan brisa, ambiente interior hueco, pasos por suelo, volumen separado y calidad baja/media/alta. El eco y el sonido direccional son aproximaciones, no acústica física por trazado de rayos.

El guardado conserva mapa, cámara, posición, decisiones, fotografías descubiertas, encuentros completados, semilla planetaria y estado lunar. Sigue guardando cada ocho segundos, al interactuar/cambiar de mundo y al pausar, con CRC y copia anterior. La 1.9 **lee las partidas de la 1.8** usando el mismo almacenamiento. Si estabas en el interior retirado, reapareces en la estación reunida; si estabas en el parque que cambió de forma, reapareces en su entrada segura. Los sueños secretos conservan la puerta por la que se entró. Desinstalar o borrar datos elimina la partida y el álbum; no hay sincronización en la nube.

El evento de las lunas sigue activo: mirar durante **120 segundos efectivos** produce la aparición gigante. Apartar la mirada más de dos segundos reinicia la vigilia, y la pausa/oclusión no cuentan. Ahora la atmósfera lunar también acompaña el nuevo reino oscuro.

El motor continúa siendo GLES2. Se mejoran superficies, modelado y composición, pero la geometría sigue siendo estilizada. No se promete fotorrealismo, reproducción exacta de las imágenes/vídeo, ray tracing ni FPS de un teléfono. Las sombras son direccionales cerca del jugador; la iluminación local y efectos atmosféricos son aproximados. Las vistas incluidas son renders del GLSL y geometría reales en Mesa/GLES2 de la nube, no capturas de móvil ni pinturas promocionales.

Consulta `VALIDACION.json` para compilación, pruebas, fuentes cotejadas y audio. Falta comprobar ejecución, sonido percibido, controles, cámara/álbum y rendimiento en dispositivo, además del instalador en Windows. Las instrucciones con ubicaciones y spoilers están en `RECORRIDO_CON_SECRETOS.md`.
