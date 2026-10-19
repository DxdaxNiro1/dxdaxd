# DreamCore — actualizaciones Android

## Base 1.3 completa para Android Studio

[**Descargar proyecto completo DreamCore 1.3**](https://github.com/DxdaxNiro1/dxdaxd/raw/refs/heads/main/DreamCore_1_3_Proyecto_Completo.zip)

Este ZIP contiene una carpeta **DreamCore** que puedes abrir directamente con **Android Studio → Open**. Incluye Gradle/wrapper, módulo app, manifiesto, código, texturas y sonidos. No es solo una actualización.

Se reconstruyó a partir del código y APK del usuario y los cambios 1.3. La base compiló y generó un APK con firma válida. También compiló la 1.4 al aplicar su paquete sobre una copia de esta base. Los APK no se probaron en un teléfono y el ZIP entrega solo el proyecto fuente.

Después de abrir y probar la base, puedes aplicar el [paquete de actualización 1.4](https://github.com/DxdaxNiro1/dxdaxd/raw/refs/heads/main/DreamCore_1_4_Mundos.zip) indicando la ruta de esa carpeta DreamCore en el instalador. [Instrucciones completas](DreamCore_1_3_Proyecto_Completo/LEEME_ABRIR_EN_ANDROID_STUDIO.md).

## DreamCore 1.4: Mundos que no terminan

[**Descargar actualización 1.4**](https://github.com/DxdaxNiro1/dxdaxd/raw/refs/heads/main/DreamCore_1_4_Mundos.zip)

Cuatro mapas nuevos inspirados en las referencias del usuario: ajedrez gigante, túneles de piscinas, hotel sobre nubes y carretera nostálgica. Doce mapas en total, cada uno con música ambiental original propia. Nubes suaves iluminadas que responden a la cámara, materiales pulidos y carga de un mapa por vez. Incluye las caídas y el regreso de la casa central del abismo 1.3.

Extrae todo el ZIP, abre `Instalar_Windows.cmd` e indica la carpeta de tu proyecto. Crea una copia de seguridad antes de actualizar. También hay [instrucciones manuales](DreamCore_1_4_Mundos/LEEME.md). Se deben instalar todos los 15 Kotlin, músicas y textura de nubes; conserva PlayerState y los recursos originales.

Los cuatro portales nuevos están en el jardín, al otro lado de la estación. Cada mapa permite regresar al inicio y pasar al siguiente.

Código Kotlin compilado, shaders enlazados y mapas renderizados en GLES2/Mesa. Pruebas de física, recorridos, suelo y audio correctas. El ZIP 1.4 es un parche de código y recursos. Su aplicación sobre la nueva base 1.3 fue comprobada compilando un APK; no se probaron el teléfono ni el instalador en Windows. Las nubes son una aproximación por capas, no una simulación volumétrica completa.

### Vistas con el mapa y shaders reales

![Ajedrez gigante](DreamCore_1_4_Mundos/vistas/ajedrez.png)

![Túneles](DreamCore_1_4_Mundos/vistas/tuneles.png)

![Hotel](DreamCore_1_4_Mundos/vistas/hotel.png)

![Carretera](DreamCore_1_4_Mundos/vistas/carretera.png)

Renderizados de escritorio sin interfaz táctil; no establecen rendimiento en el teléfono. Las carpetas 1.2 y 1.3 conservan las versiones anteriores.
