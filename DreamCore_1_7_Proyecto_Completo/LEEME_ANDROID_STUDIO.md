# DreamCore 1.7 — Proyecto completo

Extrae todo el ZIP en una carpeta nueva. Android Studio → Open → selecciona **DreamCore**, la carpeta que contiene `settings.gradle.kts` y `app`.

Usa JDK 17 o 21. Si Android Studio lo solicita, instala Android SDK Platform 34 y Build Tools 34.0.0; sincroniza y ejecuta Run en el teléfono o `gradlew.bat assembleDebug`. La APK aparece en `app/build/outputs/apk/debug/app-debug.apk`.

Gradle 8.7, AGP 8.5.2, Kotlin 1.9.24, Android 5.0/API21 mínimo y GLES2. Las fuentes y recursos están completos. No incluye la ruta SDK de otra PC, claves, cachés ni un APK compilado. Conserva tu proyecto y claves anteriores si necesitas actualizar una aplicación instalada con la misma firma.

Lee **DETALLES_1_7.md** para carrera rápida, ajuste de pasos, lunas animadas, la aparición tras 120 segundos y los nueve recuerdos. Consulta VALIDACION.json y tests/LEEME.md para las comprobaciones y pruebas pendientes en móvil/Windows.
