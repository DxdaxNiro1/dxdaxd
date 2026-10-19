# Comprobaciones en JVM

Los tests usan las mismas clases puras de geometría, catálogo y física del juego. No sustituyen probar el APK y los controles en Android.

Con Kotlin y JDK instalados, desde la carpeta de este paquete en PowerShell:

```powershell
$sources = Get-ChildItem app\src\main\java\com\dreamcore\game\*.kt | Where-Object { (Get-Content $_.FullName -Raw) -notmatch 'import android\.' } | Select-Object -ExpandProperty FullName
kotlinc @sources tests\CraterPhysicsCheck.kt tests\RadialFallCheck.kt tests\ExpansionValidation.kt -include-runtime -d gameplay-checks.jar
java -cp gameplay-checks.jar com.dreamcore.game.CraterPhysicsCheckKt
java -cp gameplay-checks.jar com.dreamcore.game.RadialFallCheckKt
java -cp gameplay-checks.jar com.dreamcore.game.ExpansionValidationKt
```

La compilación debe incluir el catálogo y los cuatro mapas nuevos, pues `DreamWorlds.canWalk` utiliza el catálogo. Las comprobaciones del cráter cubren diferentes frecuencias de fotogramas y 24 rutas radiales. Los informes de la versión 1.4 añaden rutas entre portales y geometría de la ampliación.
