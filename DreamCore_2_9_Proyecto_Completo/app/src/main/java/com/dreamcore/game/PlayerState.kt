package com.dreamcore.game

// Un jugador tiene identidad y estado independientes de la pantalla.
// Más adelante podremos sincronizar estos valores por Wi-Fi / Internet.
data class PlayerState(
    val id: String = "player_1",
    var x: Float = 0f,
    var y: Float = 1.65f,
    var z: Float = 6f,
    var yaw: Float = 0f,
    var worldId: String = DreamWorlds.STATION
)
