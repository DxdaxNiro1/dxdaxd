# Rendimiento y límites 3.1

La optimización actúa en el juego: mallas preparadas por un trabajador fuera del hilo OpenGL, hasta dos pendientes, subidas limitadas y espera solo de geometría cercana. La residencia no vuelve a ordenar todas las celdas cada cuadro mientras se suben las mismas mallas. Los índices opacos se separan una vez en superficies comunes y efectos; no se elimina ningún triángulo al separarlos.

Los programas compactos excluyen el trabajo de portales, agua y entidades cuando se dibujan paredes y suelos. Conservan normales, rugosidad, sombras PCF, luces locales, linterna, flores, ventanas, humedad y otros acabados. El follaje de las plantas compartidas usa texturas con recorte en lugar de hojas elipsoidales. Las nuevas entidades usan un atlas, modelos indexados reutilizados, dos niveles geométricos y descarte fuera de cámara; se preparan en otro trabajador y se sube como máximo un modelo nuevo por cuadro.

## Comparación controlada

Mesa llvmpipe / GLES2, 640×360, cuatro hilos de software, 30 muestras después del calentamiento, misma geometría indexada 3.1, cámara, texturas y calidad. Se cronometra el dibujo de la geometría sin cielo, posprocesado, compilación, carga de imágenes o entidades. Son tiempos de ese tramo en el equipo de pruebas, **no FPS del juego ni mediciones del celular**.

| Zona | Programa general (ms) | Superficies compactas (ms) | Reducción del tramo |
| --- | ---: | ---: | ---: |
| dark_01 | 19.51 | 13.81 | 29.2 % |
| echo_10 | 23.84 | 15.90 | 33.3 % |
| echo_01 | 23.28 | 15.67 | 32.7 % |
| echo_09 | 30.50 | 20.17 | 33.9 % |
| dream_01 | 9.58 | 7.03 | 26.5 % |

El nuevo flujo añade dos buffers de índices por página: juntos cuestan dos bytes por índice opaco; las posiciones y texturas no se duplican para esos dos flujos. Los informes de mallas indexadas anteriores no incluyen este coste adicional y no deben leerse como una medición de VRAM. La selección de páginas continúa usando índices de 16 bits sin requerir extensiones GLES.

## Validación

- Se ejecutan el guardado, los recorridos de los 61 mundos, las funciones de 3.0 y las rutas nuevas en JVM.
- Se enlazan 621 combinaciones reales GLES2: escena, cielo, ruta compatible, precisión media, superficies compactas y entidades. Los 61 mundos se dibujan en perfiles Estable y Esencial; una selección también en Completo: 142 vistas en total.
- Cuatro escenas independientes comparan píxeles de flores, ventanas, techo con nubes y brillo de asfalto entre el programa general y el compacto. Se exige error medio menor de 3/255 y percentil 99 menor de 16/255.
- La regresión de bitmap ejecuta el cargador real y los PNG reales con las fronteras Android/GL simuladas. La corrección sin Canvas permanece intacta.
- Android `assembleDebug`, Android Lint y verificación de firmas para API 19. No hay emulador ni teléfono físico en estas mediciones.

La fábrica inicial de escenarios y el enlace de programas OpenGL siguen siendo síncronos; todavía pueden causar espera al entrar. Infinix NOTE 50s, POCO X4 Pro 5G y el Redmi reportado requieren medir FPS, pausas y temperatura en sus equipos. No se promete eliminar todo calentamiento ni igualar renders ilustrados externos. La mejora conserva API 19, GLES2 y los perfiles de compatibilidad.
