# Pruebas 1.8

Los cinco ejecutables Kotlin son Journey18Check, CraterPhysicsCheck, RadialFallCheck, MoonTravel17Check (regresión lunar) y Ecos18Check. Compila todos los archivos del paquete sin imports Android junto con estos cinco tests, usando Kotlin 1.9.24 y kotlin-stdlib. Ejecuta cada clase `com.dreamcore.game.NombreKt`. `-Ddreamcore.output=/ruta/salida` dirige los volcados para renders a una carpeta elegida.

No son pruebas ejecutadas en un teléfono. Las fuentes GLES2 también se compilan/enlazan en Mesa, con fragmentos normales y mediump. El APK se compila con Gradle/SDK Android reales. Véase VALIDACION.json.
