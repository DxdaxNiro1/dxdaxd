# DreamCore 3.3 · Ecos y bruma

**63 mundos y 64 composiciones.** Esta entrega ordena los menús y destinos, incorpora sonido de proximidad, respiración reactiva, clima suave y una pesadilla urbana propia inspirada en las referencias visuales y sonoras aportadas. Conserva la partida, el capítulo del Umbral, sus finales y las funciones de las lunas.

## Dónde encontrar cada cosa

- **Inicio → Mundos** o **PAUSA → Mundos:** Dreamcore; Backrooms y espacios liminales; Dark Fantasy; Terror y pesadillas; Refugio y recuerdos. Cada mundo tiene una sola categoría. Puedes buscar por nombre dentro del grupo. La lista indica dónde estás y qué lugares visitaste.
- **PAUSA → Vídeo y rendimiento:** calidad, objetivo de FPS, energía y compatibilidad. El objetivo seleccionado puede diferir de los FPS medidos por calor o por el modo Fresco.
- **PAUSA → Sonido:** música, ambiente general, pasos, voces de presencias, respiración y clima. El ambiente general funciona como maestro de efectos; las otras capas se ajustan por separado.
- **PAUSA → Controles:** sensibilidad, tamaño y posiciones de botones.
- **PAUSA → Accesibilidad:** correr con un toque, reducción del movimiento, subtítulos y lluvia visible.
- **PAUSA → Diagnóstico:** panel opcional de FPS y memoria, datos gráficos e informe para compartir.
- **Presencias → archivo de observación:** nombres y lugares de los modelos nuevos, habitantes anteriores, acompañantes y lunas.
- **Sala de escucha:** inicio y músicas agrupadas con las mismas categorías de los mundos.

## El lugar nuevo

**Mundos → Terror y pesadillas → La ciudad que escucha llover.** También hay un portal en el Umbral, al lado izquierdo del recorrido de terror. La calle mojada desemboca en un corredor rojo y una plaza suspendida con manos de piedra. El centro tiene suelo continuo y barandas; las islas laterales son paisaje. Hay regreso junto a la entrada y al fondo.

El inquilino del turno vacío observa desde dos puntos del lugar. Su voz se escucha al acercarte; no se añade un combate ni una persecución a este mapa. La pradera de ojos continúa sin ataques ni respiración de amenaza.

## Actualizar

1. Extrae todo el ZIP. `Instalar_Windows.cmd` respalda el proyecto y copia fuentes y recursos.
2. Versión **34 / 3.3**, mínimo **API 19**, GLES2. Conserva el identificador y la firma anteriores para actualizar sin desinstalar ni borrar datos.
3. Para el proyecto completo, abre la carpeta con `settings.gradle.kts` en Android Studio, JDK 17 y SDK 34. Ejecuta `gradlew.bat :app:assembleDebug`.

Los SDK locales, carpetas de compilación y claves personales no se incluyen. El instalador se revisa estáticamente; no se ejecutó en Windows. Esta entrega es una actualización del juego, no una publicación ni aprobación en Google Play.

[Detalle de sonido y contenido](ECOS_Y_BRUMA_3_3.md) · [Vistas del motor](VISTAS.md) · [Validación](VALIDACION_3_3.json).

Las comprobaciones de escritorio y la compilación no sustituyen pruebas físicas de audio, fluidez o temperatura en Infinix, POCO y Redmi. El diagnóstico sigue disponible para recogerlas.
