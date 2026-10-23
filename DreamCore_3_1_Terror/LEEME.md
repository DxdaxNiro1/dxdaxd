# DreamCore 3.1 · Los testigos

Actualización de motor, materiales y encuentros inspirados en **nueve videos accesibles**. El archivo `WhatsApp Video 2026-10-22 at 19.12.04.mp4` queda excluido por indicación del autor. No se incluyen copias de los videos ni sus audios.

**61 escenarios, 62 composiciones originales.** Los 54 lugares anteriores conservan sus rutas y sistemas. Siete lugares nuevos tienen portales físicos, nombres, regreso y acceso en **Ajustes → Rutas**. En el Umbral también aparece el portal **TESTIGOS**.

- Superficies comunes con programas compactos de vértices y materiales; agua, portales, personajes y efectos conservan sus rutas especializadas. Nuevas texturas compartidas de materiales y tratamiento de luz/color en todos los escenarios.
- Preparación de mallas fuera del hilo de dibujo, cola limitada y residencia que espera solo geometría cercana. Follaje con recorte en lugar de hojas elipsoidales; normales, sombras filtradas, reflejos y funciones anteriores conservados.
- Diez modelos nuevos: coro de túnicas, ángel enmascarado, testigo de alas con ojos, figura carmesí, figura de astas, guardián dorado, cabeza estrellada con traje, observatorio ocular, hongos con ojos y ojos del agua. Alas con plumas curvas, máscaras esculpidas y vestidos con pliegues en la geometría, usando un atlas original de materiales.
- Encuentros escasos, mirada dirigida, movimiento sutil y un observador que se aproxima cuando apartas la vista. El diario recuerda los encuentros. La pradera de ojos sigue **sin ataques**.
- Oficina con acabado de suelo y reflejos de luminarias, cine con techo e iluminación nuevos, costa con piscina y reflejo. Las lunas mantienen parpadeo, mirada, rojo al minuto y aparición gigante a los dos minutos.

## Instalar en tu proyecto

1. Extrae todo el ZIP. Ejecuta `Instalar_Windows.cmd` y elige la carpeta de tu proyecto. El instalador respalda código, recursos y Gradle antes de copiar.
2. Se aplica **versionCode 32 / versionName 3.1 / minSdk 19**. Conserva el identificador de aplicación y la firma de tu instalación anterior para actualizar y mantener el guardado.
3. Abre Android Studio con JDK 17 y SDK 34. Sincroniza y compila `gradlew.bat :app:assembleDebug`.

El proyecto completo se abre desde la carpeta que contiene `settings.gradle.kts`. Para integrar manualmente, respalda y copia `app/src/main/java/com/dreamcore/game`, `res/raw`, `res/drawable-nodpi` y los temas `dream_theme_29.xml` de `values` y `values-v21`. Conserva el manifiesto compatible `@style/DreamTheme29` y tus identificadores. Para restaurar, repón las rutas y Gradle de la copia. El instalador se revisa en Linux; no se ejecutó en Windows.

[Recorrido y referencias](REFERENCIAS_3_1.md) · [Vistas reales](VISTAS.md) · [Rendimiento y alcance de las pruebas](RENDIMIENTO_3_1.md) · [Validación](VALIDACION_3_1.json).

No hay mediciones físicas de FPS o temperatura en Infinix, POCO o Redmi. Las imágenes de esta entrega son renders del código del juego mediante GLES2/Mesa; las portadas ilustradas anteriores no son capturas del juego. Los modelos son recreaciones propias inspiradas en las referencias, no extracciones de sus modelos originales. El cambio conserva GLES2, API 19, los perfiles compatibles y la corrección del error de bitmap sin premultiplicar.
