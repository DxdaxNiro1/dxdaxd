# DreamCore 1.6 — Proyecto completo

1. Extrae el ZIP en una carpeta nueva; contiene una sola carpeta `DreamCore`.
2. Android Studio → Open → selecciona **DreamCore**, donde están `settings.gradle.kts` y `app`.
3. Instala Android SDK Platform 34 y Build Tools 34.0.0 si Android Studio los solicita. Usa JDK 17 o 21 para Gradle. Sincroniza.
4. Conecta tu teléfono y pulsa Run, o ejecuta `gradlew.bat assembleDebug`. El APK de prueba aparece en `app/build/outputs/apk/debug/app-debug.apk`.

Gradle 8.7, Android Gradle Plugin 8.5.2, Kotlin 1.9.24. Android 5.0/API21 mínimo; GLES2. Incluye fuentes, recursos, manifiesto y wrapper. No incluye rutas SDK de otra PC, claves privadas, cachés ni APK compilado.

Si necesitas instalar sobre tu APK anterior, conserva su applicationId y firma. Usa tus propias claves para exportar una versión de distribución. Conserva tu proyecto previo como respaldo antes de cambiar de carpeta.

Lee DETALLES_1_6.md para conocer los mundos, la casa imposible, la presencia, controles de ruta y límites de la comprobación. Se ha compilado y verificado el APK en la nube; falta probar ejecución y rendimiento en tu teléfono.
