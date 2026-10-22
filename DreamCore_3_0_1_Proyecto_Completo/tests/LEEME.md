# Regresión de texturas 3.0.1

Las instrucciones y límites de `tools/run_texture_checks301.py` están en [CORRECCION_3_0_1.md](../CORRECCION_3_0_1.md). Las fronteras Android y GL de `trace301` solo pertenecen a las pruebas, no al APK.

# Pruebas de 3.0

`python tools/run_checks30.py` compila las comprobaciones puras con Kotlin 1.9.24 y Java 17. El compilador `kotlinc` debe estar en PATH; opcionalmente define KOTLINC y KOTLIN_STDLIB para usar una distribución sin runtime incorporado. DREAMCORE_VALIDATION_OUT permite elegir dónde guardar los resultados.

`UltimoTren30Check.kt` comprueba decisiones, viajes, objetos, recompensas, guardado y accesibilidad de las interacciones mediante recorridos en rejilla. Las variantes secretas se exportan con prefijo `variant30_` para no reemplazar las geometrías del catálogo usadas por los ensayos indexados.

`tools/check_shaders30.py`, `tools/render_update30.py` y `tools/render_compatibility30.py` usan los volcados de esa ejecución. Requieren Python, NumPy, Pillow y GLES2/EGL. `tools/generate_play30.py` también necesita ffmpeg y solo genera grabaciones originales para las nuevas notas, efectos y tres composiciones.

Los registros ejecutados que se distribuyen están en `tools/validacion30` y `VALIDACION.json`. Las pruebas de capacidades y ciclo de buffers registran la frontera GL; las imágenes y shaders se probaron con Mesa. No equivalen a mediciones en un teléfono.

## Pruebas anteriores conservadas

# Pruebas de compatibilidad 2.9

Ejecuta `python tools/run_core_checks_29.py` con Kotlin 1.9.24 y Java 17 instalados. Incluye las regresiones anteriores, indexado exacto y políticas de capacidades, límites de destinos, adaptación de cadencia y comprobación de los cálculos GLSL.

Después, `python tools/run_gpu_trace28.py` y `python tools/run_runtime_trace29.py`. Define `DREAMCORE_VALIDATION_OUT` como `build/validation29` si usas la herramienta antigua de trazas. Las clases de `tests/trace28` y `tests/trace29` simulan la frontera GL para comprobar los métodos reales; no se incluyen en el APK y no emulan un teléfono. Si tu compilador embebido no trae la biblioteca estándar, indica su JAR con `KOTLIN_STDLIB`.

`probe_math29.py` ejecuta matemática de los shaders reales sobre GLES2; `check_shaders29.py` enlaza las variantes exportadas. `compare_frames29.py`, `quality_cases29.py`, `render_compatibility29.py` y `gallery29.py` usan Python, NumPy, Pillow y EGL/GLES2/Mesa. `DREAMCORE_VALIDATION_OUT` apunta a los shaders y páginas de 2.9; `DREAMCORE_BASELINE_DUMPS` apunta a las geometrías y `catalog_maps.tsv` de 2.8. Se requieren las carpetas fuente 2.8 y 2.9 hermanas para las comparaciones.

Los informes ejecutados se incluyen en `tools/validacion29`; no son mediciones de temperatura/FPS ni pruebas físicas de POCO, Redmi o Android 4.4. La aplicación también se compila y se audita por Android Lint con mínimo API 19.
