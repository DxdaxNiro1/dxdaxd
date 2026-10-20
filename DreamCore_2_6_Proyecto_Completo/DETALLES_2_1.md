# DreamCore 2.1 — Los lugares imposibles

## Instalar

Extrae TODO el ZIP y ejecuta `Instalar_Windows.cmd` en tu PC. Indica la carpeta de tu juego en Android Studio. El instalador respalda `app/src/main` y `app/build.gradle.kts`, copia fuentes y recursos, y establece **versionCode 21 / versionName 2.1**. Abre Android Studio y compila. Conserva tu identificador y clave de firma y actualiza la aplicación sin desinstalarla para mantener los datos.

Como alternativa, extrae el proyecto completo y abre **DreamCore**, donde está `settings.gradle.kts`. Android Studio crea `local.properties` para tu SDK. Se usa SDK 34, JDK 17 o compatible, Gradle 8.7, AGP 8.5.2 y Kotlin 1.9.24; Android mínimo API 21.

Instalación manual: copia todos los `.kt` junto a DreamRenderer.kt y MainActivity.kt, y `res/raw` / `res/drawable-nodpi` conservando recursos anteriores; ajusta las dos versiones en Gradle. Para restaurar, devuelve `main` y `build.gradle.kts` desde `DreamCore_backup_2_1_...`. No compiles tras una copia incompleta. Se entregan fuentes, no un APK con otra firma ni claves privadas.

## Encontrar los once mundos

Entra en **Ajustes → Los lugares imposibles · 11 mundos**. También hay una puerta central al fondo del Umbral y otra a la izquierda de la entrada del refugio. Cada mundo tiene una puerta de vuelta y otra para avanzar; la última devuelve al lugar desde el que entraste. Los trece niveles Dreamcore y los dos Dark Fantasy anteriores siguen disponibles por sus entradas habituales.

| Mundo | Recreación y contenido |
| --- | --- |
| El barrio que se inclinó | Calles blancas, casas de colores inclinadas y apiladas, fachadas de dos niveles, marcos redondeados y tejados modelados. |
| El cine sobre la Tierra | Butacas tapizadas con respaldos curvos, pasillo central con balizas, pantalla animada original, arcos, galería orbital y una sola Tierra enorme. Una pasarela permite observar la estructura circular y su pilar desde fuera. |
| Las duchas del valle infinito | Paredes de musgo con curvatura continua, camino que serpentea, duchas de tubos curvos y agua, tres piscinas circulares, zonas de descanso y gruta con aberturas redondas en el techo. |
| La casa reflejada entre nubes | Isla de roca, estanque anular, puente de madera, casa de dos plantas, nubes y reflejo de la casa calculado mediante una cámara reflejada sobre el agua. |
| El canal de los hongos dormidos | Agua azul entre paseos rosados, puentes, farolas y grandes hongos con tallos cilíndricos, sombreros redondos y manchas modeladas. |
| La colina de la nube inmensa | Terreno ondulado, una casa rosa, hierba y un banco enorme de nubes suaves. |
| La última gasolinera del atardecer | Campo aislado, cielo de tarde, cubierta de bordes curvos, surtidores con pantallas y mangueras, postes y manchas iridiscentes en el suelo. |
| El barrio de las sombras de colores | Casas pastel, jardines, tejados, vegetación, aceras redondeadas y veladuras de color en el asfalto. |
| El pasillo de las sillas vacías | Alfombra verde azulada, treinta y dos sillas de madera y tela, huecos inclinados de luz, puertas, cuadros y lámparas de vidrio con brazos curvos. |
| El edificio de los pisos sin nadie | Atrio blanco, vacío central, puertas en varios niveles, corredores superiores, puentes y rampas transitables que suben ocho metros. El vacío está protegido para conservar un recorrido seguro. |
| La oficina dentro de las nubes | Escritorios, computadoras CRT con carcasas biseladas, teclados, ratones, sillas curvas, alfombra con un corredor de otro color, techo de placas con nubes y nubes suaves dentro y fuera del ventanal. |

Son recreaciones jugables de los motivos y ambientes de tus referencias. Los videos mezclan distintas tomas y algunas formas cambian entre ellas; se organizaron en espacios estables que puedes recorrer. La geometría sigue siendo estilizada y no se promete una reproducción fotográfica idéntica a los videos.

