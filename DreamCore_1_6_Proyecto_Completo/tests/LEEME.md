# Comprobaciones de jugabilidad 1.6

Desde la raíz del paquete, con JDK y Kotlin disponibles, PowerShell:

```powershell
$sources = Get-ChildItem app\src\main\java\com\dreamcore\game\*.kt | Where-Object { (Get-Content $_.FullName -Raw) -notmatch 'import android\.' } | Select-Object -ExpandProperty FullName
kotlinc @sources tests\CraterPhysicsCheck.kt tests\RadialFallCheck.kt tests\Journey16Check.kt -include-runtime -d gameplay-checks.jar
java -cp gameplay-checks.jar com.dreamcore.game.CraterPhysicsCheckKt
java -cp gameplay-checks.jar com.dreamcore.game.RadialFallCheckKt
java -cp gameplay-checks.jar com.dreamcore.game.Journey16CheckKt
```

Las pruebas usan las clases reales del juego: mallas, llegadas, portales, búsqueda de rutas y secretos, casa/piscina, cambio del pasillo, reacción de la presencia y separación de zonas. El hoyo se prueba a 20/30/60/120 FPS y desde 24 ángulos. Los FPS son pasos de simulación, no mediciones del teléfono. El volcado de mallas se guarda en build/journey-validation.

Prueba móvil pendiente: abrir y salir de cada experiencia; caer al hoyo; usar joystick, mirada, salto y carrera simultáneamente; entrar/salir de la casa pequeña; mirar el espejo desde varios ángulos; pausar/reanudar; escuchar transiciones y pasos. En Windows falta ejecutar el instalador real.
