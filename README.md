# DreamCore 2.7 · Vigilia

[Descargar actualización para tu proyecto](https://github.com/DxdaxNiro1/dxdaxd/raw/refs/heads/main/DreamCore_2_7_Vigilia.zip) · [Proyecto completo para Android Studio](https://github.com/DxdaxNiro1/dxdaxd/raw/refs/heads/main/DreamCore_2_7_Proyecto_Completo.zip) · [Ver imágenes reales del motor](DreamCore_2_7_Vigilia/VISTAS.md)

Lunas modeladas con parpadeo y mirada: las azules se vuelven rojas al minuto y la luna gigante sigue apareciendo a los dos minutos. Dos mundos nuevos de Dark Fantasy, cuatro criaturas en reposo y una isla entre nubes sin tobogán, observada por una figura alada gigantesca. 51 escenarios y 52 músicas. [Instalación y cambios](DreamCore_2_7_Vigilia/LEEME.md).

<details>
<summary>Versiones anteriores</summary>

# DreamCore · 2.6 Profundidad

Juego Android Kotlin / GLES2. Revisión del motor compartido inspirada en las portadas: oclusión de contacto, suavizado, nubes con volumen, vegetación recortada y agua transparente con reflejos de la sala. Se remodelan especialmente las piscinas, el pueblo y las casas compartidas. [Comparar vistas reales del motor](DreamCore_2_6_Profundidad/VISTAS.md).

- [Descargar actualización 2.6](https://github.com/DxdaxNiro1/dxdaxd/raw/refs/heads/main/DreamCore_2_6_Profundidad.zip)
- [Proyecto completo 2.6 para Android Studio](https://github.com/DxdaxNiro1/dxdaxd/raw/refs/heads/main/DreamCore_2_6_Proyecto_Completo.zip)
- [Instalación y cambios](DreamCore_2_6_Profundidad/LEEME.md)
- [Presupuesto gráfico y alternativas por calor/capacidad](DreamCore_2_6_Profundidad/RENDIMIENTO_2_6.txt)
- [Informe de validación](DreamCore_2_6_Profundidad/VALIDACION.json)
- [Álbum MP3: nombres, miniaturas y reproductor](https://github.com/DxdaxNiro1/dxdaxd/raw/refs/heads/main/DreamCore_2_4_Banda_Sonora.zip)
- [Catálogo individual de las 49 músicas](DreamCore_2_4_Resonancias/Banda_sonora/CATALOGO.md)
- [49 portadas ilustradas con sus títulos](DreamCore_2_4_Portadas/GALERIA.md)
- [Kit Dreamcore de NiroDxdax para YouTube](NiroDxdax_Kit_YouTube_Dreamcore/GALERIA.md)
- [49 videos musicales a 1080p con marca de agua NiroDxdax](NiroDxdax_Videos_Dreamcore/DESCARGAR.md)
- [Plan del juego](PLAN_DEL_JUEGO.md)

Extrae TODO el ZIP de actualización y ejecuta **Instalar_Windows.cmd** sobre tu proyecto. Crea una copia de seguridad antes de copiar. Compila e instala con tu mismo identificador y firma para conservar la partida. El proyecto completo se abre por la carpeta con `settings.gradle.kts`. SDK 34 / JDK 17 compatible / minSdk 21.

Se mantienen los 48 escenarios, las 49 composiciones, los secretos y el guardado de esquema 23. Las músicas conservan exactamente los archivos anteriores. En el juego: **Sala de escucha**, desde el inicio o Ajustes. El álbum y los videos se descargan por separado.

Verificación 2.6: APK compilado y firma comprobada; **80.032 comprobaciones** de lógica/geometría; trece combinaciones de shaders enlazadas en GLES2/Mesa; 49 vistas del catálogo en Media, nueve en Baja/mediump, dos en Alta y una con profundidad de renderbuffer sin AO. Las pruebas de AO comprueban que un plano no se oscurece a sí mismo y que el contacto con un objeto sí produce una sombra localizada.

**Media a 30 FPS** es el punto de partida para el Infinix NOTE 50s. Los efectos nuevos tienen coste adicional y se reducen con calor o Ahorro; las nubes y la oclusión trabajan a media resolución. No se han medido temperatura, autonomía ni FPS en el teléfono físico. Las portadas son la dirección artística: las comparaciones muestran el acabado actual del motor, que todavía no es idéntico a las ilustraciones.

</details>
