# DreamCore 3.0.1 · Corrección de texturas

Corrige el error de inicio **“Canvas: trying to use a non-premultiplied bitmap”** mostrado en la captura de la versión 2.9. La misma ruta seguía presente en 3.0.

El problema aparecía cuando era necesario redimensionar los atlas de materiales o vegetación. Se decodificaban con canales sin premultiplicar y `Bitmap.createScaledBitmap` intentaba dibujarlos mediante Canvas, que rechaza esa combinación.

Ahora el cargador obtiene los píxeles y aplica un filtro bilineal independiente a RGBA, sin dibujar estos atlas con Canvas. Se conservan los datos de color bajo transparencia, los normales y la altura usados por los materiales. No se cambian imágenes, músicas, shaders, mapas ni decisiones de la partida. El menú y la pantalla de juego muestran 3.0.1.

## Instalar

1. Extrae todo el ZIP y cierra Android Studio.
2. Ejecuta `Instalar_Windows.cmd` y elige tu carpeta de proyecto. Se respaldan el código, los recursos y el Gradle antes de copiar la actualización.
3. Se aplica **versionCode 31 / versionName 3.0.1 / minSdk 19**. Conserva tu identificador y la firma de la instalación anterior para actualizar sin borrar la partida.
4. Abre el proyecto, sincroniza y compila `gradlew.bat :app:assembleDebug`, con JDK 17 y SDK 34.

El proyecto completo se abre por la carpeta que contiene `settings.gradle.kts`. Para instalar manualmente, respalda tu proyecto y copia el código de `app/src/main/java/com/dreamcore/game`, `res/raw`, `res/drawable-nodpi` y los dos temas `dream_theme_29.xml` en `values` y `values-v21`. Aplica las versiones indicadas; el manifiesto debe usar el tema compatible `@style/DreamTheme29`, conservando identificadores y componentes. Un tema personalizado requiere integración manual. Para restaurar, repón las rutas respaldadas. El instalador se revisó en Linux; no se ejecutó en Windows.

Incluye todo lo publicado en 3.0: **54 escenarios, 55 músicas**, tren, objetos móviles, piano, grabadora, nuevos secretos y lunas remodeladas con sus funciones anteriores. [Funciones](DETALLES_3_0.md) · [Recorrido](RECORRIDO_3_0.md) · [Vistas del motor](VISTAS.md).

## Verificación

- Pruebas de interpolación y conservación de canales, incluyendo color bajo alpha cero y comparación con una referencia independiente.
- Ejecución del cargador real con ambos atlas PNG, tres calidades y contratos API 19/34. La frontera Bitmap/Canvas simula el rechazo comunicado; la frontera GL comprueba los bytes enviados y los datos del mapa de normales. No es una ejecución de Android nativo en un teléfono.
- Compilación Android, Android Lint y firma compatible con API 19. Recursos y shaders anteriores conservados byte por byte.

Informes nuevos en `tools/validacion301` y `VALIDACION_3_0_1.json`. `VALIDACION.json` y `tools/validacion30` conservan las pruebas previas de la actualización 3.0; no se presentan como mediciones nuevas en un teléfono.

No se ha probado físicamente este arreglo en el POCO de la captura. Se elimina la llamada que provoca el error señalado, conservando los perfiles gráficos Estable y Esencial y el mínimo Android 4.4 / GLES2.
