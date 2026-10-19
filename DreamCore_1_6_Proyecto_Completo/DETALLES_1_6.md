# DreamCore 1.6 — Sueños sin medida

Actualización de código y recursos para Android Studio, Kotlin y OpenGL ES 2.0. No contiene un APK instalable: compila el proyecto para generarlo. Proyecto completo recomendado si no sabes qué versión de los archivos tienes.

## Instalar en tu proyecto actual

1. Extrae todo el ZIP en una carpeta aparte. Cierra el juego y detén la compilación en Android Studio.
2. Ejecuta **Instalar_Windows.cmd**, indica la carpeta que contiene `app` y `settings.gradle.kts`. Acepta rutas con espacios. Necesita la estructura Kotlin `app/build.gradle.kts`.
3. El instalador copia primero `app/src/main` y el Gradle de la aplicación a `DreamCore_backup_1_6_...` dentro de tu proyecto. Después copia código, sonidos y texturas; actualiza `versionCode = 16` y `versionName = "1.6"`.
4. Abre Android Studio, sincroniza y compila con `gradlew.bat assembleDebug`, o ejecuta Run en el dispositivo.

No extraigas el paquete de actualización dentro de `app`: sus rutas empiezan desde la raíz del proyecto. No necesitas cambiar `applicationId`, SDK ni claves de firma. Para actualizar un APK ya instalado debes usar la misma firma del proyecto anterior.

Alternativa manual: guarda una copia de `app/src/main` y de `app/build.gradle.kts`; copia los `.kt` desde `app/src/main/java/com/dreamcore/game` al paquete que ya contiene `DreamWorlds.kt`, y mezcla las carpetas `res/raw` y `res/drawable-nodpi`. Conserva `PlayerState.kt`, manifiesto, iconos y recursos originales. Ajusta las dos versiones de Gradle a 16 y 1.6.

Para restaurar: cierra Android Studio, guarda lo que hayas cambiado desde la actualización, aparta la carpeta `app/src/main` actual y restaura la carpeta `main` del respaldo como `app/src/main`. Así no quedan las clases nuevas duplicadas. Restaura también `build.gradle.kts` del respaldo. No borres otros archivos del proyecto.

## Qué cambió

La piscina y el hoyo conservan su geometría y su lugar como etapas 1 y 2. Se reconstruyeron los nueve mundos siguientes. Dark Fantasy mantiene su portal independiente y sus lunas esculpidas. Dreamcore sigue una ruta de once etapas, con puertas para avanzar y retroceder, hasta volver al Umbral.

| Etapa | Mundo | Exploración |
|---|---|---|
| 1 | Piscinas del recuerdo | Piscina original conservada |
| 2 | Abismo de las casas perdidas | 180 m, repisas verdes, caída y casa central conservados |
| 3 | Valle de las cúpulas de agua | Estanques, arcadas y casas distantes |
| 4 | Invernadero de las constelaciones | Árboles bajo estructuras abiertas, cuerpos luminosos y estrellas |
| 5 | Pueblo de las ventanas despiertas | 73 casas, calles, jardines, relieve suave y parque de columpios |
| 6 | Casa que recuerda otra puerta | Pasillo largo, alcobas y ventanas que cambian de lado al volver |
| 7 | Mar dentro del cielo | Avenida bajo agua suspendida y formas que flotan |
| 8 | Llanura de los reyes dormidos | Tablero extenso y piezas gigantes |
| 9 | Hotel de los horizontes | Bloques de habitaciones, ventanas irreales y grandes nubes |
| 10 | Carretera donde caen estrellas | Casas dispersas, agua junto al camino y cielo nocturno |
| 11 | Estación del último sueño | Sala final de espera; su puerta completa el viaje |

Los exteriores nuevos tienen un área transitable de unos 760 × 775 m. Son finitos: la niebla y la repetición distante buscan que parezcan continuar. La geometría se divide en zonas de 64 m; el juego conserva un solo mundo activo y dibuja las zonas cercanas.

El indicador bajo el título muestra dirección y distancia al siguiente umbral. Puedes desviarte y volver a la calle central. En la pradera, una casa pequeña cerca del inicio del pueblo (a la izquierda, x=-40, z=-55) permite entrar con el botón de interacción en una piscina inmensa de otra escala. Dentro hay una puerta para salir. Los bordes de la piscina son transitables; no se puede caminar sobre su agua.

En la casa del pasillo, uno de los espejos también conduce a un cuarto escondido; su puerta permite regresar.

Hay 23 descubrimientos de proximidad repartidos entre los mundos renovados, sonidos lejanos con posición estéreo, y una presencia en pueblo, invernadero y carretera. Mirarla unos segundos provoca una reacción y su desaparición gradual. No ataca; no es un enemigo con combate. Los descubrimientos se recuerdan durante la sesión. La casa cambia sus ventanas al cruzar de vuelta el pasillo, sin cerrar la ruta.

## Imagen y sonido

Cuatro espejos muestran una reflexión real de la escena desde una cámara reflejada: pueblo, invernadero, casa de los pasillos y hotel. Se actualizan cerca del jugador, a un máximo de diez veces por segundo, en una textura de 512 px. Si el dispositivo no puede crear el destino de reflexión, usan cielo aproximado. En este juego en primera persona el jugador no tiene cuerpo modelado para reflejarse. No hay reflejos recursivos ni sombras dinámicas de todo el mapa.

Luz tenue sobre el suelo, materiales menos planos, ventanas a otros cielos, movimiento independiente en las dos capas de nubes, nubes gigantes en pueblo/hotel, estrellas de parpadeo lento y cuerpos celestes que se desplazan en algunos mundos nocturnos. El agua del interior remoto usa coordenadas locales para evitar cuadros por baja precisión; el cielo usa una formulación que evita discos negros en GPUs con precisión limitada.

Hay **13 composiciones nuevas**, una por escena principal, de 44 a 68 segundos, con distintas notas, pausas e instrumentos (piano, arpa, vidrio, flauta, órgano y coro sintetizados). Cambian con fundidos al pasar de mundo. Viento, jardín, interior y piscina añaden capas según la zona. Los pasos distinguen hierba, madera, piedra, baldosa y agua, con ecos suaves en interiores. Las casas transitables del pueblo también cambian pasos y ambiente al entrar. Todo el audio nuevo es síntesis original; no se extrajo música de los videos.

## Verificación y límites

Consulta `VALIDACION.json`, `tests/LEEME.md` y `tools/audio_validation_16.json`. Se compila un APK real y se verifican sus recursos y firma; las imágenes de `vistas` se renderizan con la geometría y shaders del proyecto mediante GLES2/Mesa. No son capturas del teléfono.

La ejecución en un teléfono Android y el instalador en Windows siguen pendientes de prueba. No hay una medición de FPS ni una promesa de rendimiento para tu modelo. La carga por zonas reduce la geometría residente, pero la primera generación de un mundo puede producir una pausa. Los espejos cuestan un pase adicional cuando estás cerca. No incluye multijugador. El estilo es procedural y onírico; las nubes usan capas texturizadas, no simulación volumétrica fotográfica.
