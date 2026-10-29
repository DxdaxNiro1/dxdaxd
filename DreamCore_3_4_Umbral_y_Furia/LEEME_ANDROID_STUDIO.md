# Abrir DreamCore 3.4 en Android Studio

Extrae `DreamCore_3_4_Proyecto_Completo.zip` y abre la carpeta con `settings.gradle.kts`. Usa JDK 17 y Android SDK 34; el proyecto configura su Gradle Wrapper. Android Studio debe disponer de conexión durante la primera descarga de dependencias.

Versión 35 / 3.4, com.dreamcore.game, minSdk 19. Ejecuta `gradlew.bat :app:assembleDebug` en Windows o `./gradlew :app:assembleDebug` en Linux/macOS. Para comprobar Lint: `:app:lintDebug`.

El SDK, local.properties y claves personales no se distribuyen. Si actualizas una instalación anterior, conserva tu firma y el identificador de aplicación. La compilación de desarrollo local no es una publicación en Google Play.

Lee LEEME.md y UMBRALES_Y_FURIA_3_4.md para orientación, ataques y ajustes.
