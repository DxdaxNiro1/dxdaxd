# DreamCore 3.4 · Umbral y furia

**63 mundos, 64 músicas y 18 encuentros hostiles con secuencias propias.**

- **PAUSA → Plano del Umbral:** esquema de los cinco caminos por números y colores.
- **Mundos:** categorías y búsqueda; verde Dreamcore, ámbar Backrooms, violeta Dark Fantasy, carmesí terror y azul biblioteca/tren/refugio.
- **Accesibilidad:** sangre, encuentros letales y reducción del movimiento.
- **Sonido → Voces:** regula también los gritos. Diagnóstico conserva su página separada.

Las presencias advierten antes del agarre. Retrocede para evitarlo. Después de morir puedes despertar en la entrada o volver al Umbral; conservas el progreso. Las cuatro figuras de la pradera reaccionan ahora si te acercas demasiado. Los acompañantes amistosos y Alma siguen siendo seguros.

## Instalar

1. Extrae el ZIP completo. Para actualizar tu proyecto existente en Windows, ejecuta `Instalar_Windows.cmd`: conserva el flujo de copia y respaldo.
2. Para abrir el proyecto completo, usa la carpeta que contiene `settings.gradle.kts` en Android Studio; JDK 17 y SDK 34. Compila con `gradlew.bat :app:assembleDebug`.
3. Versión **35 / 3.4**, paquete **com.dreamcore.game**, mínimo **Android 4.4 / API 19**, OpenGL ES2.
4. Actualiza sobre la aplicación anterior usando la misma firma para conservar sus datos. No necesitas iniciar una partida nueva.

El ZIP contiene fuentes y recursos, no un APK publicado en Google Play. No incluye SDK local, cachés, carpetas de compilación ni claves privadas. El instalador se comprueba estáticamente; no se ejecutó en Windows.

[Contenido y comportamiento](UMBRALES_Y_FURIA_3_4.md) · [Imágenes del motor](VISTAS.md) · [Validación](VALIDACION_3_4.json).

Compilación, Lint, comprobaciones JVM y renders GLES2 de escritorio aprobados. Falta comprobar interacción, reproducción, FPS y temperatura en teléfonos físicos.
