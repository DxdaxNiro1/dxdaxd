# Rendimiento y diagnóstico · 3.2

La optimización se aplica al juego: reutilización de geometría repetida, atlas progresivo, colisiones sobre el BVH existente, colas acotadas y persistencia solo cuando cambia una pista, además del autoguardado habitual. Los pasos del custodio no provocan una escritura del guardado. Se conservan mapas, geometría detallada, texturas finales, músicas, reflejos y perfiles compatibles.

La caché de plantillas mantiene hasta 32 modelos. Los objetos estáticos siguen agrupados por celda; no se añade una llamada de dibujo por cada silla. Las piezas interactivas reutilizan sus mallas GPU. Las entidades usan detalle por distancia y un historial de poses limitado para los reflejos.

El atlas inicial contiene todas sus superficies. El trabajador prepara el detalle final y hasta cuatro pares de tiles pendientes; el hilo GL sube hasta cuatro pares por cuadro al inicializar y uno por cuadro al refinar. El intercambio se hace al tener toda la imagen nueva válida. Si una asignación/subida falla, permanece la pareja válida; si fallan los mipmaps, se conserva filtrado lineal completo. La pérdida de contexto cancela el trabajo sin borrar identificadores del contexto nuevo. El refinamiento ordena los tiles por distancia a los límites de sus superficies en las celdas cercanas. Esos límites se preparan en el trabajador de mallas y se consultan como máximo dos veces por segundo. Cuando aún no hay geometría preparada se usa el perfil interior/exterior como prioridad inicial; no se vuelven a recorrer todos los vértices en cada cuadro.

En una comparación JVM de **160 sillas de producción y la misma cantidad de vértices**, con cinco calentamientos y trece muestras, la mediana de generación bajó de **5,446 ms a 1,338 ms** en este entorno. Es una medición de trabajo CPU de modelos repetidos; no mide FPS, GPU ni temperatura de un celular. Los datos y método están en `tools/validacion33/bench_models_before33.json` y `bench_models_after33.json`.

## Medir en el teléfono

1. Actualiza conservando la firma y la aplicación instalada. Deja las mismas opciones de imagen para comparar.
2. Abre **Ajustes → Diagnóstico → Iniciar prueba**. Cierra el diálogo y juega por las zonas que fallaban durante varios minutos.
3. Activa “Mostrar diagnóstico” si deseas ver la lectura. El informe guarda estadísticas separadas por mundo.
4. Vuelve a Diagnóstico y pulsa **Guardar y compartir**. El selector de Android te permite escoger el destino. El juego no envía datos de forma automática.
5. Si tienes ADB y una compilación de depuración: `python tools/probar_telefono33.py --serial TU_SERIAL --seconds 180 --output telefono33.json`. Guarda el informe dentro del juego al terminar; el script recoge batería durante la sesión y el informe que guardaste.

El informe distingue cadencia real, tirones, tiempo CPU de envío y memoria Java/nativa. Los cuadros largos del primer mundo o de una carga también cuentan; las pausas de la aplicación reinician el reloj. El p95 se agrupa en intervalos de un milisegundo hasta la categoría 250 o más; el máximo conserva la duración real, incluso en bloqueos largos. La temperatura corresponde a **batería**, no al chip. El estado térmico del sistema puede no estar disponible en Android antiguo. No se atribuye al diagnóstico una medición de tiempo GPU o temperatura del SoC.

No se dispuso físicamente de Infinix NOTE 50s, POCO X4 Pro 5G ni Redmi. Sus resultados se mantienen pendientes. Las pruebas GLES2/Mesa comprueban renderizado, no garantizan una temperatura, batería, compatibilidad universal ni velocidad en esos teléfonos. La construcción inicial del escenario sigue siendo síncrona; la caché reduce su trabajo repetido y la indexación/carga se distribuye posteriormente.
