package dev.robocode.tankroyale.gui.ui.livescore

import dev.robocode.tankroyale.client.model.Participant
import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.Timer
import kotlin.math.max
import kotlin.math.roundToInt

class LiveScoreFrame : RcFrame("实时积分排行榜", isTitlePropertyName = false) {

    private val participants = mutableMapOf<Int, Participant>()
    private var pendingTickScores: JsonArray? = null
    private var rankedParticipantIds: List<Int> = emptyList()

    // New components for graphical layout
    private val backgroundPanel = BackgroundPanel()
    private val robotPanels = mutableMapOf<Int, RobotScorePanel>()
    private val panelCurrentPositions = mutableMapOf<Int, Point>()
    private val panelTargetPositions = mutableMapOf<Int, Point>()

    // Timers
    private val dataUpdateTimer: Timer // Throttles incoming data
    private val animationTimer: Timer  // Drives smooth animation

    companion object {
        private const val PANEL_HEIGHT = 80
        private const val PANEL_WIDTH = 820
        private const val ANIMATION_SPEED = 0.15 // Higher is faster
        private const val INTERVAL = 10
    }

    init {
        contentPane = backgroundPanel
        setSize(2730, 1535)
        setLocationRelativeTo(null)

        // This timer throttles how often we process new score data
        dataUpdateTimer = Timer(100) {
            pendingTickScores?.let { processScores(it) }
            pendingTickScores = null
        }
        dataUpdateTimer.start()

        // This timer runs the animation loop
        animationTimer = Timer(16) { // Approx. 60 FPS
            animatePanels()
        }

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                dataUpdateTimer.stop()
                animationTimer.stop()
                UIManager.hideLiveScoreFrame()
            }
        })

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                recalculateTargetPositions()
            }
        })

        subscribeToEvents()
        updateParticipantsFromCurrentGame()
    }
    // In LiveScoreFrame.kt
    private fun subscribeToEvents() {
        ClientEvents.onGameStarted.subscribe(this) { event ->
            // Clear everything for the new game
            clearPanels() // Use the new method here
            updateParticipants(event.participants)
        }
    }

    fun updateParticipants(participantsList: List<Participant>) {
        participants.clear()
        participantsList.forEach { participants[it.id] = it }
    }

    private fun updateParticipantsFromCurrentGame() {
        val currentParticipants = UIManager.getCurrentParticipants()
        if (currentParticipants.isNotEmpty()) {
            updateParticipants(currentParticipants)
        }
    }

    fun updateWithTickScores(tickScoresJson: String) {
        try {
            val jsonObject = Json.Default.parseToJsonElement(tickScoresJson).jsonObject
            pendingTickScores = jsonObject["tickScores"]?.jsonArray
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processScores(tickScores: JsonArray) {
        // Store the ranked list of participant IDs
        rankedParticipantIds = tickScores.map { it.jsonObject["participantId"]?.jsonPrimitive?.int ?: 0 }

        // 1. Calculate max scores for consistent bar scaling
        val maxScores = mutableMapOf(
            "total" to 1.0, "survival" to 1.0, "lastSurvivor" to 1.0,
            "bulletDmg" to 1.0, "bulletKill" to 1.0, "ramDmg" to 1.0, "ramKill" to 1.0
        )
        tickScores.forEach { element ->
            val score = element.jsonObject
            maxScores["total"] = max(maxScores["total"]!!, score["totalScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0)
            maxScores["survival"] =
                max(maxScores["survival"]!!, score["survivalScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0)
            maxScores["lastSurvivor"] =
                max(maxScores["lastSurvivor"]!!, score["lastSurvivorBonus"]?.jsonPrimitive?.doubleOrNull ?: 0.0)
            maxScores["bulletDmg"] =
                max(maxScores["bulletDmg"]!!, score["bulletDamageScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0)
            maxScores["bulletKill"] =
                max(maxScores["bulletKill"]!!, score["bulletKillBonus"]?.jsonPrimitive?.doubleOrNull ?: 0.0)
            maxScores["ramDmg"] =
                max(maxScores["ramDmg"]!!, score["ramDamageScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0)
            maxScores["ramKill"] =
                max(maxScores["ramKill"]!!, score["ramKillBonus"]?.jsonPrimitive?.doubleOrNull ?: 0.0)
        }

        // Create a map for quick lookup of scores by participant ID
        val scoresByParticipantId = tickScores.associateBy {
            it.jsonObject["participantId"]?.jsonPrimitive?.int ?: 0
        }

        // 2. Update panels and calculate target positions
        rankedParticipantIds.forEach { participantId ->
            val scoreObject = scoresByParticipantId[participantId] ?: return@forEach // Skip if score not found
            val score = scoreObject.jsonObject
            val participant = participants[participantId]
            val name = participant?.name ?: "Bot $participantId"

            // Get or create the panel
            val panel = robotPanels.getOrPut(participantId) {
                val newPanel = RobotScorePanel(participantId)
                backgroundPanel.add(newPanel)
                // Start new panels off-screen to slide in
                val startPos = Point(10, -PANEL_HEIGHT)
                newPanel.setBounds(startPos.x, startPos.y, PANEL_WIDTH, PANEL_HEIGHT)
                panelCurrentPositions[participantId] = startPos
                newPanel
            }

            // Update its data
            panel.updateScores(score, name, maxScores)
        }

        // 3. Recalculate all positions based on new data and current window size
        recalculateTargetPositions()
    }

    private fun recalculateTargetPositions() {
        if (rankedParticipantIds.isEmpty()) return

        val panelCount = rankedParticipantIds.size
        val totalPanelsHeight = panelCount * PANEL_HEIGHT + max(0, panelCount - 1) * INTERVAL

        // Calculate centered starting position
        val startX = (contentPane.width - PANEL_WIDTH) / 2
        val startY = (contentPane.height - totalPanelsHeight) / 2

        // Update target position for each panel based on its rank
        rankedParticipantIds.forEachIndexed { index, participantId ->
            val targetY = startY + (index * (PANEL_HEIGHT + INTERVAL))
            panelTargetPositions[participantId] = Point(startX, targetY)
        }

        // Start animation if not already running to move panels to their new target positions
        if (!animationTimer.isRunning) {
            animationTimer.start()
        }
        // If animation is already running, it will just pick up the new target positions on its next tick.
    }

    private fun animatePanels() {
        var allInPlace = true
        robotPanels.keys.forEach { id ->
            val currentPos = panelCurrentPositions[id] ?: return@forEach
            val targetPos = panelTargetPositions[id] ?: return@forEach
            val panel = robotPanels[id] ?: return@forEach

            // Simple linear interpolation (lerp) for smooth movement
            val newX = (currentPos.x + (targetPos.x - currentPos.x) * ANIMATION_SPEED).roundToInt()
            val newY = (currentPos.y + (targetPos.y - currentPos.y) * ANIMATION_SPEED).roundToInt()

            panel.setLocation(newX, newY)
            panelCurrentPositions[id] = Point(newX, newY)

            // Check if this panel is at its destination
            if (newX != targetPos.x || newY != targetPos.y) {
                allInPlace = false
            }
        }

        // Stop the animation timer if all panels have reached their destination
        if (allInPlace) {
            animationTimer.stop()
        }

        // Repaint the main panel to show the updated positions
        backgroundPanel.repaint()
    }

    override fun dispose() {
        dataUpdateTimer.stop()
        animationTimer.stop()
        super.dispose()
    }
    // Add this function inside the LiveScoreFrame class
    fun clearPanels() {
        backgroundPanel.removeAll()
        robotPanels.clear()
        panelCurrentPositions.clear()
        panelTargetPositions.clear()
        backgroundPanel.repaint()
    }
}