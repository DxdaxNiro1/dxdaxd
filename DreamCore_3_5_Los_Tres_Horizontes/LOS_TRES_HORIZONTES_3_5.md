# DreamCore 3.5 · Los tres horizontes

Tres mundos nuevos basados en las referencias del mar cálido con puertas, el paisaje rojo bajo un gigante y la roca elevada en el océano azul. La entrega contiene **66 mundos y 67 músicas**. No requiere comenzar otra partida.

## Lugares y accesos

| Mundo | Categoría | Contenido |
|---|---|---|
| Las puertas del último sol | Dreamcore, verde | Isla rocosa con hierba y flores, mar turquesa al atardecer, arco distante, puertas suspendidas y dos escaleras transitables |
| Bajo el gigante carmesí | Dark Fantasy, violeta | Valle rojo con agujas de piedra, vegetación oscura, bruma y un planeta inmenso con lunas |
| La roca del océano infinito | Dreamcore, verde | Cima a unos 76 metros, paredes rocosas, mar azul reflectante, planeta azul e islas lejanas |

**Umbral → sector verde → Los tres horizontes** abre el primer mapa. También puedes escogerlos en **PAUSA → Mundos** buscando su nombre en la categoría. Se conserva el portal clásico de Piscinas y sueños: el Umbral tiene ahora once accesos.

Cada escenario tiene una puerta de regreso y otra que lleva al siguiente. Las escaleras del atardecer terminan en puertas elevadas hacia los otros dos horizontes. Las puertas que permanecen en el cielo lejano forman parte del paisaje; no todas tienen suelo debajo. Las señales de las puertas interactivas muestran su destino.

## Borde, salto y regreso

Puedes caminar hasta la orilla, mirar hacia abajo y saltar. La ausencia de terreno inicia una caída real con gravedad, velocidad vertical y control limitado en el aire. No hay un muro invisible alrededor de estos mapas. El océano y el cielo continúan más allá de las islas; no significa que puedas caminar sobre el agua o que exista suelo infinito.

El viento frontal se hace más intenso y cambia de tono según la velocidad. Antes de alcanzar el agua o el fondo de la bruma, se cierran dos párpados. El cambio de posición ocurre únicamente mientras la pantalla está completamente cubierta. Reapareces en el último suelo firme de arriba y vuelves a abrir los ojos. No se aplica la muerte ni se pierden objetos, pistas o decisiones por este salto.

Un salto normal que vuelve a tocar terreno aterriza normalmente; no provoca regreso automático. La entrada de cada escalón y la consulta de colisiones corresponden al volumen real de las escaleras. Los marcos y las rocas siguen siendo sólidos. Guardar o salir durante una caída guarda un punto firme; continuar desde un punto en el aire se repara hacia una llegada segura.

El volumen de aire utiliza **Sonido → Clima**, además del volumen general de ambiente. Los bucles se detienen al pausar, ir al inicio, cambiar de mapa o mandar la app al fondo. La mecánica anterior de cerrar los ojos voluntariamente sigue disponible en Objetos.

## Acabado gráfico

Los cielos usan tres panoramas originales generados para esta entrega, con las nubes, sol y planetas. El terreno, acantilados, escaleras, puertas y vegetación cercana son geometría 3D; el cielo y las islas muy lejanas pertenecen al fondo panorámico. Esto acerca la composición y el detalle de las referencias sin convertir todos sus elementos lejanos en modelos volumétricos.

El agua comparte la misma radiancia del cielo: al mirar hacia abajo aparecen las nubes y el planeta correspondientes, con ondas, brillo y color costero. Usa además el reflejo plano de la geometría cuando está disponible. La alternativa compatible conserva el cielo reflejado incluso sin ese framebuffer. Las rocas y superficies usan el atlas fotográfico compartido. No se promete una reproducción idéntica de las ilustraciones en todos los teléfonos.

Se reutiliza un sampler ya existente, sin ampliar el límite GLES2 de ocho texturas de fragmento. Se mantiene un solo panorama residente: normalmente 2048×1024, o 1024×512 en calidad baja; se libera al viajar y se invalida al recrear el contexto GL. Hay un cielo procedural de respaldo si la textura no puede cargarse. Estos mundos no activan la pasada anterior de nubes volumétricas; las caídas no generan geometría ni sintetizan audio en el teléfono.

## Música

65. **Puertas donde duerme el sol** — cristal, brisa y luz cálida.
66. **El peso de otro cielo** — cuerdas oscuras y resonancias del gigante.
67. **Antes de abrir los ojos** — piano, agua y distancia azul.

Las tres composiciones y el viento se sintetizan desde señales propias. Las 64 músicas, imágenes y sonidos de 3.4 se conservan byte por byte. Las lunas vivas, encuentros de terror, guardado y capítulos anteriores permanecen.

[Instalar](LEEME.md) · [Vistas del motor](VISTAS.md) · [Validación](VALIDACION_3_5.json).
