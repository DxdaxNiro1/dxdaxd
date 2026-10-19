# Comprobaciones de jugabilidad 1.5

Con JDK y Kotlin instalados, desde esta carpeta en PowerShell:

```powershell
$sources = Get-ChildItem app\src\main\java\com\dreamcore\game\*.kt | Where-Object { (Get-Content $_.FullName -Raw) -notmatch 'import android\.' } | Select-Object -ExpandProperty FullName
kotlinc @sources tests\CraterPhysicsCheck.kt tests\RadialFallCheck.kt tests\Journey15Check.kt -include-runtime -d gameplay-checks.jar
java -cp gameplay-checks.jar com.dreamcore.game.CraterPhysicsCheckKt
java -cp gameplay-checks.jar com.dreamcore.game.RadialFallCheckKt
java -cp gameplay-checks.jar com.dreamcore.game.Journey15CheckKt
```

Resultados: 36 comprobaciones de física a cuatro frecuencias de fotogramas, 480 de caída/ruta desde 24 ángulos y 258 del recorrido 1.5, mallas, llegadas, repisas y Dark Fantasy. Los ejecutables usan las mismas clases puras del juego. No sustituyen una prueba del APK y controles en el teléfono.
