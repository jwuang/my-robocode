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
    
    fun initialize() {
        // Subscribe to events to manage the live score frame
        ClientEvents.onGameStarted.subscribe(this) { event ->
            // 保存当前参与者信息
            currentParticipants = event.participants
            // 新比赛开始时清空实时排行榜数据
            liveScoreFrame?.clearScores()
            // 更新实时排行榜中的参与者信息
            liveScoreFrame?.updateParticipants(event.participants)
        }

        ClientEvents.onGameEnded.subscribe(this) {

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
