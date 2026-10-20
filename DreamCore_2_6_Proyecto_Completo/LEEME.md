# DreamCore 2.6 · Profundidad

Revisión del motor y del modelado tomando las portadas como dirección artística. Afecta al renderizado compartido del juego; la remodelación más amplia está en las piscinas, la pradera y las casas que comparten modelo. [Ver comparaciones reales](VISTAS.md).

## Instalar sobre tu proyecto Android Studio

1. Extrae TODO el ZIP y cierra Android Studio.
2. Ejecuta **Instalar_Windows.cmd**. Indica tu carpeta de DreamCore; por ejemplo, `C:\Users\Servidor Dxdax\AndroidStudioProjects\DreamCore`.
3. El instalador guarda una copia completa de `app/src/main` y `app/build.gradle.kts` antes de copiar los archivos nuevos.
4. Abre tu proyecto, sincroniza y compila `gradlew.bat :app:assembleDebug`.
5. Instala usando tu mismo identificador y firma para actualizar conservando la partida.

Versión **2.6 / versionCode 26**, guardado de esquema 23. Para instalación manual, respalda las dos rutas anteriores, copia `app/src/main/java/com/dreamcore/game`, `app/src/main/res/raw` y `app/src/main/res/drawable-nodpi`, y cambia las dos versiones de `app/build.gradle.kts`. También se necesitan los nuevos archivos `DreamPost26.kt`, `DreamSky26.kt`, `DreamGraphics26.kt`, `DreamBotany26.kt`, `DreamPools26.kt`, `dream_botany_26.png` y `dream_cloud_volume_26.png`: copia las carpetas completas.

Para restaurar, cierra Android Studio y repón `main` y el Gradle desde la copia que indica el instalador. El script se revisó por código en Linux; no se ejecutó en Windows.

El **proyecto completo** se abre por la carpeta que contiene `settings.gradle.kts`. Android Studio configura la ruta del SDK. Requiere SDK 34, JDK 17 compatible, Android mínimo 21 y GLES2. Los ZIP no incluyen rutas locales, claves privadas ni carpetas de compilación.

## Qué cambia

- **Sombras de contacto:** oclusión ambiental de pantalla a media resolución, con reconstrucción de profundidad de alta precisión. Añade volumen en encuentros de paredes, muebles y suelo. Se activa solo cuando la GPU admite la profundidad y precisión necesarias.
- **Suavizado de bordes:** FXAA sobre la imagen del mundo; los controles Android se dibujan después. Alta añade un resplandor leve.
- **Nubes con volumen:** campo de densidad tridimensional precalculado, iluminación interna y desplazamiento mundial. Su pasada a media resolución usa 16 muestras en Media y 24 en Alta; el cielo, los planetas y las estrellas conservan su resolución normal. Los interiores cerrados y cielos especiales usan sus alternativas apropiadas. Baja mantiene nubes de dos capas.
- **Vegetación:** ramas redondas, copas de hojas recortadas, arbustos y flores en el pueblo; movimiento suave y detalle del pasto limitado por distancia. Las hojas también recortan su silueta en la sombra direccional.
- **Arquitectura:** tejados de teja rosa o pizarra, remates, chimeneas, zócalos y ventanas laterales con jambas y alféizares. Se conservan las entradas de las casas abiertas.
- **Piscinas:** vasos con fondo de mosaico a más de un metro bajo el agua, agua transparente con reflejo real de la sala según el ángulo de visión, bordes redondeados, columnas cilíndricas y tres claraboyas abiertas. Las dos salidas siguen en su lugar. El reflejo de ambos canales comparte una única captura plana con actualización espaciada.
- **Materiales:** se mantienen los dieciséis materiales de 2.5 y se añaden cuatro de construcción y doce variantes botánicas en dos atlas GPU. Conservan relieve, rugosidad y la iluminación por superficie de la base anterior.

No todos los escenarios se han reconstruido geométricamente. El motor compartido mejora los materiales y la presentación donde corresponda; la luna viviente, sus dos minutos de observación, los encuentros, los secretos y los 48 escenarios permanecen. Se conservan exactamente las 49 músicas y sus miniaturas.

## Calidad y consumo

**Media a 30 FPS** es el punto de partida para el Infinix NOTE 50s. Alta utiliza más memoria y trabajo gráfico. Si Android comunica calor elevado, los efectos se reducen; en ahorro o ante un límite de 24 FPS se omiten los nuevos efectos costosos. El contenido y la selección guardada de calidad se conservan.

Esta revisión añade efectos y tiene un coste mayor que 2.5. El volumen se prepara fuera del teléfono, las nubes y la oclusión se calculan a media resolución, y las copas del pueblo usan menos geometría. [Presupuesto concreto](RENDIMIENTO_2_6.txt). No se han medido temperatura, autonomía ni FPS en tu teléfono físico.

## Pruebas y aspecto real

APK compilado y firma verificada; regresiones de recorrido, caídas, guardado, objetos y misterios; pruebas nuevas de geometría, calidad y profundidad; renderizado de los 49 puntos del catálogo en GLES2/Mesa, incluyendo modos Bajo/mediump y Alto. `VALIDACION.json` registra los resultados y sus límites.

Las imágenes de [VISTAS.md](VISTAS.md) proceden de las mallas y shaders entregados, con las pasadas reales de nube, oclusión y suavizado. Son vistas de Mesa, no capturas del teléfono. El acabado todavía no es idéntico a las portadas ilustradas; esos materiales, composiciones y detalles siguen siendo la referencia para próximas remodelaciones.

Herramientas de desarrollo: `python tools/run_core_checks_26.py`, `python tools/check_graphics26.py`, `python tools/render_graphics_26.py --meshes build/validation26 --output build/graphics26`. Requieren Kotlin 1.9.24, Java compatible, Python/numpy/Pillow y EGL/GLES2. No se ejecutan durante el juego.
