# DreamCore 2.4 · Resonancias

Una banda sonora completa para cada escenario, el inicio y los recuerdos visitables. 49 composiciones originales, 103 minutos y 36 segundos de música, con motivos, armonías, pulsos, instrumentos y arreglos distintos. Cuatro secciones por pieza desarrollan y transforman las frases. Son composiciones de síntesis, con intención relajante e inquietante.

## Instalar sobre tu proyecto

1. Extrae TODO el ZIP, no ejecutes el instalador desde el archivo comprimido.
2. Ejecuta **Instalar_Windows.cmd** e indica tu carpeta `C:\Users\Servidor Dxdax\AndroidStudioProjects\DreamCore`.
3. El instalador hace una copia completa de `app/src/main` y del Gradle de app antes de copiar. Valida los 49 audios y las 49 miniaturas y actualiza a versionCode 24 / versionName 2.4. Retira únicamente las pistas antiguas conocidas, que quedan en la copia de seguridad; no borra otros audios propios.
4. Abre tu proyecto en Android Studio, sincroniza y compila con `gradlew.bat :app:assembleDebug`.
5. Instala el APK usando la misma clave de firma e identificador que antes para conservar tus datos. No desinstales la versión anterior. Si Android rechaza la firma, recompila con tu clave habitual; el ZIP no incluye claves privadas.

Para volver atrás: cierra Android Studio, sustituye `app/src/main` por el `main` de la copia y restaura su `build.gradle.kts`. No mezcles los archivos de ambas versiones. El instalador Windows no pudo ejecutarse en este entorno Linux.

Instalación manual: haz copia de `app/src/main` y `app/build.gradle.kts`, copia el contenido de `app/src/main/java/com/dreamcore/game`, `res/raw` y `res/drawable-nodpi` encima de tu proyecto, y cambia versionCode a 24 y versionName a `2.4`. No necesitas reemplazar tu firma ni tu configuración del SDK. Opcionalmente retira los nombres de música antigua enumerados en `tools/legacy_music_24.txt` para evitar conservar audios sin usar en el APK.

## Escuchar dentro y fuera del juego

- El inicio tiene una composición exclusiva. El Umbral conserva otra distinta.
- **Sala de escucha** aparece en el inicio y en Ajustes. Permite elegir entre las 49 pistas, ver la miniatura del escenario, pausar, reanudar, ir a la anterior o a la siguiente. Cerrar la sala restaura la música del lugar o del inicio. Abrirla detiene la simulación y los fotogramas del mundo.
- Cada mapa elige su pista exacta, manteniendo transiciones suaves y los controles separados de música y ambiente. Las fotografías visitables y los refugios también tienen piezas propias.
- Descarga aparte **DreamCore_2_4_Banda_Sonora.zip**: contiene todos los MP3 con nombres y miniaturas incrustadas, un catálogo de mapas, una lista M3U8 y **ESCUCHAR.html** con búsqueda, filtros, anterior/siguiente, aleatorio, repetición y descarga individual. Extrae todo el ZIP. El álbum es independiente y no tienes que copiar MP3 al proyecto Android.
- La pista de la casa antigua se conserva en el catálogo como archivo. El recorrido y los guardados siguen consolidando ese interior en la estación del último sueño, tal como ocurría en 2.3.

Las miniaturas son renders de las geometrías y shaders reales GLES2, no capturas del teléfono. Para la miniatura del inicio se usa una vista del Umbral. El MP3 subido como referencia supera el límite de transferencia de 32 MB: no pudo escucharse ni analizarse. Estas piezas originales interpretan la dirección de calma e inquietud de la conversación; no contienen muestras de ese archivo. Se puede afinar el estilo después con fragmentos menores de 32 MB.

## Una ayuda para orientarse

En **Ajustes → Pista del lugar** puedes leer indicaciones concretas para las aberturas del cielo, las nubes y el castillo, el tren de las flores, las grietas y Backrooms. La ayuda de las aberturas explica cuál de los círculos responde; la de la parada explica la espera de seis segundos. No cambia las condiciones de los rompecabezas ni reinicia descubrimientos.

## Rendimiento y guardado

La composición se hace fuera del teléfono. Durante el juego se reproducen OGG estéreo a 32 kHz; no se sintetizan notas en tiempo real. El mezclador mantiene como máximo dos bases musicales y dos capas ambientales durante las transiciones, libera las anteriores y pausa al pasar a segundo plano. La galería de navegador usa un único reproductor. Las miniaturas son de 480 × 270 y la sala Android carga solamente la elegida.

Se conservan los modos de energía y optimizaciones de 2.3. La pantalla de escucha mantiene parado el bucle de renderizado. No se midieron FPS, temperatura o batería en un teléfono físico, ni se verificó interacción táctil en Android. No se promete una mejora térmica medida ni compatibilidad universal.

El esquema de guardado continúa siendo 23, sin cambios de formato; se mantienen progreso, recuerdos, fotografías, semillas y decisiones. Android mínimo 21, SDK objetivo 34, GLES2, JDK 17 compatible, Gradle 8.7 / AGP 8.5.2 / Kotlin 1.9.24.

## Verificación

`VALIDACION.json` describe la compilación y firma del APK, la cobertura de los 48 escenarios, los 49 OGG y 49 MP3 decodificados, sus portadas, las pruebas de regresión, el mezclador bajo un ciclo de vida Android simulado y el reproductor en Chromium de escritorio y móvil. La política del Chromium administrado impide abrir rutas file://; la prueba de interfaz se hizo con un servidor local, sin peticiones externas. El HTML no depende de fetch, bibliotecas remotas ni conexión.

Repetir lógica: `python tools/run_core_checks_24.py` con Kotlin 1.9.24 y Java 17. Regenerar audio: `python tools/generate_soundtrack_24.py` con numpy y FFmpeg. Las herramientas son de desarrollo; no se ejecutan al jugar.
