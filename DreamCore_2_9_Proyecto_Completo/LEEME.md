# DreamCore 2.9 · Compatibilidad

Correcciones del renderizado y adaptación para más GPU y versiones de Android, a partir de 2.8. Conserva los **51 escenarios**, las **52 músicas**, las texturas y modelos originales, las funciones lunares y el guardado. [Ver los tres perfiles del motor](VISTAS.md).

## Instalar la actualización

1. Extrae TODO el ZIP y cierra Android Studio.
2. Ejecuta `Instalar_Windows.cmd` e indica la carpeta de tu proyecto DreamCore.
3. El instalador respalda `app/src/main` y `app/build.gradle.kts`, copia código, recursos y los dos temas nuevos. Aplica **versionCode 29, versionName 2.9, minSdk 19** y el tema compatible del manifiesto.
4. Abre Android Studio, sincroniza y compila `gradlew.bat :app:assembleDebug`.
5. Mantén tu mismo identificador y firma para actualizar tu instalación y conservar la partida.

El proyecto completo se abre por la carpeta que contiene `settings.gradle.kts`. Requiere JDK 17 compatible y SDK 34 para compilar. El mínimo declarado y auditado es **Android 4.4 / API 19 con GLES2**; el APK no incorpora librerías nativas ni restringe la arquitectura a ARM de 64 bits. Esto amplía el mínimo anterior de Android 5.0; la instalación posible no garantiza fluidez en cualquier equipo antiguo.

Instalación manual: respalda las dos rutas anteriores; copia todo `app/src/main/java/com/dreamcore/game`, `res/raw`, `res/drawable-nodpi` y los nuevos `res/values/dream_theme_29.xml` y `res/values-v21/dream_theme_29.xml`. En el Gradle de la aplicación aplica las tres versiones indicadas. En `AndroidManifest.xml`, cambia el tema de la aplicación de `@android:style/Theme.Material.NoActionBar` a `@style/DreamTheme29`, conservando identificadores y componentes. Si tu proyecto tiene un tema personalizado, intégralo con una alternativa de API 19; el instalador se detiene ante un tema que no reconoce para evitar reemplazarlo.

Para restaurar: cierra Android Studio y repón `main` y el Gradle desde la copia indicada por el instalador. El instalador se revisó por código en Linux; no se ejecutó en Windows.

## Qué cambió

- Se eliminan operaciones GLSL indefinidas en cuadrados de distancias negativas usados por párpados, ojos y luces. Los cuadrados usan multiplicación y límites seguros; otras potencias reciben una base no negativa. La normalización protege vectores nulos y evita desbordamientos de la precisión mínima de GLES2.
- Las texturas artísticas y fotografías se transfieren con canales **RGBA8 explícitos**, usando decodificación ARGB de 8 bits y sRGB cuando Android lo permite. Se reutiliza el bloque nativo de subida. Si falla la generación de mipmaps se conserva la textura base y se usa filtrado lineal.
- Al arrancar se consultan las capacidades de la GPU y se comprueban los canales de un destino RGBA. La ruta completa también comprueba una lectura real de profundidad, con un valor conocido, antes de habilitarla. Se evita leer texturas unidas al destino de dibujo.
- El **Adreno 619** del POCO X4 Pro 5G recibe por defecto la ruta estable. También se aplica de forma conservadora a Adreno 610/612/616/618 y generaciones anteriores a 600. Mantiene los materiales, reflejos, sombras y nubes compatibles, evitando la profundidad leída por la oclusión de contacto y el descarte de nubes ocultas.
- Las GPU sin precisión de fragmento suficiente o con el mínimo de ocho vectores de interpolación usan iluminación esencial. Los programas compatibles conservan mundos y texturas, con iluminación simplificada.
- Se selecciona una superficie EGL de ventana con canales y profundidad explícitos, prefiriendo RGB de 8 bits y evitando elegir por accidente una configuración con multisampling costoso. Existen alternativas de profundidad de 16 bits y RGB565.
- Solo queda una solicitud de dibujo pendiente desde el programador de cuadros. En energía Automática, la resolución interna responde a retrasos sostenidos y se recupera gradualmente; no cambia tus texturas, geometría, distancias ni calidad guardada. La opción Resolución completa conserva el tamaño nativo, sujeto al máximo admitido por la GPU.
- Se mantienen las optimizaciones de mallas, memoria y viajes de 2.8. Las lunas siguen parpadeando, siguiendo la mirada, cambiando a rojo al minuto y mostrando la aparición gigante a los dos minutos.

## Si persisten los colores incorrectos

En **Ajustes → Compatibilidad** hay tres opciones: Automática, Estable y Esencial. El cambio guarda la partida y reinicia la actividad. Prueba Estable y, si sigue ocurriendo, Esencial. Son rutas distintas del motor; no basta con bajar la opción Calidad para cambiar de ruta. **Información gráfica** muestra la GPU y el perfil que se están usando, útil para identificar el Redmi concreto.

No se identificó todavía el modelo del Redmi reportado. No se presupone que todos los teléfonos de esa marca tengan la misma GPU.

## Validación y límites

Compilación Android y Android Lint con mínimo API 19. Las geometrías generadas de los 51 escenarios coinciden con 2.8; los 148 recursos anteriores se conservan byte por byte. Se añaden únicamente dos XML de tema para las versiones de Android.

Se enlazan 416 variantes especializadas y se ejecutan 74 casos de matemática gráfica en GLES2. Se comparan 52 pares del catálogo y 26 pares de estados/calidad con 2.8, y se renderizan otros 102 cuadros: todos los mundos por las rutas estable y esencial/mediump. Los informes están en `tools/validacion29` y `VALIDACION.json`.

Las pruebas gráficas usan **Mesa/llvmpipe de escritorio**. Las pruebas de fallos de capacidades registran llamadas GL simuladas, además de la comprobación real que el APK ejecuta al arrancar. **No se ha probado físicamente el POCO, el Redmi ni un teléfono con Android 4.4.** Se corrigen causas concretas de resultados indefinidos y se añaden alternativas; todavía hay que confirmar en los teléfonos si desaparece el verde y cómo cambian fluidez y temperatura.
