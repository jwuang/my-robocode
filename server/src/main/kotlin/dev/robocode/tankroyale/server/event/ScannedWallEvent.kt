package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.BotId
/** Event sent when a wall got scanned. */
class ScannedWallEvent (
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bot id of the bot that scanned the bot */
    val scannedByBotId: BotId,

    /** Bot id of the bot that got scanned */
    val scannedWallId: Int,

    /** X coordinate of the scanned bot */
    val x: Double,

    /** Y coordinate of the scanned bot */
    val y: Double,

    /** Width of the scanned wall */
    val width: Double,

    /** Height of the scanned wall */
    val height: Double,

    /** Rotation of the scanned wall */
    val rotation: Double,

    ): Event()