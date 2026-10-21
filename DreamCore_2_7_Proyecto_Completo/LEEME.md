# DreamCore 2.7 · Vigilia

Lunas con volumen y rostros inspirados en tus referencias, cuatro criaturas nuevas descansando en la pradera de ojos, dos mundos de Dark Fantasy y una isla entre nubes sin tobogán. [Imágenes reales del motor](VISTAS.md).

## Instalar en tu proyecto

1. Extrae el ZIP completo y cierra Android Studio.
2. Ejecuta `Instalar_Windows.cmd` e indica la carpeta de tu proyecto DreamCore.
3. El instalador respalda `app/src/main` y `app/build.gradle.kts`, copia las fuentes y los recursos y aplica **versionCode 27 / versionName 2.7**.
4. Abre Android Studio, sincroniza y compila `gradlew.bat :app:assembleDebug`.
5. Utiliza tu mismo identificador y firma para actualizar la instalación conservando la partida.

Para hacerlo manualmente: respalda esas dos rutas, copia **todas** las carpetas `app/src/main/java/com/dreamcore/game`, `app/src/main/res/raw` y `app/src/main/res/drawable-nodpi` del ZIP y establece las versiones anteriores en el Gradle de tu aplicación. No sustituyas tu firma por otra.

El proyecto completo se abre desde la carpeta con `settings.gradle.kts`. Requiere JDK 17, SDK 34, Android mínimo 21 y OpenGL ES 2.0. Los paquetes no incluyen SDK local, claves ni compilaciones. El instalador de Windows se revisó por código en Linux; no se ejecutó en Windows. Para restaurar, cierra Android Studio y repón la carpeta `main` y el Gradle desde la copia que muestra el instalador.

## Qué hay y dónde entrar

- **Lunas:** caras azules, rojas, pálidas, de calavera y una creciente con perfil. Tienen superficie esculpida, cuencas, nariz, cavidad bucal, dientes, ojos y párpados separados. Conservan el parpadeo recurrente, la sonrisa y el seguimiento de la mirada. Las azules cambian a rojo y muestran una expresión más macabra al completar **60 segundos** de observación; a los **120 segundos** sigue apareciendo la luna gigante. Apartar la mirada más de dos segundos reinicia la cuenta de observación. Los menús, el juego en segundo plano y un techo que tape el cielo no suman tiempo.
- **Dark Fantasy III · El pueblo de la luna escarlata:** nieve, coníferas, casas encendidas y una capilla. Se llega desde el final de la catedral, o desde Ajustes → Rutas.
- **Dark Fantasy IV · El castillo de la sonrisa pálida:** torres rotas, arcos, árboles desnudos y estanques. Sigue al pueblo escarlata y su salida vuelve al Umbral. Los cuatro mapas oscuros usan combinaciones distintas de lunas.
- **El santuario del ojo entre nubes:** una casa rosa con interior, ventanas, cama y jardín, árboles y sendero sobre una isla suspendida. **Sin tobogán.** Un observador blanco de seis alas, ojo central, plumas modeladas y máscaras de marfil permanece muy lejos, en el aire. Sus alas abarcan aproximadamente 170 metros. Para verlo completo, mira arriba desde un lateral de la casa. Entrada: nuevo portal con nombre en el Umbral, detrás del punto de llegada, o Ajustes → Rutas.
- **Pradera de ojos:** el vigilante de cabeza puntiaguda sentado, el árbol de un ojo, el caballero de traje con cabeza de ojo y la figura blanca de alas plegadas. Son cuatro modelos nuevos en reposo, con zonas de colisión; no persiguen ni atacan. El compañero rojo que ya existía se conserva.
- **Música:** se conservan sin cambiar los 49 audios anteriores y se añaden tres composiciones originales, una para cada mapa nuevo. La sala de escucha tiene 52 temas y miniaturas del propio motor para las incorporaciones.
- **Orientación y guardado:** destinos con nombre, rutas directas y pistas opcionales para los tres escenarios nuevos. El guardado sigue usando el esquema 23.

## Gráficos y rendimiento

Se conservan las sombras, reflejos, materiales, nubes y oclusión de contacto de 2.6. Los modelos lunares comparten dos mallas inmutables y un atlas; las criaturas forman parte de los bloques espaciales del mundo y se descartan cuando quedan fuera de vista. Las texturas se preparan antes de jugar y no se generan cada fotograma. Si falla la inicialización del renderizador lunar, queda disponible el cielo lunar compatible anterior.

Para tu **Infinix NOTE 50s**, empieza en **Media, 30 FPS**. Se mantiene la respuesta a los avisos térmicos de Android y el modo ahorro. El contenido nuevo también tiene coste: no se ha medido temperatura ni FPS en un teléfono físico. Son interpretaciones modeladas de las referencias; no reproducciones idénticas ni el mismo acabado de una ilustración prerenderizada.

Los resultados de compilación, recorridos, tiempos y renderizado están en `VALIDACION.json`; los presupuestos, en `RENDIMIENTO_2_7.txt`.
