# DreamCore 2.8 · Fluidez

Actualización del motor centrada en reducir trabajo y memoria. Se conservan los **51 escenarios**, las **52 músicas**, las texturas, los modelos, los recorridos y el guardado de 2.7. [Comparación visual del motor](VISTAS.md).

## Instalar en tu proyecto de Android Studio

1. Extrae TODO el ZIP y cierra Android Studio.
2. Ejecuta `Instalar_Windows.cmd` e indica tu carpeta de DreamCore.
3. El instalador respalda `app/src/main` y `app/build.gradle.kts`, copia el contenido y aplica **versionCode 28 / versionName 2.8**.
4. Abre Android Studio, sincroniza y compila `gradlew.bat :app:assembleDebug`.
5. Usa tu mismo identificador y firma para actualizar la instalación y conservar la partida.

Para hacerlo manualmente: respalda esas dos rutas, copia las carpetas completas `app/src/main/java/com/dreamcore/game`, `app/src/main/res/raw` y `app/src/main/res/drawable-nodpi`, y actualiza las dos versiones en el Gradle de la aplicación. Los nuevos archivos `DreamIndexed28`, `DreamGpuMesh28`, `DreamPrograms28`, `DreamShader28`, `DreamVisibility28` y `DreamCache28` son necesarios.

El proyecto completo se abre desde la carpeta con `settings.gradle.kts`. Requiere JDK 17, SDK 34, Android mínimo 21 y GLES2. La sugerencia de frecuencia de pantalla se usa desde Android 11 y el sistema decide si la acepta. Los paquetes no incluyen rutas locales del SDK, claves privadas ni compilaciones. El instalador de Windows se revisó por código en Linux; no se ejecutó en Windows.

Para restaurar, cierra Android Studio y repón `main` y el Gradle desde la copia que muestra el instalador.

## Qué se optimizó

- **Geometría compartida:** los vértices idénticos se guardan una sola vez. Se mantienen todos los triángulos, coordenadas, normales y UV originales, sin reducir la resolución de las texturas ni simplificar modelos. Las páginas usan índices de 16 bits compatibles con GLES2.
- **Sombras:** cada malla prepara una lista de las superficies que pueden proyectarlas. Las superficies que el shader anterior descartaba dejan de enviarse a esa pasada. Las hojas mantienen su recorte y las sombras conservan su resolución.
- **Nubes ocultas o fuera de vista:** el motor evita pasadas que no se usan en esos cielos y comprueba si la cámara puede ver una capa de nubes. Cuando hay profundidad de alta precisión, también evita recorrer volumen que queda completamente tapado por el escenario, protegiendo los bordes del cielo al ampliar la imagen. Las nubes visibles conservan densidad, texturas y muestras.
- **Programas por mundo:** el controlador puede eliminar ramas que nunca se utilizan en ese escenario. Se mantienen dos parejas recientes en caché y los programas generales compatibles como alternativa.
- **Menos llamadas y objetos por fotograma:** se reutiliza el resultado de visibilidad entre las fases opaca y transparente, se guardan las ubicaciones de los parámetros gráficos y se evita subir dos veces los mismos parámetros de la escena.
- **Memoria entre mundos:** se liberan búferes y árboles de consultas del mapa anterior antes de construir el siguiente; las cachés de otras familias de escenarios dejan de retener sus mallas grandes. La subida a la GPU reutiliza un bloque nativo de como máximo 4 MiB.
- **Lunas y figuras:** las lunas también reutilizan vértices, dejan de enviarse cuando quedan completamente fuera de cámara y cargan sus recursos al entrar en Dark Fantasy. Sus relojes siguen actualizándose. Los materiales especiales de ojos/plumas se cargan en sus mundos. Los marcos del refugio liberan su textura al salir y vuelven a cargar las fotografías guardadas al regresar.
- **Cadencia de pantalla:** Android recibe una sugerencia acorde a los FPS que ya elegiste, para facilitar una frecuencia compatible. Se conservan tus ajustes de calidad, volumen, FPS y la respuesta térmica anterior.

## Resultados y límites

Al medir los búferes de cada uno de los 51 escenarios por separado, la suma pasa de **267.258.000 a 152.687.186 bytes**, una reducción del **42,9 %**. Es memoria de mallas; no representa la RAM total de la aplicación ni el ahorro de batería. El juego sigue cargando un solo mundo a la vez.

Las 51 geometrías generadas coinciden byte por byte con 2.7. Los 148 archivos de recursos también coinciden. Se comparan 52 pares de vistas del catálogo y 26 pares adicionales de calidad, estados lunares, objetos, tamaños impares y alternativas gráficas. Las pequeñas diferencias de redondeo del controlador no cambian el aspecto; las métricas completas se incluyen en `tools/validacion28`.

Las mediciones de tiempo usan OpenGL ES 2.0 sobre **Mesa/llvmpipe de software**, con cámaras y resolución iguales. Sirven para comparar el trabajo de las dos versiones en esa referencia; **no son FPS ni temperaturas medidos en tu teléfono**. La prueba de cachés también es una medición JVM de escritorio.

Para tu Infinix NOTE 50s se conserva el perfil Media a 30 FPS que ya usabas. El comportamiento térmico real y las sesiones largas todavía deben comprobarse en el dispositivo. No se puede prometer que deje de calentarse ni compatibilidad universal basándose en estas pruebas.
