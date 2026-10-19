# DreamCore 1.9 para Android Studio

Extrae todo. En Android Studio usa Open y abre la carpeta DreamCore que contiene settings.gradle.kts. Sincroniza Gradle e instala SDK 34 si se solicita; usa JDK 17 o compatible. Build APK(s) permite probar. Generate Signed Bundle / APK con tu misma clave e identificador permite actualizar tu instalación conservando los datos. No desinstales la app anterior.

Gradle 8.7, AGP 8.5.2, Kotlin 1.9.24, minSdk 21, compile/targetSdk 34, versionCode 19 y versionName 1.9. Android Studio crea su propio local.properties. No se incluye SDK, APK publicado, cachés ni claves privadas.

Lee DETALLES_1_9.md, VALIDACION.json y RECORRIDO_CON_SECRETOS.md. Se leen las partidas de la 1.8 y se conserva su progreso; los lugares remodelados tienen migración a una posición segura.
