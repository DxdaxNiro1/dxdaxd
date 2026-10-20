# DreamCore 2.5 · Luz de los sueños

Primera mejora del renderizado tomando las portadas como dirección artística. Materiales con más presencia, iluminación por superficie, escalas más creíbles y agua más tranquila. [Comparar vistas del motor](VISTAS.md).

## Instalar en tu proyecto Android Studio

1. Extrae todo el ZIP de actualización y cierra Android Studio.
2. Ejecuta **Instalar_Windows.cmd** e indica la carpeta de tu proyecto DreamCore. El instalador crea una copia de `app/src/main` y `app/build.gradle.kts` antes de copiar.
3. Abre tu proyecto, sincroniza y compila con `gradlew.bat :app:assembleDebug`.
4. Usa tu misma firma e identificador al instalar para conservar la partida. Conserva la instalación anterior.

El instalador actualiza a **versionCode 25 / versionName 2.5**. Si prefieres instalación manual, haz esa copia de seguridad, copia `app/src/main/java/com/dreamcore/game`, `res/raw` y `res/drawable-nodpi` sobre tu proyecto y ajusta esas dos versiones en `app/build.gradle.kts`. La configuración de firma sigue siendo la de tu proyecto.

Para volver atrás, cierra Android Studio y restaura `main` y el Gradle de la copia de seguridad. El instalador se revisó por código; este entorno Linux no permite ejecutarlo en Windows.

El proyecto completo se abre directamente por la carpeta que contiene `settings.gradle.kts`. Configura tu SDK con Android Studio; el paquete no incluye rutas locales ni claves privadas. SDK 34, Android mínimo 21, Java 17 compatible y GLES2.

## Cambios visuales

- Atlas de dieciséis materiales renovado: hierba, estuco, piedra, madera, mosaico, cerámica, mármol, tela, hormigón, suelo, roca lunar y metal.
- La luz de los materiales se calcula una vez por superficie con su relieve y rugosidad; los blancos tienen una curva suave para conservar detalle.
- Fibras de alfombra y juntas de piscina a escalas más pequeñas. La tela lejana usa su color medio para evitar rejillas, brillo y ruido de muestreo; conserva el tono verde azulado.
- Luces locales en pasillo de sillas, atrio, oficina y Backrooms, alineadas con las lámparas existentes. Son luces sin sombras locales de objetos; las sombras direccionales exteriores siguen disponibles.
- Agua de las piscinas con ondas más suaves y reflejo aproximado del cielo. Los reflejos planos existentes se conservan donde ya estaban.
- Nubes con más diferencia entre núcleos en sombra y bordes iluminados, en dos capas que se mueven por separado.

Las portadas siguen siendo la referencia artística: esta versión mejora los materiales y la luz del motor actual. Alcanzar aquel acabado también requiere más modelado, decoración e iluminación de los escenarios. [VISTAS.md](VISTAS.md) muestra el resultado real del shader, sin sustituirlo por ilustraciones.

## Contenido y rendimiento

Se mantienen los 48 escenarios registrados, las 49 músicas, la sala de escucha, las decisiones y el guardado de esquema 23. Las canciones no se recomponen; el álbum y los videos de NiroDxdax se descargan por separado desde el repositorio.

Bajo y equilibrado conservan atlas de 512 y 1024. Alta permite atlas de 2048 y usa más memoria; la textura fuente tiene 1254 × 1254 píxeles. Se mantienen los límites de FPS y la respuesta a señales térmicas de Android. [Presupuesto y pruebas](RENDIMIENTO_2_5.txt). No se han medido temperatura, autonomía ni FPS en un teléfono físico.

## Validación

El APK se compila y verifica por firma. Las vistas se dibujan con los shaders GLES2, mallas del juego y texturas de esta entrega en Mesa; se revisan los 49 puntos del catálogo, ocho casos de precisión mediump/calidad baja y un caso en calidad alta. Las regresiones de lógica se describen en `VALIDACION.json`. No se ha probado la interacción táctil de esta versión en un dispositivo Android.

Para repetir el renderizado: `python tools/run_core_checks_24.py`, y luego `python tools/render_graphics_25.py --meshes build/validation24 --output build/graphics25`. Se requieren Kotlin 1.9.24, Java compatible, Python con numpy/Pillow y EGL/GLES2. Las herramientas de desarrollo no se ejecutan al jugar.
