# Pruebas 1.9

Ejecutables: Journey19Check, CraterPhysicsCheck, RadialFallCheck, MoonTravel17Check (regresión), Ecos18Check (regresión adaptada al parque nuevo) y Horizons19Check. Compila con Kotlin 1.9.24 los archivos del paquete sin imports Android junto con los seis tests, y ejecuta cada clase com.dreamcore.game.NombreKt con kotlin-stdlib en el classpath. La propiedad -Ddreamcore.output=/ruta/salida dirige los volcados geométricos.

Incluyen geometría y normales, rutas completas y acceso a objetos, física y caída, evento lunar, guardado CRC/recuperación, consecuencias, finales, fotos con oclusión de triángulos reales, migración desde la 1.8, subida y regreso de la torre, compañero y autobús. El APK se compila también con SDK/Gradle reales. Los shaders se prueban en GLES2/Mesa normal y mediump. Véase VALIDACION.json; no son pruebas en teléfono.
