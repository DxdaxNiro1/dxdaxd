# DreamCore 3.4 · Umbral y furia

Se conserva el juego de 63 mundos y 64 composiciones. El Umbral se reconstruye en cinco sectores y las presencias hostiles reciben encuentros de proximidad con advertencia, agarre, observación, grito y muerte. La partida no se reinicia al morir.

## Orientación

El origen es la plaza central. Desde allí salen cinco caminos; cada uno repite número, color y categoría en suelo, portales, etiquetas y catálogo. Los colores indican categorías, no seguridad.

| Número | Color | Sector | Accesos físicos |
|---|---|---|---|
| 1 | Verde | Dreamcore | Piscinas y sueños, barrio inclinado, aberturas del cielo, ojo entre nubes |
| 2 | Ámbar | Backrooms | Las luces |
| 3 | Violeta | Dark Fantasy | Las lunas dormidas |
| 4 | Carmesí | Terror y pesadillas | Los testigos, la ciudad que escucha llover |
| 5 | Azul | Refugio y recuerdos | El último tren, biblioteca |

**PAUSA → Plano del Umbral** presenta un esquema y accesos a los catálogos de cada sector. **Mundos** mantiene la búsqueda y las marcas de visita. El tren conserva su horario y los viajes; el archivo sigue requiriendo las pistas anteriores. Todos los destinos se conservan.

## Encuentros

Invadir el espacio de una presencia hostil produce primero una advertencia. Puedes retroceder para evitar el agarre. Si permaneces demasiado cerca durante aproximadamente 0,85 segundos, comienza la secuencia: te sujeta, gira hacia ti, te observa y reacciona con furia antes de matarte. El grito ocurre una vez por encuentro. La sangre aparece en los bordes de la vista y termina en un fundido, sin destellos rápidos.

Hay 18 perfiles: coro, máscara, testigo, carmesí, astas, guardián dorado, estrella, observatorio, herbario, ojo del agua, inquilino, observador, figura del duelo, figura de ventana, vigilante del cono, ojo de raíces, caballero ocular y serafín. Cambian el movimiento de alas, brazos, cabeza, mandíbula o apéndices según el cuerpo; no son 18 clips capturados, sino secuencias y poses procedurales propias para los modelos reales.

Las cuatro figuras en reposo de la pradera pasan a ser entidades articuladas que reaccionan al invadir su espacio, conforme a la nueva petición. Mantienen sus posiciones y no recorren el campo persiguiéndote. El compañero amistoso, el guía, el cartero y Alma siguen siendo seguros. Las lunas y el ángel gigantesco distante conservan sus reglas y su papel en el paisaje; no se convierten en atacantes que alcancen desde otro extremo del mapa.

Las activaciones comprueban distancia, altura y visibilidad para evitar agarres a través de muros o desde otro piso. Los escondites del archivo conservan su protección. La persecución se detiene durante la advertencia de proximidad para permitir retroceder; las demás acciones quedan bloqueadas durante el agarre. Pausar o mandar la aplicación al fondo detiene la secuencia y los sonidos.

Al morir eliges **Despertar aquí** o **Volver al Umbral**. Conservas objetos, pistas, decisiones, capítulo y fotografías. Despiertas en la entrada del mapa con diez segundos de protección; un guardado durante el agarre utiliza la entrada segura, no la posición capturada. El esquema de guardado sigue siendo el 23.

## Modelos y sonido

Las articulaciones tienen canales separados para brazos, mandíbula, alas y ojos. Las extremidades usan tubos orientados según su eje y los rostros incorporan cavidad bucal y dientes separados. Los ojos conservan iris y pupila durante la furia. Las figuras antiguas del camino, duelo y ventana usan ahora geometría articulada y materiales compartidos. Las cuatro figuras del campo se dibujan como instancias animadas; sus cuerpos anteriores no se duplican en el suelo.

Dieciocho gritos originales se sintetizan fuera del teléfono y se distribuyen en OGG mono corto. No se incluyen grabaciones comerciales. El control **Sonido → Voces y sonidos de las presencias** también controla los gritos. Se reutiliza el SoundPool de ocho voces, una sola voz principal y los tres bucles de clima/respiración existentes. Las 64 músicas y todos los recursos heredados se conservan byte por byte.

## Ajustes y coste

**Accesibilidad → Sangre en los encuentros** permite ocultarla. **Encuentros letales** permite observar sin morir; la opción **Reducir movimiento** atenúa el desplazamiento de cámara del agarre. Se conservan subtítulos, ajustes separados y diagnóstico.

No hay generación de mallas por cuadro ni nueva pasada de pantalla para el ataque: poses en el shader existente, dos LOD por modelo, modelos indexados, caché y carga en segundo plano. La sangre es un conjunto fijo de 22 gotas en Canvas, sin crear bitmaps por cuadro. No se promete un rendimiento o temperatura concretos sin probar físicamente los teléfonos.

[Instalación](LEEME.md) · [Vistas del motor](VISTAS.md) · [Pruebas](VALIDACION_3_4.json).
