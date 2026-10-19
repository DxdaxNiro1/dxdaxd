# DreamCore 1.5 — Proyecto completo

1. Extrae todo el ZIP en una carpeta nueva.
2. En Android Studio pulsa **Open / Abrir** y selecciona la carpeta **DreamCore**, donde está `settings.gradle.kts`. No abras solo app.
3. Espera la sincronización. Si Android Studio solicita Android SDK Platform 34 o Build Tools 34.0.0, instálalos con SDK Manager. Usa JDK 17 o 21 para Gradle.
4. Conecta el teléfono y pulsa Run. También puedes ejecutar `.\gradlew.bat assembleDebug`; el APK suele quedar en `app/build/outputs/apk/debug/app-debug.apk`.

Este proyecto ya contiene toda la versión 1.5. Al iniciar, el portal izquierdo abre el recorrido Dreamcore y el derecho Dark Fantasy. No necesita aplicar primero los paquetes 1.3 o 1.4.

Incluye Gradle y wrapper, manifiesto, todos los Kotlin, recursos originales, músicas y texturas nuevas. Android Studio genera `local.properties` con la ruta del SDK de tu computadora. No se incluyen SDK, cachés, archivos build ni claves de firma.

Se generó un APK 1.5 y se verificó su firma en Linux. Falta probarlo en tu teléfono; no se ejecutó Android Studio para Windows en este entorno. Consulta DETALLES_1_5.md para conocer el recorrido y las limitaciones.
