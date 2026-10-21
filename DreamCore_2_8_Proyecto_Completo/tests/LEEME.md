# Comprobaciones de DreamCore 2.8

Instala Java 17 y el compilador Kotlin de consola 1.9.24. Desde esta carpeta de proyecto ejecuta `python tools/run_core_checks_28.py`. Compila la lógica sin Android, ejecuta los trece grupos de regresión anteriores y Fluidez28Check, y escribe sus resultados y geometrías en `build/validation28`.

Después ejecuta `python tools/run_gpu_trace28.py`: compila las clases reales de subida de mallas y caché de programas contra un registro de llamadas GL de prueba. Comprueba índices, rangos, páginas, sombras y liberación de identificadores. El registro de `tests/trace28` no entra en el APK. Si utilizas un compilador embebido sin librería estándar, indica su JAR en `KOTLIN_STDLIB`.

Las herramientas `compare_frames28.py`, `quality_cases28.py`, `check_shaders28.py`, `benchmark28.py` y `gallery28.py` usan Python, NumPy, Pillow y EGL/GLES2 con Mesa. Requieren extraer también el código 2.7 en una carpeta hermana `DreamCore_2_7_Vigilia` y generar sus geometrías con los tests anteriores. `DREAMCORE_BASELINE_DUMPS` indica la carpeta con `catalog_maps.tsv` y los `.f32` de referencia; `DREAMCORE_VALIDATION_OUT` indica la salida con los shaders y páginas indexadas de 2.8. Por defecto se usan `../DreamCore_2_7_Vigilia/build/validation27` y `build/validation28`. Las herramientas de render reproducen pasadas del motor; no ejecutan la aplicación Android completa.

`Memory28Probe` recorre tres veces los 51 mundos en procesos JVM independientes, con `-Ddreamcore.optimized=false` o `true`. La recolección forzada se limita a ese diagnóstico, nunca a la aplicación.

Los informes ejecutados para esta entrega están en `tools/validacion28` y `VALIDACION.json`. No representan mediciones en un teléfono físico. El APK se verifica por separado con Gradle y Android Lint.
