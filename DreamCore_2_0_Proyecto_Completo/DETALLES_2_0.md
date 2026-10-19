# DreamCore 2.0 — Los lugares que te recuerdan

## Instalar sobre tu juego

Extrae TODO el ZIP, cierra el juego y ejecuta `Instalar_Windows.cmd`. Indica la carpeta de tu proyecto, por ejemplo `C:\Users\Servidor Dxdax\AndroidStudioProjects\DreamCore`. El instalador respalda `app/src/main` y `app/build.gradle.kts` antes de copiar Kotlin, músicas y texturas; actualiza a versionCode 20 / versionName 2.0. Abre el proyecto en Android Studio y compila. No desinstales la aplicación: conserva el mismo identificador y tu misma clave de firma para actualizar sin perder datos.

También puedes abrir el proyecto completo: extrae y abre la carpeta **DreamCore**, donde está `settings.gradle.kts`. Android Studio creará `local.properties` para tu SDK. Herramientas: SDK 34, JDK 17 o compatible, Gradle 8.7, AGP 8.5.2 y Kotlin 1.9.24; Android mínimo 5.0 / API 21.

Instalación manual: copia todos los `.kt` a la carpeta que contiene `DreamRenderer.kt` y `MainActivity.kt`, y los recursos `res/raw` y `res/drawable-nodpi` conservando los anteriores. Ajusta las dos versiones en Gradle. Para restaurar, copia `main` y `build.gradle.kts` desde `DreamCore_backup_2_0_...`. No compiles tras una copia incompleta. El ZIP contiene fuentes, no un APK de distribución ni claves privadas.

## Lo nuevo

- **Refugio personal:** Ajustes → Ir a mi refugio. Habitación amueblada, cuatro marcos para fotos, reloj cuando reúnes tres recuerdos, teléfono tras una llamada y pequeñas señales de otra visita al regresar. La puerta luminosa devuelve al punto exacto de entrada. Desde aquí puedes entrar a la zona de gravedad.
- **Fotografías transitables:** toma fotos NUEVAS en Pueblo, Mar del cielo, Hotel o Tobogán de planetas. En Ajustes → Álbum elige una y pulsa Entrar al recuerdo. Visitas una versión quieta de ese mundo, con una presencia o detalle cambiado y una puerta de regreso. Otras fotos siguen siendo fotografías normales; las de versiones anteriores se pueden ver y colocar, pero carecen del dato de mundo para entrar. Son cuatro variaciones prediseñadas del lugar fotografiado, no reconstrucciones de cada píxel de una foto.
- **Tus fotos en casa:** Álbum → Colocar en refugio → Marco 1–4. La copia del marco permanece aunque esa foto salga de las doce últimas del álbum. No necesita permiso de galería ni conexión.
- **Objetos transportables:** recoge linterna, brújula y prisma en la mesa del refugio. Cambia el que llevas en Ajustes → Objetos. La linterna añade luz cálida y revela una puerta oculta en el pueblo; la brújula muestra dirección/distancia y emite una señal suave hacia cartas, pistas fotográficas o el siguiente umbral; el prisma transforma la vista de las ventanas. Se conserva la propiedad y el objeto equipado al cerrar.
- **Cartero sin rostro:** espera en el pueblo. Entrega tres cartas en orden, con destinatarios en Mar del cielo, Hotel y Tobogán de planetas. La primera respuesta abre una casa opcional con una mesa y una nota. Habla otra vez con el cartero después de cada entrega. Son encuentros tranquilos, sin combate.
- **Gravedad diferente:** un camino de unos 170 metros se curva suavemente hasta una pared vertical. La posición física de cámara y su orientación siguen la superficie; saltas según la normal local. El suelo anterior y sus casas quedan abajo. El paso se limita aquí para que el giro sea gradual; hay salida en ambos extremos.
- **La criatura del mar suspendido:** un animal enorme con cuerpo, cabeza, aletas y cola cruza lentamente bajo el agua del cielo. Deja una sombra suave en el piso y un sonido grave si está cerca. No persigue ni causa daño.
- **Piscina renovada:** bóvedas curvas, nuevas ventanas, claraboyas, detalles cerámicos y pequeños cursos de agua. Se conserva el puente y sus accesos.
- **Abismo variable:** casas laterales con ángulo, altura, tamaño y estilo derivados de la semilla guardada. La distribución es estable para tu partida y distinta entre semillas; no se recoloca mientras caminas. Continúan las terrazas, caída, isla y puerta central.
- **Cielos sin la constelación:** se retiró el dibujo de la casa. Se conservan estrellas, lunas y planetas con texturas y rotación; cada etapa principal recibe un móvil elevado de formas, escala y color propios. Se mueven los adornos y cuerpos celestes, sin desplazar el suelo habitual ni sus colisiones.
- **Sonido:** siete temas originales suaves y diferentes para refugio, cartas, gravedad y cuatro recuerdos. Una nueva llamada grave acompaña a la criatura. Conserva música anterior, brisa, eco, pasos y volúmenes separados.

## Rendimiento para tu Infinix NOTE 50s

El modo inicial es **30 FPS**, con una opción de **60 FPS** en Ajustes. Se conservan las texturas y la iluminación del nivel de calidad elegido. Cuando Android 10+ avisa de calor elevado, el objetivo baja a 24 FPS y vuelve al elegido al desaparecer esa señal; si el teléfono no publica el estado térmico, sigue el límite manual.

El motor ya no dibuja continuamente: solicita cuadros según el límite, para al abrir menús y al enviar la app al fondo. Descarta bloques fuera de la vista en la escena, los reflejos y las sombras. Las superficies opacas se dibujan antes del cielo para evitar calcular nubes y planetas detrás de las paredes. La transparencia se aplica por separado. Las sombras estáticas se reutilizan hasta desplazarte y los espejos se actualizan menos cuando no mueves la cámara. Una jerarquía de triángulos acelera las comprobaciones de visibilidad.

Estas medidas reducen trabajo y no prometen una temperatura o autonomía concretas. En la nube se comprobaron compilación, reglas y renders GLES2; falta medir calentamiento, FPS, sonido y controles en tu teléfono y ejecutar el instalador en Windows. Los gráficos siguen siendo estilizados y el motor GLES2 propio.

## Partida y recorrido

La 2.0 lee guardados 1.8 y 1.9 con el mismo almacenamiento y conserva recuerdos, decisiones, fotos y estado lunar. También guarda la pila de salidas para volver correctamente si visitas refugio/foto/zona secreta desde otro sueño. El límite es ocho puertas anidadas; al alcanzarlo te pide volver antes de entrar en otra, sin perder la salida.

Permanecen las trece etapas Dreamcore, los dos reinos Dark Fantasy y los cinco secretos anteriores, más siete destinos opcionales de esta versión. El evento de la luna gigante sigue necesitando **120 segundos de mirada efectiva**, con dos segundos de margen al apartarte; menús y pausa no suman tiempo. El progreso se guarda periódicamente, al interactuar y al pausar. Borrar datos o desinstalar elimina partida, álbum y marcos.

Consulta `RECORRIDO_CON_SECRETOS.md` para ubicaciones y `VALIDACION.json` para pruebas. Las vistas incluidas se renderizaron con la geometría y los shaders del juego en Mesa/GLES2, sin HUD; no son capturas de un teléfono.
