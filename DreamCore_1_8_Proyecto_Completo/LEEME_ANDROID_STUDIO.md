# Abrir DreamCore 1.8

Extrae todo el ZIP. En Android Studio selecciona **Open** y abre esta carpeta **DreamCore**, la que contiene `settings.gradle.kts`. Sincroniza Gradle, instala SDK 34 si se solicita y usa JDK 17 o compatible. Compila con **Build APK(s)** para probar. Para actualizar la instalación de tu teléfono debes usar el mismo identificador y la misma clave de firma de tu app anterior; para publicar usa **Generate Signed Bundle / APK**.

No copies `local.properties` de otra computadora; Android Studio crea su ruta al SDK. Esta versión usa Gradle 8.7, AGP 8.5.2, Kotlin 1.9.24, minSdk 21, target/compileSdk 34 y versión 18/1.8. La firma de depuración que genera tu equipo sirve para pruebas, no sustituye tu clave de publicación.

Lee `DETALLES_1_8.md` y `RECORRIDO_CON_SECRETOS.md`. Los datos de guardado empiezan con la 1.8; conservar la instalación al actualizar mantiene esos datos. No se incluyen APK, SDK ni claves privadas.
