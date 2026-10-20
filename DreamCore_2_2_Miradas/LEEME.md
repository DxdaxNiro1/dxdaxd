# DreamCore 2.2 · Miradas

## Instalar sobre tu juego actual

Extrae TODO el ZIP y ejecuta **Instalar_Windows.cmd**. Indica tu carpeta `C:\Users\Servidor Dxdax\AndroidStudioProjects\DreamCore`. El instalador localiza la carpeta Kotlin, crea `DreamCore_backup_2_2_...`, copia fuentes y recursos y actualiza Gradle a **versionCode 22 / versionName 2.2**. Después abre Android Studio y compila el APK.

Para instalar encima de la app conservando la partida, usa el mismo identificador `com.dreamcore.game` y la misma clave de firma que antes. No desinstales la app. El proyecto completo se ofrece como alternativa: extrae y abre **DreamCore**, donde está `settings.gradle.kts`. Android Studio crea `local.properties` para tu SDK. Se necesitan SDK 34 y JDK 17 o compatible; Gradle 8.7, AGP 8.5.2, Kotlin 1.9.24, Android mínimo API 21.

Instalación manual: copia todos los `.kt` al lado de MainActivity.kt y DreamRenderer.kt; copia `res/raw` y `res/drawable-nodpi` conservando los recursos anteriores y ajusta ambas versiones de Gradle. Si la copia falla, no compiles: restaura `main` y `app/build.gradle.kts` desde la copia de seguridad. El instalador Windows no pudo ejecutarse en esta máquina Linux; se revisaron sus rutas, recursos y cambios de versión.

## Inicio y guardado

Ahora se abre una pantalla con **Continuar partida, Nueva partida y Ajustes**. Continuar está desactivado cuando no existe una partida válida. Nueva partida pide confirmar si reemplazará el progreso; conserva ajustes y fotografías personales del álbum, y limpia los marcos colocados en el refugio. La pantalla de inicio no simula el mundo ni escribe un guardado nuevo. Ajustes permite volver al inicio guardando la partida.

Los guardados 1.8, 1.9, 2.0 y 2.1 se leen con el mismo almacenamiento anterior. Continúa la partida después de actualizar para conservar recuerdos, elecciones, portales de retorno y el estado de las lunas. Nueva partida es una elección explícita, no un paso obligatorio de instalación.

## Rutas sin perderte

Desde el juego entra en **Ajustes → Rutas y destinos**. El selector muestra:

1. Dreamcore: recorrido principal de 13 mundos.
2. Dark Fantasy: las lunas y la catedral.
3. Los lugares imposibles: los 11 mundos de los cinco videos y tres imágenes anteriores.
4. La isla de la casa rosa: nuevo sueño con tobogán.
5. Pradera de ojos: **VISTA PREVIA SIN ATAQUES**.
6. Mi refugio: objetos y fotografías.

Los portales de cada ruta llevan rótulos con el destino y la dirección: SEGUIR, VOLVER o SALIR. Las etiquetas se proyectan desde el portal, desaparecen detrás de paredes y se escalonan para que no se tapen entre sí. Las puertas de sueños opcionales también tienen nombre; las puertas secretas que requieren la linterna siguen ocultas. El refugio incorpora dos puertas para los nuevos escenarios. Los mapas opcionales devuelven al lugar desde el que entraste. Elegir una ruta principal desde el selector inicia esa ruta conservando los recuerdos recogidos.

## Nuevos lugares y presencias

**La pradera que te mira** es una prueba opcional del concepto de tu imagen: colinas verdes, cielo negro con nueve ojos texturados que parpadean de forma independiente, una silla y un árbol para dar escala. Una sola cabeza roja, con rostro texturado y cola afilada curva, se acerca lentamente y se mantiene a unos nueve metros. No persigue para atacar, no causa daño y no hay combate. Se puede entrar y salir para valorar el aspecto antes de ampliarlo.

**La casa rosa sobre el océano** adapta tu referencia: pilar de hierba redondeado de 72 metros, casa y árboles arriba, un tobogán rosa de 200 metros con paredes curvas y plataforma de salida a nivel del mar. Puedes recorrerlo caminando o corriendo en primera persona. Hay un portal de retorno arriba y otro al final. Las paredes de la casa y los bordes del recorrido tienen colisión.

Las presencias anteriores reciben formas nuevas: observadora de vestido plegado y ojo central, plumas modeladas y texturadas, ojos en las mangas, manos alargadas; doliente más alto con lágrimas oscuras; guía con cabeza floral. El cartero conserva su silueta sin rostro y las entregas de cartas. Se retrasa la aparición de observadores y se evita duplicar la presencia del pueblo; los encuentros del jardín y la carretera se limitan a uno por visita. Las figuras siguen siendo no agresivas. El evento de la luna tras 120 segundos efectivos de mirada permanece.

## Renovación visual y rendimiento

- Jardines antiguos: invernaderos arqueados, estructuras curvas, macetas, asientos y vegetación colgante, distribuidos en veinte conjuntos.
- Ajedrez: cuatro tipos de piezas con perfiles torneados, collares y coronas diferentes, más piezas cercanas para explorar.
- Valle de cúpulas: superficies continuas y arcos suaves sobre los canales.
- Carretera: bordillos biselados, rincones con asientos y vegetación.
- Césped: margen de textura mayor y filtrado de detalle lejano para reducir cuadrículas y contaminación de otras casillas del atlas.
- Isla: mar con ondas suaves, brillo dependiente del ángulo y espuma alrededor del pilar.
- Presencias: ojos y rostro originales texturados, parpadeo, sombras suaves de contacto y descarte de figuras fuera de cámara.
- Se mantienen las sombras direccionales, materiales con rugosidad y relieve aproximado, reflejos planos del lago y espejos, nubes con capas independientes y el descarte de zonas que no se ven. Los rótulos no añaden texturas ni una novena unidad de muestreo; el atlas de entidades reutiliza una unidad durante su dibujo.
- Inicio y menús detienen los fotogramas del mundo. Se mantiene el objetivo de 30 FPS, opción 60 y límite térmico de 24 cuando Android informa calor severo (API 29 o posterior). No se han medido FPS ni temperatura en tu Infinix NOTE 50s.

Los dos nuevos lugares tienen música original diferente: coro y notas espaciadas en la pradera; arpa y flauta en la isla. No se copió audio de los videos.

## Comprobación y vistas

`VALIDACION.json` contiene los resultados de compilación, comprobaciones de lógica, enlace GLES2, geometría, audio y vistas. `vistas/` contiene renders del propio motor hechos con Mesa GLES2, sin interfaz Android; sirven para revisar mapas y modelos, no son capturas de un teléfono. Las adaptaciones mantienen geometría estilizada: no son reconstrucciones fotográficas idénticas a las referencias.

Para repetir la lógica: Java 17 y compilador Kotlin 1.9.24 en PATH, `python tools/run_core_checks_22.py`. Para compilar: `gradlew.bat :app:assembleDebug`. Las herramientas de vistas requieren Python, numpy, Pillow y EGL/GLES2; el código de la app no las necesita. El proyecto no incluye cachés, SDK, APK firmado, claves privadas ni los videos originales.
