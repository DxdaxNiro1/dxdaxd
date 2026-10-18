# Audio del abismo 1.3

La música `music_crater_13.ogg` y la capa `layer_abyss_13.ogg` son composiciones
procedurales originales, sin muestras externas ni audio de los videos de referencia.
El objetivo es una atmósfera espaciosa y nostálgica: acordes suaves ligeramente
desafinados, notas de campana espaciadas y reverberación larga; en profundidad se
añade aire grave. No se afirma haber escuchado ni reproducido las canciones de
las referencias.

`DreamAudio.kt` usa estas dos pistas en el cráter y conserva los recursos originales
del resto de mapas. La casa del cráter continúa su música; la capa `CASA` se mezcla
suavemente. Las transiciones conservan el volumen real si se interrumpen; al volver
de segundo plano solo arrancan las pistas del mundo y zona actuales. `release()`
puede llamarse varias veces sin liberar dos veces un reproductor.

Para integrar, copiar **ambos** OGG a `app/src/main/res/raw/` y sustituir
`DreamAudio.kt` en su paquete habitual. No borrar los archivos de sonido originales:
el resto del juego aún los necesita.

Para regenerar desde la raíz del paquete:

```bash
python3 tools/generate_abyss_audio.py
```

Requiere Python 3, NumPy y FFmpeg con libvorbis. Es determinista con las mismas
versiones de esas herramientas. La síntesis es periódica y la reverberación se
envuelve en el bucle. Se valida también el OGG decodificado, no solo el WAV fuente.
`audio_validation.json` guarda duración, pico, RMS, muestras saturadas y diferencia
entre las muestras de unión.

Resultados del artefacto OGG: música de 64 s, RMS 0.140, pico 0.608; capa de 32 s,
RMS 0.085, pico 0.343; ninguna muestra saturada. Saltos de unión máximos 0.00691 y
0.00401, inferiores a los máximos normales dentro de cada pista. Esta comprobación
de señal no sustituye escuchar los bucles y probar pausa, reanudación y portales en
un teléfono Android; esas pruebas todavía quedan pendientes.

También pasó la compilación de `DreamAudio.kt` contra la API de Android con Kotlin
1.9.24 y un ensayo de estado con reproductores/animadores simulados: fundidos
interrumpidos sin saltos de volumen, cambio de capa profunda según el mapa,
pausa y cambio de mundo antes de reanudar, continuidad al entrar en la casa y
liberación repetida. El ensayo simulado no evalúa el decodificador del teléfono.
