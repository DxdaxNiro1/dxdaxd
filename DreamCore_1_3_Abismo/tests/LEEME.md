# Comprobaciones de física en JVM

Estos ejecutables ejercitan las mismas clases de geometría y física que el juego. No son sustitutos de probar la interfaz táctil o el APK.

Con JDK y Kotlin instalados, desde la raíz de este paquete:

```sh
kotlinc app/src/main/java/com/dreamcore/game/DreamWorlds.kt app/src/main/java/com/dreamcore/game/DreamCraterPhysics.kt tests/CraterPhysicsCheck.kt -include-runtime -d physics-check.jar
java -jar physics-check.jar
kotlinc app/src/main/java/com/dreamcore/game/DreamWorlds.kt app/src/main/java/com/dreamcore/game/DreamCraterPhysics.kt tests/RadialFallCheck.kt -include-runtime -d radial-check.jar
java -jar radial-check.jar
```

Resultados comprobados: 36 verificaciones de salto, caída, techo, colisiones y puerta; 480 verificaciones adicionales de rutas desde 24 puntos del borde. La física se ejecuta a cuatro frecuencias de fotogramas para detectar dependencia del tiempo de cuadro.
