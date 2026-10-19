# DreamCore 1.5 — Umbrales y lunas dormidas

Esta actualización organiza Dreamcore como una experiencia lineal, adapta el hoyo a la referencia verde y añade una experiencia Dark Fantasy independiente. Se mantienen los otros escenarios y sus músicas de la 1.4.

## Instalar sobre tu proyecto actual

1. Extrae todo el ZIP.
2. Ejecuta `Instalar_Windows.cmd` e indica la carpeta de tu proyecto abierto en Android Studio. El instalador respalda `app/src/main` en `DreamCore_backup_1_5_…` antes de cambiar archivos.
3. Compila y prueba con Android Studio, o ejecuta en su terminal:

```powershell
.\gradlew.bat assembleDebug
```

Instalación manual: copia **los 18 archivos Kotlin** de `app/src/main/java/com/dreamcore/game` del paquete a la ubicación de tus clases existentes. Conserva `PlayerState.kt`. Copia todos los recursos de `app/src/main/res/raw` y `app/src/main/res/drawable-nodpi` a sus carpetas correspondientes. No borres recursos originales ni crees una segunda copia de las clases en otro paquete.

El instalador se revisó pero no se ejecutó en Windows. La aplicación del mismo conjunto de archivos fue comprobada compilando un proyecto Android completo. Si prefieres empezar en una carpeta nueva, usa el ZIP **DreamCore_1_5_Proyecto_Completo.zip**, abre su carpeta DreamCore con Android Studio y sigue su LEEME.

Para restaurar, cierra el juego y Android Studio, mueve la carpeta modificada `app/src/main` a un respaldo y repón la carpeta `main` completa desde `DreamCore_backup_1_5_…`.

La pantalla del juego muestra 1.5. El parche conserva tu configuración Gradle; para actualizar también la información del APK puedes cambiar `versionCode` a 15 y `versionName` a "1.5" en `app/build.gradle.kts`. El proyecto completo ya incluye esos valores.

## Un inicio, dos experiencias

En **El Umbral**, el portal de la izquierda inicia **Dreamcore** y el de la derecha inicia **Dark Fantasy**. No hay selector de todos los mapas ni entradas directas que salten etapas.

### Recorrido Dreamcore

Cada entrada vuelve al mundo anterior y cada salida continúa al siguiente. La interfaz muestra el número de etapa, nombre y destino de la puerta.

1. Piscinas del recuerdo.
2. El abismo de las casas perdidas.
3. Túneles de agua y porcelana.
4. Jardín de la lluvia detenida.
5. Pradera del último sol.
6. Casa de los pasillos largos.
7. Acuario de las horas.
8. Ajedrez del cielo inmóvil.
9. Hotel sobre las nubes.
10. Carretera del último recuerdo.
11. Casa que mira hacia abajo: su puerta final devuelve al Umbral y muestra el cierre del recorrido.

Los cambios entre mapas se realizan con portales y el botón **ENTRAR**. El juego mantiene una sola malla de escenario activa, en lugar de unir todos los mapas en una malla enorme.

### Hoyo con anillos verdes

- Conserva 180 m de profundidad, paredes verticales y agua al fondo.
- Paredes verdes, 23 repisas circulares y casas más grandes, inspiradas en la imagen proporcionada.
- Las repisas sostienen al jugador al caer cerca de la pared. Puedes moverte hacia dentro y seguir bajando.
- La casa central conserva su entrada real y techo. **Su puerta continúa hacia los túneles**, siguiendo el nuevo recorrido.
- Al volver desde los túneles apareces en la isla, fuera de la puerta: no tienes que repetir la caída.
- Las casas flotantes y los fragmentos glitch son decorativos. La casa central sí tiene colisiones y acceso.

### Dark Fantasy: El reino de las lunas dormidas

Un mapa nuevo, separado del recorrido Dreamcore: calzada, jardines inundados con puentes, bosque de coníferas, figuras de piedra, obeliscos y catedral con torres. La puerta del inicio y el portal al fondo de la catedral vuelven al Umbral; este último muestra el cierre de la experiencia.

Dos lunas grandes permanecen ancladas a la dirección del mundo: una luna con rostro esculpido y otra en fase parcial. Se incluyen estrellas, niebla nocturna, luz violeta y reflejos aproximados de luna. La textura de rostro se generó de forma original mediante relieve procedural; no se extrajeron modelos o imágenes del video.

La música nueva de 48 segundos es una composición original de acordes menores, órgano sintético y campanas lejanas. No contiene la grabación del video. Las figuras son esculturas estáticas; esta versión todavía no incluye enemigos con IA ni multijugador.

## Comprobaciones y límites

Se compiló un APK real con Gradle, Android SDK y recursos completos. Además pasaron las comprobaciones de recorrido y regresión de física: 258 de navegación/mallas/repisas/Dark Fantasy, 36 de física y 480 de rutas radiales del hoyo. Los shaders enlazaron y los mapas se renderizaron en OpenGL ES2/Mesa. La música se verificó por duración, continuidad de bucle y ausencia de saturación.

`VALIDACION.json` registra los datos finales. Las imágenes de `vistas/` muestran los mapas, texturas y shaders reales en un render de escritorio sin controles táctiles.

Falta probar el APK y el audio real en tu teléfono. No se midieron FPS de tu dispositivo ni se ejecutó el instalador en Windows. Nubes, sombras y reflejos siguen siendo aproximaciones para móviles, no simulaciones completas. La posición se mantiene mientras vive la sesión; no se añadió guardado de progreso entre cierres del proceso.
