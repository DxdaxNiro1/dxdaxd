# DreamCore 3.5 · Los tres horizontes

**66 mundos y 67 composiciones.** Tres escenarios nuevos con bordes abiertos, caída, viento y cierre de ojos para regresar arriba sin perder progreso.

## Entrar

- **Umbral → sector verde → Los tres horizontes**.
- **PAUSA → Mundos → Dreamcore:** Las puertas del último sol / La roca del océano infinito.
- **PAUSA → Mundos → Dark Fantasy:** Bajo el gigante carmesí.

Puedes asomarte, saltar y caer. Antes del agua o del fondo se cierran los ojos y reapareces en el último terreno firme. Las escaleras del atardecer se pueden subir. Las señales indican las puertas de regreso y los destinos.

## Actualizar o abrir el proyecto

1. Extrae el ZIP. `Instalar_Windows.cmd` conserva el flujo anterior de respaldo y copia de fuentes/recursos.
2. El proyecto completo se abre desde su `settings.gradle.kts` en Android Studio: **JDK 17, SDK 34**. Compila con `gradlew.bat :app:assembleDebug` o `./gradlew :app:assembleDebug`.
3. Versión **36 / 3.5**, paquete **com.dreamcore.game**, mínimo **API 19**, GLES2. Conserva la misma firma para actualizar tu instalación sin borrar los datos. No hace falta una partida nueva.
4. El aire de la caída se regula en **Sonido → Clima**. Los ajustes de sangre y encuentros de 3.4 siguen en Accesibilidad.

El ZIP contiene código y recursos, no un APK publicado en Google Play. No incluye SDK local, claves personales, cachés ni salidas de compilación. El instalador se revisa estáticamente; no se ejecutó en Windows.

[Contenido y límites visuales](LOS_TRES_HORIZONTES_3_5.md) · [Renders del motor](VISTAS.md) · [Pruebas](VALIDACION_3_5.json).

Falta verificar controles, audio, FPS y temperatura en teléfonos físicos. Las galerías técnicas de versiones antiguas siguen en el repositorio y sus paquetes anteriores; este ZIP incluye la galería de esta entrega y todos los recursos del juego.
