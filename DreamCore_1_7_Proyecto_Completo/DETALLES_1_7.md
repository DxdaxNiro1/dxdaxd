# DreamCore 1.7 — Lunas vivas

Actualización de Kotlin y recursos para Android Studio/OpenGL ES 2.0. Incluye lo anterior de la 1.6 y estas mejoras de movimiento, sonido, Dark Fantasy y exploración.

## Aplicar a tu proyecto

1. Extrae **todo** el ZIP en una carpeta aparte, cierra el juego y detén la compilación en Android Studio.
2. Ejecuta **Instalar_Windows.cmd** y elige tu carpeta DreamCore, donde están `app` y `settings.gradle.kts`. Las rutas con espacios están admitidas. El instalador necesita `app/build.gradle.kts`.
3. Se guarda un respaldo de `app/src/main` y de `app/build.gradle.kts` en `DreamCore_backup_1_7_...`, dentro del proyecto. Luego se mezclan fuentes, sonidos y texturas y se ajusta Gradle a versionCode 17 y versionName 1.7.
4. Sincroniza Android Studio y ejecuta Run o `gradlew.bat assembleDebug`.

Alternativa manual: respalda esas mismas carpetas; copia los `.kt` al paquete que ya contiene DreamWorlds.kt, y mezcla `res/raw` y `res/drawable-nodpi`. Conserva PlayerState.kt, iconos, manifiesto, recursos originales, applicationId y firma. Ajusta las dos versiones de `app/build.gradle.kts` a 17 y 1.7. Si no sabes qué archivos tienes, usa el **proyecto completo** en una carpeta nueva.

Para restaurar, guarda cualquier cambio posterior, aparta el `app/src/main` actual y restaura `main` del respaldo como `app/src/main`; restaura también el Gradle respaldado. No mezcles la copia antigua con las clases nuevas al volver atrás. El instalador no borra tus otros archivos ni tus claves.

## Carrera y pasos

Caminar pasa de 3,5 a **4,2 m/s**. En exteriores, correr empieza a **10,4 m/s** y, después de sostener el movimiento, llega gradualmente a **12,4 m/s**. En interiores se limita a **8,2 m/s**. El agua conserva su reducción de velocidad. Los desplazamientos rápidos se dividen en tramos cortos para comprobar paredes, muebles y bordes de estanques.

Los pasos usan **tres variantes por superficie** (hierba, piedra, baldosa, madera y agua), con apoyo de talón, suela, punta y roce sintetizados. Menos volumen, menos agudos y ecos más discretos; se limita la frecuencia de las pisadas para que correr no genere un sonido continuo molesto.

Arriba a la derecha puedes tocar **PASOS: SUAVES** para alternar entre suaves, normales y silencio. Esta preferencia se guarda. Solo afecta a las pisadas; la música, el viento y los sonidos de exploración siguen presentes. Los avisos largos ahora se dividen en líneas.

## Las lunas de Dark Fantasy

Entra por el portal de **Dark Fantasy**, a la derecha del Umbral. Las dos lunas tienen ojos, párpados, pupilas y boca animados. Parpadean repetidamente con ciclos diferentes, y las pupilas cambian según tu posición. La sonrisa de cada una crece mientras miras su cara y se relaja lentamente al apartarte. La luna principal se vuelve más fría y azulada.

Al acumular **120 segundos mirando las lunas durante esa visita**, aparece rápidamente una luna azul mucho mayor, de sonrisa ancha, inspirada en la imagen enviada. Permanece en el cielo y sigue parpadeando y mirando al jugador. El evento sucede una vez por visita; salir de Dark y volver inicia otra vigilia.

El contador avanza solo mientras la cara está en la región central de la mirada y el cielo está visible. Se toleran interrupciones de hasta **dos segundos**; una interrupción mayor reinicia los 120 segundos de observación, aunque la sonrisa conserva parte de su expresión. Pausar el juego no hace avanzar ese tiempo. La visibilidad se comprueba contra los triángulos de la escena para que una luna detrás de la catedral no cuente.

Hay más estrellas, parpadeo lento, cometas con estelas que vuelven a cruzar el cielo y dos capas ambientales originales, suaves, para la observación y la aparición. Los reflejos de luna en el agua y los materiales pulidos usan la misma expresión animada. Se mantiene el control del personaje durante el evento. La cara es un recurso procedural estilizado, no una copia fotográfica del video o la imagen.

## Qué da vida a Dreamcore

No se ampliaron otra vez los mapas. Se conservan la piscina y el hoyo como etapas 1 y 2, el recorrido lineal de once etapas y el portal aparte de Dark Fantasy.

Los nueve mundos renovados tienen ahora **un recuerdo opcional cada uno**: parada de autobús vacía, luces de jardín, campana sobre el agua, reloj de otra hora, formas del mar suspendido, corona fuera del tablero, teléfono del hotel, parada nocturna y máquina de billetes final. Cada escena tiene un pequeño detalle narrativo al descubrirla y luces flotantes en movimiento.

El indicador muestra **Recuerdos 0/9**. Los nueve recuerdos se suman a los 23 descubrimientos anteriores: **32 encuentros de exploración**. Se recuerdan durante la sesión; puedes terminar el juego sin reunirlos todos. Si reúnes los nueve, el mensaje final cambia. Las copas de los árboles se mecen y los columpios del pueblo se balancean suavemente.

Siguen presentes las 73 casas del pueblo, los jardines, la piscina imposible, el cuarto tras el espejo, las ventanas que cambian al regresar, los cuatro espejos de escena, la figura lejana, las nubes con vientos diferentes y las trece composiciones musicales propias.

## Qué está comprobado

Lee VALIDACION.json y tests/LEEME.md: APK real 1.7 compilado, firma/versión/recursos comprobados, **1.252 pruebas** de código puro, shaders normales y mediump en GLES2/Mesa y vistas con estados reales de animación. Los 17 sonidos nuevos se decodificaron y midieron sin clipping. No se usaron grabaciones externas para los nuevos pasos o capas lunares.

Las imágenes de `vistas` son renders en la nube con los mapas y shaders reales, sin HUD; no capturas de un móvil. Queda pendiente probar la ejecución, los controles y el rendimiento en tu teléfono, y ejecutar el instalador en Windows. Los mapas son finitos y los reflejos/nubes conservan las aproximaciones descritas en versiones anteriores. No se ha medido FPS de tu dispositivo.
