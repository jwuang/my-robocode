package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.client.model.GameStartedEvent
import dev.robocode.tankroyale.client.model.Participant

object UIManager {
    private var liveScoreFrame: LiveScoreFrame? = null
    private var currentParticipants: List<Participant> = emptyList()

    init {
        initialize()
    }
    // In UIManager.kt
    fun initialize() {
        ClientEvents.onGameStarted.subscribe(this) { event ->
            // Save the current participants
            currentParticipants = event.participants

            // --- FIX ---
            // Instead of disposing the frame, clear it and update it.
            // This keeps the window alive if it was already open.
            liveScoreFrame?.apply {
                clearPanels()
                updateParticipants(event.participants)
            }
        }

        ClientEvents.onGameEnded.subscribe(this) {
            // You might want to clear participants when the game ends
            // currentParticipants = emptyList()
            // liveScoreFrame?.clearPanels()
        }
    }

    fun showLiveScoreFrame() {
        if (liveScoreFrame == null) {
            liveScoreFrame = LiveScoreFrame()
            // 如果已经有当前比赛的参与者信息，则更新到新创建的LiveScoreFrame中
            if (currentParticipants.isNotEmpty()) {
                liveScoreFrame?.updateParticipants(currentParticipants)
            }
        }
        liveScoreFrame?.isVisible = true
        liveScoreFrame?.toFront()
    }

    fun hideLiveScoreFrame() {
        liveScoreFrame?.isVisible = false
    }

    fun updateLiveScores(tickScoresJson: String) {
        liveScoreFrame?.updateWithTickScores(tickScoresJson)
    }
    
    fun getCurrentParticipants(): List<Participant> {
        return currentParticipants
    }
}
