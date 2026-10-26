# Error al redimensionar un bitmap sin premultiplicar

El cargador anterior decodificaba con `DreamTexture29.options(data=true)` y llamaba a `Bitmap.createScaledBitmap` cuando las dimensiones no coincidían con la calidad elegida o con el límite de la GPU. Ese escalado utiliza Canvas. Android rechaza dibujar bitmaps con alpha que no están premultiplicados y aborta el inicio del renderizador.

Los atlas contienen canales numéricos además de color: convertirlos indiscriminadamente a datos premultiplicados puede alterar sus valores. `DreamPixels301.resizeInto` interpola cada canal por separado y `DreamSurfaceTextures` continúa subiendo RGBA explícito. Si el tamaño ya coincide, copia los píxeles sin interpolarlos. Los bitmaps decodificados se liberan en `finally`.

El filtro y la lectura solo se ejecutan cuando se cargan los materiales; no añaden trabajo a cada fotograma. Se conservan los tamaños de calidad y los límites de textura. Las imágenes artísticas que utilizan Canvas siguen decodificándose premultiplicadas; la densidad de nubes sigue su ruta de datos sin Canvas.

Para ejecutar las pruebas con Kotlin 1.9.24 y Java 17: primero `python tools/run_checks30.py` para crear el JAR de clases puras y después `python tools/run_texture_checks301.py`. Las variables KOTLINC, KOTLIN_STDLIB, DREAMCORE_CORE_JAR y DREAMCORE_VALIDATION_OUT permiten usar otras ubicaciones. Las pruebas del cargador usan ImageIO para los PNG reales y fronteras Android/GL registradas, no un teléfono emulado.
