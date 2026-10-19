# Pruebas 1.7

Desde la raíz del paquete, con JDK y Kotlin instalados, en PowerShell:

```powershell
$sources = Get-ChildItem app\src\main\java\com\dreamcore\game\*.kt | Where-Object { (Get-Content $_.FullName -Raw) -notmatch 'import android\.' } | Select-Object -ExpandProperty FullName
kotlinc @sources tests\CraterPhysicsCheck.kt tests\RadialFallCheck.kt tests\Journey17Check.kt tests\MoonTravel17Check.kt -include-runtime -d gameplay-checks.jar
java -cp gameplay-checks.jar com.dreamcore.game.Journey17CheckKt
java -cp gameplay-checks.jar com.dreamcore.game.CraterPhysicsCheckKt
java -cp gameplay-checks.jar com.dreamcore.game.RadialFallCheckKt
java -cp gameplay-checks.jar com.dreamcore.game.MoonTravel17CheckKt
```

Resultados: 655 comprobaciones de rutas/mallas/secretos, 36 de física, 480 de caídas desde el borde y 81 de lunas/carrera/recuerdos: **1.252**. La luna se prueba a 10/20/30/60/120 pasos por segundo: no aparece antes de los 120 s, aparece una vez, vuelve a parpadear, reinicia su vigilia al apartar la vista y no cuenta una luna tapada por geometría. Se prueban pausa, salida de Dark, mirada, paredes y estanques al correr rápido. Esas frecuencias son simulaciones, no FPS medidos en un teléfono.

El script exporta mallas y estados reales de animación a build/journey-validation. Los shaders normales y su alternativa mediump se enlazan mediante GLES2/Mesa. La APK se compila con Gradle y se verifican firma, versión y recursos. Los OGG nuevos se decodifican antes de medir picos, RMS y clipping.

Pendiente en teléfono: probar carrera y esquinas, los tres modos de pasos, pausa/reanudación, entrar y salir de portales, reacción lunar frente a movimiento, observación de dos minutos y rendimiento con la luna gigante. El instalador de Windows tampoco se ejecutó en esta máquina Linux.
