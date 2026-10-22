# DreamCore 3.0 · El último tren

Actualización sobre 2.9: **54 escenarios registrados, 55 composiciones**, nuevas acciones persistentes y lunas remodeladas. Las referencias guían el diseño; las imágenes de `vistas` son capturas renderizadas con las mallas y shaders del juego.

## Instalar en Android Studio

1. Extrae todo el ZIP. Cierra Android Studio y ejecuta `Instalar_Windows.cmd`, indicando la carpeta de tu proyecto.
2. El instalador respalda `app/src/main` y el Gradle, copia los archivos y aplica **versionCode 30 / versionName 3.0 / minSdk 19**. Conserva tu identificador y tu firma para mantener la partida.
3. Abre el proyecto y sincroniza. Compila con JDK 17, SDK 34 y `gradlew.bat :app:assembleDebug`.

El proyecto completo se abre por la carpeta que contiene `settings.gradle.kts`. No copies ni reemplaces tu archivo de firma. No es necesario borrar los datos del juego. Se mantiene el guardado con CRC, sus copias de recuperación y las decisiones anteriores.

Instalación manual: respalda tu proyecto, copia `app/src/main/java/com/dreamcore/game`, `res/raw`, `res/drawable-nodpi` y los temas `res/values/dream_theme_29.xml` y `res/values-v21/dream_theme_29.xml`. Aplica las versiones indicadas en el Gradle de la aplicación. El manifiesto debe usar `@style/DreamTheme29`; conserva sus identificadores y componentes. El instalador se detiene si encuentra un tema personalizado que requiere integración manual. Para restaurar, repón el código, recursos y Gradle desde la copia creada antes de instalar. El instalador se revisó en Linux; no se ejecutó en Windows.

## Empezar a descubrir la actualización

- En el **Umbral**, el andén del último tren está a la izquierda; la entrada de la biblioteca, a la derecha. También están disponibles en **Ajustes → Rutas**.
- Ve al **refugio** y recoge la tiza y la grabadora de la mesa izquierda. El botón **OBJETOS** permite usarlas y cerrar los ojos. La grabadora registra eventos sonoros del juego; **no necesita el micrófono**.
- Cuando el tren se detenga, usa el andén para subir. La tabla junto a la entrada del vagón permite escoger dónde bajar. Durante sus doce segundos de viaje puedes recorrerlo.
- Elige un billete en el refugio para descubrir una parada sin cartel. Solo puedes elegir un destino secreto por recorrido. Dormir permite probar el otro y conserva lo que descubriste.
- En la biblioteca hay libros, piano, proyector, maqueta, un armario móvil y marcos de perspectiva. En la feria encontrarás válvulas, una presencia que intercambia objetos, un carrusel y un telescopio. Las pistas de Ajustes ayudan si te atascas.

[Funciones y lugares](DETALLES_3_0.md) · [Recorrido con soluciones](RECORRIDO_3_0.md) · [Vistas reales](VISTAS.md) · [Rendimiento y límites](RENDIMIENTO_3_0.txt).

## Las lunas

Se reemplazó el atlas de rostros y se remodelaron el relieve, las cuencas, la boca y los dientes. La esfera tiene más profundidad; los ojos ya no dependen de recolocaciones distintas sobre cada textura. Se conservan el seguimiento de la mirada, los párpados, la sonrisa progresiva, el cambio de azul a rojo tras **60 segundos de observación** y la aparición gigante a los **120 segundos**, con su guardado y comprobación de visibilidad. El menú, la pausa y mirar a través de un techo cerrado no suman tiempo de observación.

## Compatibilidad y verificación

Se conserva el mínimo **Android 4.4 / API 19 con GLES2**, la selección automática estable para Adreno 619, los formatos RGBA8 y la alternativa esencial. No hay librerías nativas ni restricción a ARM de 64 bits. La instalación posible no garantiza fluidez en cualquier teléfono antiguo.

Se verifican la compilación Android, Android Lint, los sistemas de juego y guardado en JVM y los shaders y escenarios en GLES2 de Mesa. Los informes ejecutados están en `tools/validacion30` y `VALIDACION.json`. **No se midieron FPS ni temperatura en un teléfono físico**. La respuesta concreta del Infinix, POCO y Redmi todavía requiere probar el juego en esos equipos.

Los destinos sin cartel requieren el billete incluso entrando desde Rutas; esa opción te lleva al tren para elegir estación.
