# DreamCore 1.3 — Proyecto completo

Esta carpeta se puede abrir como proyecto de Android Studio. Se reconstruyó con el código Kotlin 1.1 que enviaste, los recursos recuperados de tu APK y los cambios de la actualización 1.3. Incluye todos los archivos de Gradle, su wrapper, el módulo app, manifiesto, código, texturas y audio. No es solo un parche.

## Abrir

1. Extrae todo el ZIP en una carpeta nueva para conservar tu proyecto actual.
2. En Android Studio, usa **Open / Abrir** y selecciona la carpeta **DreamCore**, que contiene `settings.gradle.kts`, `build.gradle.kts`, `gradlew.bat` y `app`. No abras solo `app` ni crees un proyecto nuevo vacío.
3. Deja que termine la sincronización de Gradle. Si Android Studio solicita Android SDK Platform 34 o Build Tools 34.0.0, instálalos con SDK Manager. Usa el JDK integrado 17 o 21 para Gradle.
4. Conecta tu teléfono y pulsa **Run** para instalar y probar la versión 1.3.

También puedes generar el APK desde la terminal PowerShell de Android Studio:

```powershell
.\gradlew.bat assembleDebug
```

El archivo normalmente queda en `app/build/outputs/apk/debug/app-debug.apk`.

No se incluye `local.properties`: Android Studio lo genera con la ruta del SDK de tu computadora. No copies el de otra máquina. Tampoco se incluyen cachés, carpetas build, SDK, claves de firma ni rutas de la máquina en nube.

## Qué contiene la 1.3

Ocho mapas originales, caída con gravedad en el abismo de 180 m, isla y casa central con puerta de vuelta a la estación, mejoras de iluminación y agua, sombras de contacto y música del cráter. Los cuatro mapas nuevos de ajedrez, túneles, hotel y carretera pertenecen a la actualización 1.4, que se instala después.

## Aplicar después la 1.4

1. Primero abre y prueba esta base 1.3.
2. Descarga y extrae completamente [DreamCore 1.4](https://github.com/DxdaxNiro1/dxdaxd/raw/refs/heads/main/DreamCore_1_4_Mundos.zip).
3. Ejecuta el `Instalar_Windows.cmd` del paquete 1.4 e introduce la ruta de **esta nueva carpeta DreamCore**. Si el instalador propone la ubicación de tu otro proyecto, escribe la ruta de esta copia.
4. El instalador respalda `app/src/main` y añade los 15 Kotlin, músicas y textura de nubes de la 1.4. Vuelve a compilar y ejecutar en Android Studio.

Tras aplicar el parche, la pantalla del juego muestra 1.4. La información de versión del APK se controla por separado en `app/build.gradle.kts`: si también deseas actualizarla, cambia `versionCode` a 14 y `versionName` a "1.4".

## Herramientas fijadas

Gradle 8.7, Android Gradle Plugin 8.5.2, Kotlin 1.9.24, compileSdk/targetSdk 34, minSdk 21 y bytecode Java/Kotlin 17. El wrapper verifica la distribución mediante SHA-256. La interfaz usa Android/OpenGL nativo y no necesita Compose ni Unity.

## Comprobaciones realizadas

Se compiló este proyecto completo con `:app:assembleDebug`: se generó un APK 1.3 y se verificó su firma. Después se aplicaron los archivos del paquete 1.4 a una copia del proyecto y también se compiló correctamente su APK, ajustando allí la información de versión a 1.4. Se comprobaron código, recursos, manifiesto, Gradle y el empaquetado real de ambos. No se ejecutaron en un teléfono ni se usó Android Studio para Windows en este entorno.

El ZIP entrega el proyecto fuente, sin APK compilado ni claves de firma de la máquina de validación.
