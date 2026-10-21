# Rumbo de DreamCore

Decisiones del usuario, conversación del 19 de octubre de 2026 (America/La_Paz).

## Implementado y verificado por programa en 2.3 · prueba física pendiente

- Convertir el pueblo en una aventura con decisiones y varias formas de investigar. Su problema actual es que caminar hasta el siguiente portal resulta lineal y predecible.
- Un misterio concreto, varias pistas y soluciones; usar cámara, linterna y otros objetos existentes con consecuencias observables.
- El entorno y las presencias responden a lo que hace el jugador. Conservar lo descubierto al regresar.
- Optimizar para reducir trabajo del dispositivo y calentamiento, conservando contenidos y opciones visuales. Teléfono del usuario: Infinix NOTE 50s. No afirmar mejoras térmicas ni compatibilidad universal sin pruebas en dispositivos.
- Mantener partidas y mundos anteriores. Priorizar probar la aventura en el pueblo antes de extender el sistema a todos los mundos.

## Ramas acordadas

- Backrooms: confirmado para esta actualización con las nuevas referencias; rama propia con identidad y regreso claro.
- Dreamcore conectado como experiencia coherente; Dark Fantasy mantiene una rama independiente.
- La pradera de ojos sigue como vista previa sin ataques. Valorar su aspecto antes de añadir combate.

## Mecánicas confirmadas e incorporadas al pueblo y los escenarios nuevos

- Reglas propias de cada mundo que se aprenden observando.
- Consecuencias con doble filo: resolver algo también puede atraer una presencia.
- Personajes que recuerdan encuentros y modifican sus acciones.
- Variación de pistas y encuentros entre partidas, con lógica coherente.
- Diario de pistas que registra descubrimientos sin revelar soluciones.

## Criterios de diseño

- Las novedades deben cambiar decisiones, curiosidad o consecuencias; evitar ampliar solo el número de pasillos o portales.
- Encuentros escasos y significativos, ambientación tranquila e inquietante, sin llenar todos los mapas de figuras.
- Algunas salidas pueden ser viajes, trenes o cambios del entorno, además de puertas.
- Ninguna idea pendiente se considera implementada por estar en este documento.

La petición de 2.3 autorizó implementar las cinco mecánicas y nuevos escenarios basados en los cuatro videos y cinco imágenes adjuntos.

## Cobertura de referencias 2.3

- 22.26.54: campo y grandes aberturas luminosas.
- 22.27.01: corredores entre nubes y castillo.
- 22.27.07: Backrooms amarillo con salas y corredores.
- 22.27.06: ciudad de flores, arco, piscina y tren.
- Imágenes: arco/campo/ciudad integrados en la ciudad; sendero, sonrisa y casa junto a grietas integrados en la pradera; el abismo de casas existente se conserva.
- Conservar futuras decisiones: combate y multijugador no forman parte de esta entrega.

## 2.4 Resonancias · 20 de octubre de 2026

- Petición: una música única y tranquila a la vez que inquietante para cada escenario y el inicio; entregar las pistas con nombres y miniaturas de los mapas.
- 49 composiciones originales de síntesis, 103:36 en total: 48 escenarios registrados, incluido el interior antiguo de archivo, más el título. Catálogo exacto con los nombres actuales de los mapas, también refugios y fotografías visitables.
- Sala de escucha Android con miniatura de escenario, selección, anterior, siguiente y pausa; restaura la música al cerrar. Música de inicio distinta del Umbral.
- Álbum MP3 independiente con portadas incrustadas, catálogo, M3U8 y reproductor HTML local, sin dependencias externas. Los MP3 no se duplican dentro del APK.
- Ayuda opcional en Ajustes para las reglas de los cinco escenarios nuevos; las preguntas del jugador revelaron que el círculo activo y la parada no estaban suficientemente claros.
- La referencia musical subida no pudo descargarse: supera el límite de transferencia de 32 MB. El usuario ya fue avisado para enviar fragmentos; no afirmar que se escuchó o analizó. Las piezas interpretan la dirección estética pedida, sin extraer muestras.
- Se conserva el guardado 23, los mapas, la vista previa sin combate y las protecciones de energía. No hay mediciones térmicas ni prueba física Android de esta versión.

## Dirección visual 2.5 · 20 de octubre de 2026

El usuario quiere llevar al juego el acabado de las portadas ilustradas: materiales creíbles, texturas con detalle, nubes con profundidad, agua que refleje el cielo y luz suave. Es la referencia artística para las siguientes mejoras. Las ilustraciones no prueban el aspecto que alcanza el motor en tiempo real.

Primera implementación: atlas renovado, escala de fibras y mosaicos, relieve por superficie, rugosidad, iluminación de materiales en espacio aproximadamente lineal y curva de blancos; luz de lámparas y ventanas en pasillo, atrio, oficina y Backrooms; agua de las piscinas con reflejo del cielo y nubes con sombreado más profundo. Se conservan geometrías, recorrido, guardado y músicas. La remodelación completa y un acabado equivalente a las portadas quedan pendientes de más trabajo y revisión del resultado real.

Presupuesto: bajo 512 y equilibrado 1024 para los atlas, como en 2.4; alta opta por 2048. Se mantienen el límite de FPS, protección térmica y frecuencia de reflejos/sombras. Las vistas se verifican en GLES2/Mesa; no hay medición de temperatura, batería o FPS en el NOTE 50s.

## Revisión 2.6 Profundidad · 20 de octubre de 2026

El usuario autoriza una revisión prolongada del motor para acercarlo a las portadas. Se desarrolla una base compartida con oclusión de contacto, FXAA, resplandor Alto, nube volumétrica a media resolución, reflejo plano y transparencia de piscinas. Se remodelan piscinas, casas compartidas y vegetación del pueblo. El rendimiento sigue siendo un criterio de diseño: calidad efectiva según calor/ahorro, geometría botánica reducida, límites de FPS conservados y capacidades opcionales con alternativas.

Las 48 escenas, 49 músicas, guardado y comportamiento de la luna se mantienen. Este trabajo no equivale todavía a una reproducción de las ilustraciones. La revisión visual se demuestra con vistas del motor; la temperatura, autonomía y fluidez reales deben medirse en el NOTE 50s.

## Actualización 2.7 · Vigilia · 21 de octubre de 2026

Entregado: 51 escenarios registrados, cuatro mundos de Dark Fantasy, isla celeste sin tobogán, lunas modeladas con cambio rojo al minuto y aparición gigante a los dos minutos, cuatro criaturas nuevas en reposo en la pradera de ojos, guardado de esquema 23 y 52 composiciones en la sala de escucha.

Se conservan los recorridos Dreamcore, Backrooms, fotografías, refugio, diario, secretos, reglas por mundo, consecuencias y personajes de las versiones anteriores. Los cuatro mundos oscuros están conectados por portales; el último vuelve al Umbral.

Pendiente para una futura actualización: decidir comportamiento de las cuatro criaturas con el usuario después de que vea los modelos. No se activaron ataques. La posibilidad de multijugador requiere trabajo específico de red, sincronización y sesiones; esta actualización no lo incorpora.

Validación física pendiente: sesiones prolongadas en el Infinix NOTE 50s y una selección de otros dispositivos antes de ampliar la distribución.
