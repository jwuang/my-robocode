package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.client.ClientEvents

object UIManager {
    private var liveScoreFrame: LiveScoreFrame? = null

    init {
        initialize()
    }
    fun initialize() {
        // Subscribe to events to manage the live score frame
        ClientEvents.onGameStarted.subscribe(this) {
            // 新比赛开始时清空实时排行榜数据
            liveScoreFrame?.clearScores()
        }

        ClientEvents.onGameEnded.subscribe(this) {

        }
    }

    fun showLiveScoreFrame() {
        if (liveScoreFrame == null) {
            liveScoreFrame = LiveScoreFrame()
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
}