## Qué referencia corresponde a cada lugar

- Video **15.48.33**: barrio blanco con edificios de colores inclinados.
- Video **15.49.41 (1)**: cine, pasillo orbital y estructura sobre un pilar, reunidos en un mundo.
- Video **15.49.40**: duchas, piscinas, sendero entre paredes de musgo y gruta, reunidos en un valle con varias zonas.
- Video **15.49.41**: casa sobre el estanque, canal con hongos, colina/casa rosa, gasolinera y suburbio pastel; cinco mundos distintos conectados.
- Las **tres imágenes**: pasillo con sillas, atrio blanco y oficina entre nubes; un mundo por imagen.
- Video **15.49.41 (2)**: grabación del juego anterior. Se usó para revisar los materiales y la lectura visual de la pradera y el mar. La pradera recibe vegetación más redonda, los materiales cambian globalmente y se conserva la criatura del mar de la 2.0.

## Formas, texturas e iluminación

Se creó una nueva lámina de dieciséis materiales: yeso fino, musgo, roca, asfalto, metal, cerámica, mármol, madera, tela, concreto y suelo. Se genera una textura auxiliar de relieve y rugosidad al cargarla. Las muestras se repiten con coordenadas continuas para reducir las líneas visibles entre repeticiones; las coordenadas usan más precisión cuando el GPU lo permite.

Las piezas nuevas incluyen cajas con biseles y normales suaves, respaldos arqueados, patas y brazos de sillas, cilindros, lámparas, cabezas de ducha, mangueras, teclados y cubiertas de hongos. La roca del valle es una superficie curva, no una pila de bloques. También se sustituyeron varias sillas antiguas por el modelo curvo, conservando sus colores, y se redondeó la vegetación del pueblo.

En los interiores, la luz cálida de lámparas, luz fría de huecos y oscurecimiento de contacto se calculan al construir la escena. Se combinan con materiales y reflejos; no son luces con sombras dinámicas por cada lámpara. En exteriores se mantienen las sombras direccionales. El lago usa un reflejo plano de 512 píxeles, con recorte de la geometría bajo el agua y pequeñas ondulaciones; se activa en calidad media/alta, cuando está cerca y visible. La calidad baja conserva el reflejo ambiental más sencillo.

Las nubes interiores usan capas suaves con transparencia y deriva lenta. El techo de la oficina combina el patrón de placas con nubes. El cine muestra una Tierra con textura y una película abstracta original de nubes en movimiento; no reproduce una película comercial. Las transiciones oscurecen menos la pantalla.

## Música, partida y rendimiento

Once temas ambientales originales nuevos, uno por mundo, con frases e instrumentos distintos. Se conservan las capas de agua, brisa, vacío interior, pasos y controles de volumen.

La 2.1 lee partidas 1.8, 1.9 y 2.0 usando el mismo almacenamiento. Conserva recuerdos, llamadas, decisiones, álbum, marcos, herramientas y estado lunar. Se guardan los mundos nuevos y sus salidas anidadas. El refugio, las fotos transitables, el cartero y la zona de gravedad siguen disponibles. La luna gigante conserva sus 120 segundos efectivos de mirada, con dos segundos de margen al apartarte.

Se conserva el modo inicial de **30 FPS**, opción de 60 y límite de 24 ante aviso térmico elevado de Android 10+. Los menús detienen los cuadros; se dibuja solo la geometría visible, el cielo después de las superficies opacas y las transparencias por separado. Las sombras estáticas se reutilizan y los espejos actualizan con menos frecuencia al detener la cámara. Solo se conserva una escena de los nuevos mundos en la caché de esta ruta. El atlas mantiene el tamaño de GPU de la versión anterior según la calidad elegida.

Las vistas incluidas son renders de geometría y shaders reales en Mesa/GLES2, sin HUD. Se compila un APK de validación y se comprueban reglas, recorridos, guardados, shaders y audio. Queda medir FPS, temperatura, controles y sonido en tu Infinix NOTE 50s y ejecutar el instalador en Windows. Los límites de FPS son objetivos, no mediciones del teléfono.

Consulta `RECORRIDO_CON_SECRETOS.md` y `VALIDACION.json`.
