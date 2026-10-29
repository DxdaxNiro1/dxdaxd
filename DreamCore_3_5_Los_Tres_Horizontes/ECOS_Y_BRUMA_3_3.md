# Ecos y bruma · contenido y sonido

## Interfaz y navegación

Se separan las opciones técnicas del viaje. FPS objetivo está en Vídeo; FPS medidos y memoria están en Diagnóstico, ocultos del HUD por defecto. El diario, álbum, objetos y capítulo tienen accesos propios en Pausa. El inicio muestra la versión y cantidad reales de temas.

Cada uno de los 63 mundos pertenece a una categoría y la sala de escucha utiliza esa misma clasificación. Las ubicaciones de las presencias se obtienen de sus colocaciones y de sus encuentros dinámicos conocidos. Los viajes desde el catálogo mantienen un regreso utilizable sin llenar la pila de portales; no borran pistas ni decisiones. El acceso al depósito mantiene el requisito del capítulo.

## Presencias y respiración

Seis familias originales de timbre: coro lejano, garganta deformada, crujido óseo, raspado, susurro y resonancia húmeda. Se asignan según la figura. También se incluyen observadores anteriores del camino y de las ventanas; Alma, el guía, el cartero y el compañero del campo no generan amenaza.

El cálculo se realiza diez veces por segundo sobre las colocaciones reales, incluida la aproximación del enmascarado y el movimiento del custodio. Una sola presencia cercana ocupa la voz principal. Distancia, orientación y muros afectan al sonido; las intervenciones se espacian al menos unos seis segundos. Los subtítulos indican de qué presencia procede.

La respiración crece gradualmente al aproximarte y recupera la calma al alejarte. La música baja suavemente hasta aproximadamente la mitad cerca de una amenaza para dejar espacio a respiración y voces. La sala de escucha mantiene el volumen musical elegido por el usuario. Las capas de clima y respiración se detienen al abrir menús, ir al inicio o pasar a segundo plano. Los niveles de voces, respiración y clima son independientes y respetan el ambiente general.

Se reutiliza el SoundPool existente, con hasta tres bucles nuevos: brisa, lluvia y respiración. Las muestras se producen fuera del juego; Android reproduce OGG comprimido y no sintetiza ni filtra estas voces en tiempo real. No se añade un efecto de pantalla completa para simular el miedo.

## Clima

Brisa delicada en exteriores y ráfagas lentas en paisajes oscuros. Lluvia visible y audible en la nueva ciudad, el bosque del sol vacío y el camino de los testigos. En el corredor cubierto de la ciudad cesa la lluvia exterior. Se reutilizan las partículas existentes con caída descendente y escritura de profundidad desactivada. La vegetación y telas conservan su movimiento sutil. Puedes ocultar la lluvia visual y ajustar el clima desde Sonido.

## Composiciones

Se conservan byte por byte los 55 temas anteriores de inicio, exploración y otros ambientes. Los temas 56–63 reciben nuevas versiones originales, con voces sintetizadas sin palabras, viento, cinta, resonancias y espacios de silencio. El tema 64, **Todavía hay alguien en el turno**, pertenece a La ciudad que escucha llover. Cada tema mantiene identidad propia y puede escucharse desde su categoría.

Los siete MP3 aportados se analizaron como referencias generales de atmósfera. Las composiciones y efectos se generan desde señales y arreglos propios: los archivos subidos, sus grabaciones y las imágenes de Cry of Fear no se distribuyen dentro de la aplicación. El nuevo lugar y el inquilino son creaciones propias inspiradas en ese tipo de terror urbano, sin presentarse como contenido oficial de Cry of Fear.

## La ciudad que escucha llover

Calle de piedra húmeda y ventanas mayormente apagadas; unas pocas ventanas conservan un cielo imposible. Los faroles y cables llevan al corredor de servicio rojo. Más allá se abre una plaza suspendida con manos de piedra y bordes rojos. El corredor y el camino central son transitables sin saltar al vacío. El nuevo inquilino tiene túnica, cabeza con relieve y mandíbula articulada, brazos alargados y dedos separados; utiliza los materiales originales compartidos y dos niveles de detalle.

Se conserva el guardado, los acertijos y el capítulo 3.2. Las lunas mantienen mirada, parpadeo, cambio a rojo al minuto y la aparición gigante anterior. El campo de ojos sigue siendo un lugar sin ataques.

[Pruebas](VALIDACION_3_3.json) · [Imágenes reales del motor](VISTAS.md).
