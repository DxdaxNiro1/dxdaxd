# DreamCore 3.2 · La memoria del Umbral

**62 mundos y 63 composiciones.** La actualización une lugares actuales en un capítulo con tres propósitos, decisiones persistentes, la archivista Alma y un nuevo depósito con una persecución preparada, dos corredores y dos escondites. Las historias anteriores, sus finales, el guardado y el campo de ojos sin ataques se conservan.

Para empezar: **Ajustes → Capítulo** o **OBJETOS → La memoria del Umbral**. También puedes acercarte al reloj que está a la izquierda al llegar al Umbral. Elige buscar a Alma, reconstruir un recuerdo o encontrar una salida. El diario señala el siguiente testimonio; “Seguir la pista del capítulo” permite viajar directamente si quieres concentrarte en la historia. Puedes continuar explorando los mapas por sus portales normales.

[Las 30 propuestas y dónde están](DETALLES_3_2.md) · [Recorrido del capítulo](RECORRIDO_3_2.md) · [Vistas del motor](VISTAS.md) · [Rendimiento y medición en tu celular](RENDIMIENTO_3_2.md) · [Resultados verificables](VALIDACION_3_2.json).

## Actualizar conservando la partida

1. Extrae todo el ZIP y cierra Android Studio. `Instalar_Windows.cmd` respalda las fuentes, recursos y Gradle antes de actualizar tu proyecto. También puedes usar el proyecto completo.
2. Esta entrega usa **versionCode 33 / versionName 3.2 / minSdk 19**. Conserva el identificador y la firma de tu aplicación existente para mantener sus datos. No desinstales el juego para actualizar.
3. Abre la carpeta con `settings.gradle.kts` en Android Studio, JDK 17 y SDK 34. Ejecuta `gradlew.bat :app:assembleDebug` y actualiza la aplicación con el APK resultante.

La actualización de fuentes contiene `app/src/main/java/com/dreamcore/game`, recursos y temas compatibles. El proyecto completo añade manifiesto y Gradle Wrapper. El SDK local y las claves personales de firma no se incluyen. El instalador se revisó estáticamente en Linux; no se ejecutó en Windows.

Se conservan GLES2, los perfiles Estable y Esencial y la corrección del error de bitmap sin premultiplicar. La carga progresiva empieza con un atlas completo válido y refina sus materiales en una cola limitada; no retira texturas ni mapas para conseguir el resultado.

Las pruebas automatizadas y los renders de esta entrega se hicieron en el entorno de desarrollo. **No se dispuso físicamente del Infinix NOTE 50s, POCO X4 Pro 5G ni Redmi**, por lo que todavía no hay resultados de temperatura o FPS de esos teléfonos ni garantía de ausencia de fallos. Ahora el juego permite medir y compartir esos datos desde Ajustes → Diagnóstico.
