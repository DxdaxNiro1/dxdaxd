# DreamCore 1.3 — El abismo vuelve a casa

Actualización para el proyecto Android Studio/Kotlin DreamCore 1.1 o 1.2 compartido por el usuario. Es un paquete de código y recursos que se aplica a tu proyecto existente, no un proyecto Android independiente ni un APK ya compilado.

## Instalación en Windows

1. Descarga el ZIP y **extrae todo**. Conserva una copia de tu proyecto antes de actualizar.
2. Abre `Instalar_Windows.cmd`. Introduce la ruta de tu proyecto DreamCore. El instalador encuentra la carpeta que contiene `DreamWorlds.kt`, tanto si está en `java` como en `kotlin`.
3. Antes de cambiar archivos crea una copia completa de `app/src/main` dentro de `DreamCore_backup_1_3_…` en tu proyecto. Después copia los archivos Kotlin y dos sonidos nuevos. No elimina tus texturas ni las músicas de otros mundos.
4. Abre Android Studio y ejecuta en su terminal PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

El APK de depuración normalmente queda en `app/build/outputs/apk/debug/app-debug.apk`. Puedes instalarlo con Android Studio. Para distribución usa tu propia firma; conserva la clave de tu aplicación.

Si Windows bloquea ejecutar scripts o no deseas usar el instalador, utiliza la instalación manual siguiente. El instalador no ha sido ejecutado en Windows en este entorno; la instalación manual ofrece los mismos archivos.

### Instalación manual

Con copia de seguridad previa, copia **todos los `.kt` de `app/src/main/java/com/dreamcore/game/` del paquete** a la carpeta de tu proyecto que ya contiene `DreamWorlds.kt` y `MainActivity.kt`. Reemplaza los archivos con el mismo nombre. No crees otra carpeta de código duplicada si tu proyecto usa `kotlin/`.

Copia los dos `.ogg` de `app/src/main/res/raw/` a la misma ubicación de recursos de tu proyecto. No elimines los recursos anteriores.

Incluye los ocho archivos Kotlin de este paquete: `DreamWorlds`, `DreamRenderer`, `DreamCraterPhysics`, `DreamShaders`, `DreamEglConfigChooser`, `DreamTextures`, `DreamAudio` y `MainActivity`. **No copies solamente los dos archivos de la actualización 1.2**: ahora hay dependencias nuevas entre ellos. Conserva tu `PlayerState.kt` original.

### Volver a la versión anterior

Con Android Studio y el juego cerrados, mueve la carpeta actual `app/src/main` a otra ubicación como respaldo de la versión modificada. Copia `main` desde `DreamCore_backup_1_3_…` a `app/src/main`. Reemplaza la carpeta completa, pues mezclar helpers nuevos con clases antiguas puede producir errores de compilación. Así recuperas el código y recursos anteriores sin borrar tu respaldo.

## Qué cambia

- Caída con gravedad, velocidad máxima y pasos de física pequeños. Puedes caminar y saltar hacia el pozo de 180 m; no te teletransporta al fondo.
- Fondo con agua poco profunda y movimiento más lento al vadear. Orilla continua hacia la isla central.
- Casa central hueca con una puerta cálida iluminada: acércate por el lado del camino y pulsa **ENTRAR** para volver a la estación inicial. El techo soporta aterrizajes.
- Pueblo sobre el borde con sendero circular, casas suspendidas en repisas, fragmentos glitch y referencias luminosas de escala.
- Iluminación con luz direccional y ambiente de colores; sombras de contacto y proyección aproximadas precalculadas. No se implementan mapas de sombras dinámicos.
- Agua con reflejos de cielo aproximados, brillo y ondas; luz acuática cerca del fondo. No son reflejos exactos de todos los objetos.
- Menos ruido en materiales lejanos, mipmaps para el atlas compatible, mejor precisión de profundidad y niebla que depende de la distancia de la cámara.
- Nueva música ambiental original del cráter: 64 s de acordes, campanas suaves y reverberación; capa profunda de 32 s. Fundidos y pausa/reanudación corregidos.
- HUD 1.3, avisos de caída y más margen de cámara para mirar hacia el fondo.

Se conserva la geometría de los otros siete mundos. Sus filtros de textura y configuración de profundidad también aprovechan los cambios compartidos. Las casas suspendidas y sus repisas son decorativas; esta versión no incluye colisión ni recorrido dentro de cada casa flotante. La casa central sí tiene paredes, acceso y techo comprobados.

## Cómo probar

Entra desde la estación al cráter, llega al borde y déjate caer. La caída completa tarda unos siete segundos. Puedes dirigirla con el joystick; no hay daño por caída en este mapa. Al aterrizar, camina por el agua hacia la casa iluminada, sigue su entrada y pulsa ENTRAR. Debes reaparecer en la estación, junto al acceso al cráter.

Prueba también saltar en tierra, recorrer la orilla, intentar salir a través del muro del pozo (debe bloquearte), entrar en la casa del borde y regresar, visitar otro mundo y pausar/reanudar el juego. Revisa el volumen con auriculares y ajusta el volumen del dispositivo si hace falta.

## Validación y límites

Se compiló el código Kotlin contra una API Android real, con identificadores de recursos generados para esa comprobación. Los shaders compilaron y enlazaron en OpenGL ES 2.0 de Mesa y el mapa se renderizó con el atlas extraído del APK original. Pasaron 516 comprobaciones de jugabilidad de física/recorrido, pruebas de malla y pruebas simuladas del ciclo de audio. Se revisaron duración, saturación y unión de bucle de los OGG numéricamente.

El cráter tiene 31.610 triángulos frente a 46.092 en 1.2: un 31,42 % menos. Eso reduce geometría, pero **no establece los FPS de tu teléfono**.

No se generó ni instaló un APK en este entorno. No se ha probado la GPU, el audio real ni el instalador en tu Windows/teléfono. La calidad final debe comprobarse allí; las capturas de `vistas/` son renderizados de escritorio con el mismo mapa, shaders y atlas, sin la interfaz táctil. No se atribuye esta música a las pistas de los videos de referencia: es una composición original.
