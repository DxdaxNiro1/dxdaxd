# DreamCore 1.4 — Mundos que no terminan

Gran actualización para el proyecto Android Studio/Kotlin del usuario. Incluye el abismo 1.3 y añade cuatro mundos explorables, música propia para cada uno de los doce mapas y un cielo de nubes iluminadas. Aplica el paquete completo a tu proyecto existente: no es un proyecto Gradle independiente ni un APK precompilado.

## Instalar en Windows

1. Descarga el ZIP de la versión 1.4 y **extrae todo**.
2. Abre `Instalar_Windows.cmd` e introduce la ruta de tu proyecto DreamCore. El instalador encuentra la carpeta de los archivos Kotlin tanto si tu proyecto usa `java` como `kotlin`.
3. Antes de cambiar archivos, copia todo `app/src/main` a `DreamCore_backup_1_4_…` dentro de tu proyecto. Luego instala los archivos Kotlin, las músicas y la textura de nubes. No elimina tus recursos anteriores.
4. En la terminal PowerShell de Android Studio, desde la carpeta del proyecto:

```powershell
.\gradlew.bat assembleDebug
```

El APK de depuración suele quedar en `app/build/outputs/apk/debug/app-debug.apk`. Instálalo desde Android Studio para probar. Si compilas para distribuir, utiliza tu propia clave de firma y consérvala.

El instalador ha sido revisado pero no ejecutado en Windows en este entorno. Puedes usar la instalación manual equivalente.

### Instalación manual

Con copia de seguridad previa:

- Copia **todos los 15 archivos `.kt`** de `app/src/main/java/com/dreamcore/game/` del ZIP a la carpeta de tu proyecto que ya contiene `DreamWorlds.kt`. Reemplaza los del mismo nombre. Conserva tu `PlayerState.kt` y otros archivos originales.
- Copia **todos los archivos** de `app/src/main/res/raw/` del ZIP a esa misma carpeta de recursos de tu proyecto.
- Copia `app/src/main/res/drawable-nodpi/dream_cloud_density.png` a esa misma ubicación de tu proyecto; crea `drawable-nodpi` si hace falta.

No copies solo `DreamWorlds` y `DreamRenderer`: la versión 1.4 usa nuevos mapas, un catálogo, un constructor de mallas y la textura de cielo.

### Restaurar

Cierra el juego y Android Studio. Mueve tu carpeta actual `app/src/main` a una ubicación de respaldo, y copia la carpeta `main` de `DreamCore_backup_1_4_…` a `app/src/main`. Reponer la carpeta completa evita dejar helpers nuevos que dependen de clases de la actualización.

## Dónde están los mapas nuevos

En la estación inicial, gira hacia el lado contrario de los portales antiguos. Hay **cuatro puertas nuevas**, alineadas en el jardín con iconos distintos. Acércate y pulsa **ENTRAR**; la interfaz muestra el destino.

| Mapa | Puerta de la estación | Escenario |
| --- | --- | --- |
| 09 — Ajedrez del cielo inmóvil | Primera, con corona | Tablero monumental, 32 piezas modeladas, suelo pulido, columnas y jardines. |
| 10 — Túneles de agua y porcelana | Segunda, con aro | Cinco salas de piscinas, bóvedas, azulejos de colores, galerías y agua iluminada. |
| 11 — Hotel sobre las nubes | Tercera, con ventanas | Habitaciones, patios y arcadas rodeados de un mar de nubes rosadas. |
| 12 — La carretera del último recuerdo | Cuarta, con señal de camino | Carretera húmeda, máquinas expendedoras, parada y casa entre praderas. |

Al llegar, la puerta que queda detrás vuelve a la estación. La puerta del extremo opuesto lleva al siguiente mapa nuevo: **ajedrez → túneles → hotel → carretera → ajedrez**. Todos tienen salida al inicio.

Los ocho mapas anteriores siguen disponibles. El cráter conserva la caída, el agua transitable y el regreso desde la casa central de la versión 1.3. Los pisos superiores del hotel son decorativos: el recorrido se realiza por patios y habitaciones a nivel del suelo. No se añadió multijugador ni una partida de ajedrez con reglas: el ajedrez es un mundo de exploración.

## Gráficos y cielo

- Se sustituyen las antiguas nubes sólidas por capas de densidad suave, textura detallada, luz de sol y sombras internas aproximadas. El cielo responde a la dirección de la cámara, con vientos distintos en sus capas.
- El hotel incorpora un mar de nubes visible por debajo de sus patios y pasarelas.
- Mármol y cerámica con luz y reflejos de cielo aproximados; agua con ondas y reflejo ambiental. Los reflejos no capturan todos los objetos de la escena.
- Materiales antiguos menos ruidosos, texturas filtradas, sombras de contacto precalculadas y nueva paleta de iluminación en los cuatro mapas.
- Solo se conserva la malla del mapa activo en la GPU. Esto evita sumar los buffers de los doce escenarios, aunque cada cambio de mapa necesita construir y cargar su geometría.

Estas nubes buscan un aspecto más natural dentro de OpenGL ES 2.0; no son una simulación volumétrica completa ni una promesa de hiperrealismo. Su calidad y coste deben comprobarse en tu teléfono.

## Música

Cada uno de los doce mapas, incluidos ambos interiores, tiene una composición ambiental original distinta. Son bucles de 32 o 48 segundos, con motivos, acordes y timbres propios. El ajedrez usa campanas y notas espaciadas; los túneles, sonidos de cristal; el hotel, acordes suaves; la carretera, una melodía nostálgica.

Las transiciones mantienen el volumen al interrumpirse, y la carga de audio se limita a dos músicas y dos capas simultáneas. Las grabaciones de los videos de referencia no se han copiado ni identificado: la música de esta actualización es original. Hay detalles y medidas en `tools/AUDIO_1_4.md`.

## Prueba en el teléfono

Entra por las cuatro puertas de la estación y recorre el circuito completo. Comprueba la salida de cada mapa, las colisiones de las piezas de ajedrez, las cinco salas de piscinas, habitaciones y patios del hotel y la entrada de la casa de la carretera. Mira hacia arriba y gira la cámara: las nubes deben permanecer en el mundo y moverse lentamente.

Vuelve al cráter y comprueba caída y retorno. Pausa el juego y reanuda en distintos mapas; escucha que cambia la música y no quedan temas anteriores sonando. Revisa fluidez y temperatura durante varios minutos. Si falla algo, comparte captura de Logcat o del error y el nombre del mapa.

## Validación y límites

Se comprueban compilación Kotlin, enlace real de shaders GLES2, mallas finitas, rutas entre portales, regresión del cráter, recursos y señales de audio. El archivo `VALIDACION.json` registra los resultados finales del paquete. Las imágenes de `vistas/` muestran geometría, shaders y texturas reales renderizados en OpenGL ES2 de escritorio; no son fotografías ni capturas del teléfono.

No se generó ni instaló un APK en este entorno. No se ha probado este instalador en Windows ni la GPU, la escucha real o los controles táctiles en tu dispositivo. Las pruebas de escritorio no establecen los FPS del teléfono.
