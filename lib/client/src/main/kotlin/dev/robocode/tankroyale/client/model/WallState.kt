package dev.robocode.tankroyale.client.model

import kotlinx.serialization.Serializable

@Serializable
data class WallState(
    val id: Int,
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val rotation: Double = 0.0,
    val color: String? = null
)