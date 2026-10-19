# Audio 1.4: una composición para cada uno de los doce mapas

Todos los temas `music_*_14.ogg` son composiciones procedurales originales, sin
muestras, canciones comerciales ni audio extraído de los videos del usuario.
Cada mapa tiene una melodía, progresión, distribución de notas e instrumentación
propias. No son doce copias de una pista transportadas de tono.

| Mapa | Recurso | Duración | Dirección sonora |
| --- | --- | --- | --- |
| Estación | `music_station_14` | 48 s | Teclas de juguete lejanas, acordes mayores cálidos |
| Piscinas | `music_pools_14` | 48 s | Gotas de cristal, quintas abiertas, ecos |
| Abismo | `music_crater_14` | 48 s | Acordes graves suspendidos y campanas muy espaciadas |
| Jardín | `music_garden_14` | 48 s | Arpegios de cuerda sintética y armonía luminosa |
| Pradera | `music_meadow_14` | 48 s | Voz de aire sin palabras y notas largas |
| Acuario | `music_aquarium_14` | 48 s | Vidrio grave y cambios menores lentos |
| Casa de la pradera | `music_house_meadow_14` | 32 s | Piano íntimo de melodía descendente |
| Casa del abismo | `music_house_crater_14` | 32 s | Piano de fieltro grave, melancolía y silencios |
| Ajedrez | `music_chess_14` | 48 s | Celesta solitaria y acordes monumentales |
| Túneles | `music_tunnels_14` | 32 s | Notas acuáticas irregulares y eco de cristal |
| Hotel de nubes | `music_cloud_hotel_14` | 48 s | Sintetizador de cinta cálido, ligeramente desafinado |
| Carretera | `music_road_14` | 48 s | Piano nostálgico de arpegios y fondo tenue |

## Integración y memoria

Copiar todos los OGG nuevos a `app/src/main/res/raw/` y sustituir `DreamAudio.kt`.
Conservar `layer_inside`, `layer_threshold`, `layer_anomaly` y el nuevo recurso de
profundidad `layer_abyss_13`, usado también en esta versión. Las músicas antiguas
pueden permanecer en el proyecto; el mezclador 1.4 selecciona los doce temas nuevos.

No se abren doce reproductores al iniciar: la carga es diferida. El límite es de
dos pistas principales y dos capas ambientales, incluidos los fundidos. Al terminar
un fundido se libera el reproductor anterior. Si llega un tercer destino durante
un fundido, un puente de 320 ms libera una plaza sin saltar el volumen y luego
comienza el fundido de 2,6 s hacia el destino más reciente. Las capas usan 1,8 s.

Pausar cancela las animaciones, silencia la pista actual y libera las anteriores;
cambiar de mundo mientras la aplicación está pausada no abre decodificadores.
Reanudar solo arranca los recursos del mundo y zona actuales. `release()` es
idempotente y libera todos los reproductores.

## Reproducibilidad y comprobaciones

```bash
python3 tools/generate_world_music_14.py
```

Requiere Python 3, NumPy, FFmpeg/libvorbis y el archivo hermano
`generate_abyss_audio.py` para exportar y comprobar el OGG. El generador construye
acordes que se cruzan suavemente, eventos instrumentales originales y reverberación
circular. Elige una unión de baja pendiente sin introducir silencios en cada vuelta.
Es determinista con las mismas versiones de herramientas.

`audio_validation_14.json` registra parámetros y medidas del audio decodificado.
Los doce archivos suman 3.273.484 bytes y sus hashes son distintos. Todos conservan
la duración exacta, son estéreo de 44,1 kHz y no tienen muestras saturadas.

El nuevo `DreamAudio.kt` compiló contra una API Android real con Kotlin 1.9.24.
Pasaron 44 comprobaciones de estado con reproductores y animaciones simulados:
carga diferida, doce rutas, límite de cuatro reproductores, cambios rápidos,
continuidad al interrumpir fundidos, pausa/reanudación, recurso ausente y liberación.
Estas pruebas no sustituyen escuchar la mezcla y comprobar el decodificador,
pausa y portales en un teléfono Android. Esa prueba real sigue pendiente.

`AUDIO_1_3.md` y `audio_validation.json` documentan la versión anterior del abismo;
este documento y `audio_validation_14.json` describen la música actual.
