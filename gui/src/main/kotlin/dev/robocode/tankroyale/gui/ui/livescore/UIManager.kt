package dev.robocode.tankroyale.gui.ui.livescore

import dev.robocode.tankroyale.client.model.Participant
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.arena.BattlePanel

object UIManager {
    // The live score panel is now part of the BattlePanel
    private val liveScorePanel: LiveScorePanel by lazy { BattlePanel.liveScorePanel }
    private var currentParticipants: List<Participant> = emptyList()

    init {
        initialize()
    }
    // In UIManager.kt
    fun initialize() {
        ClientEvents.onGameStarted.subscribe(this) { event ->
            // Save the current participants
            currentParticipants = event.participants

            // Update the embedded live score panel
            liveScorePanel.clearPanels()
            liveScorePanel.updateParticipants(event.participants)
            
            // Automatically show the live score panel when game starts
            showLiveScoreFrame()
        }

        ClientEvents.onGameEnded.subscribe(this) {
            // You might want to clear participants when the game ends
            // currentParticipants = emptyList()
            // liveScorePanel.clearPanels()
        }
    }

    fun showLiveScoreFrame() {
        // Make the embedded panel visible
        liveScorePanel.isVisible = true
        liveScorePanel.revalidate()
        liveScorePanel.repaint()
    }

    fun hideLiveScoreFrame() {
        liveScorePanel.isVisible = false
    }

    fun updateLiveScores(tickScoresJson: String) {
        liveScorePanel.updateWithTickScores(tickScoresJson)
    }

    fun getCurrentParticipants(): List<Participant> {
        return currentParticipants
    }
}