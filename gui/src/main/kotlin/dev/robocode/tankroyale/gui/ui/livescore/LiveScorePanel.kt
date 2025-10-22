package dev.robocode.tankroyale.gui.ui.livescore

import dev.robocode.tankroyale.client.model.Participant
import dev.robocode.tankroyale.gui.client.ClientEvents
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*
import javax.swing.Timer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class LiveScorePanel : JPanel() {

    private val participants = mutableMapOf<Int, Participant>()
    private var pendingTickScores: JsonArray? = null
    private var rankedParticipantIds: List<Int> = emptyList()

    private val backgroundPanel = BackgroundPanel().apply {
        layout = null // absolute positioning
    }

    private val robotPanels = mutableMapOf<Int, RobotScorePanel>()
    private val panelCurrentPositions = mutableMapOf<Int, Point>()
    private val panelTargetPositions = mutableMapOf<Int, Point>()

    // Timers
    private val dataUpdateTimer = Timer(100) {
        pendingTickScores?.let { processScores(it) }
        pendingTickScores = null
    }

    private val animationTimer = Timer(16) { // ~60 FPS
        animatePanels()
    }

    companion object {
        const val MIN_PANEL_WIDTH = 200
        const val MAX_PANEL_WIDTH = 400
        const val PANEL_HEIGHT = 50
        const val INTERVAL = 10
        const val ANIMATION_SPEED = 0.2f
        const val SLIDE_IN_OFFSET = 50 // pixels off-screen for entrance
    }

    init {
        layout = BorderLayout()
        minimumSize = Dimension(MIN_PANEL_WIDTH, 0)

        val scrollPane = JScrollPane(backgroundPanel).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            border = null
        }
        add(scrollPane, BorderLayout.CENTER)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                recalculateTargetPositions()
            }
        })

        subscribeToEvents()
        updateParticipantsFromCurrentGame()

        dataUpdateTimer.start()
    }

    // ✅ Dynamic preferred width based on parent
    override fun getPreferredSize(): Dimension {
        val parentWidth = parent?.width ?: MAX_PANEL_WIDTH
        val width = max(
            MIN_PANEL_WIDTH,
            min(parentWidth / 4, MAX_PANEL_WIDTH)
        )
        // Height is determined by content; delegate to backgroundPanel
        val height = backgroundPanel.preferredSize.height
        return Dimension(width, height)
    }

    private fun subscribeToEvents() {
        ClientEvents.onGameStarted.subscribe(this) { event ->
            clearPanels()
            updateParticipants(event.participants)
        }
    }

    fun updateParticipants(participantsList: List<Participant>) {
        participants.clear()
        participantsList.forEach { participants[it.id] = it }
    }

    private fun updateParticipantsFromCurrentGame() {
        val current = UIManager.getCurrentParticipants()
        if (current.isNotEmpty()) {
            updateParticipants(current)
        }
    }

    fun updateWithTickScores(tickScoresJson: String) {
        try {
            val obj = Json.parseToJsonElement(tickScoresJson).jsonObject
            pendingTickScores = obj["tickScores"]?.jsonArray
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processScores(tickScores: JsonArray) {
        // Step 1: Extract ranked IDs
        val newRankedIds = tickScores.mapNotNull {
            it.jsonObject["participantId"]?.jsonPrimitive?.int
        }

        // Step 2: Compute max scores for normalization
        val maxScores = mutableMapOf(
            "total" to 1.0,
            "survival" to 1.0,
            "lastSurvivor" to 1.0,
            "bulletDmg" to 1.0,
            "bulletKill" to 1.0,
            "ramDmg" to 1.0,
            "ramKill" to 1.0
        )

        tickScores.forEach { element ->
            val s = element.jsonObject
            updateMax(maxScores, "total", s["totalScore"])
            updateMax(maxScores, "survival", s["survivalScore"])
            updateMax(maxScores, "lastSurvivor", s["lastSurvivorBonus"])
            updateMax(maxScores, "bulletDmg", s["bulletDamageScore"])
            updateMax(maxScores, "bulletKill", s["bulletKillBonus"])
            updateMax(maxScores, "ramDmg", s["ramDamageScore"])
            updateMax(maxScores, "ramKill", s["ramKillBonus"])
        }

        // Step 3: Build score map
        val scoresByParticipantId = tickScores.associateBy {
            it.jsonObject["participantId"]?.jsonPrimitive?.int ?: -1
        }

        // Step 4: Remove panels for participants no longer in ranking
        val panelsToRemove = robotPanels.keys - newRankedIds.toSet()
        panelsToRemove.forEach { id ->
            val panel = robotPanels[id] ?: return@forEach
            // Animate out to the right before removal
            val current = panelCurrentPositions[id] ?: Point(0, 0)
            panelTargetPositions[id] = Point(backgroundPanel.width + SLIDE_IN_OFFSET, current.y)
        }

        // Step 5: Update or create panels for current ranked participants
        newRankedIds.forEachIndexed { index, participantId ->
            val scoreObj = scoresByParticipantId[participantId] ?: return@forEachIndexed
            val participant = participants[participantId]
            val name = participant?.name ?: "Bot $participantId"

            val panel = robotPanels.getOrPut(participantId) {
                val newPanel = RobotScorePanel(participantId)
                backgroundPanel.add(newPanel)
                // Start off-screen to the right for new entries
                val start = Point(backgroundPanel.width + SLIDE_IN_OFFSET, 0)
                panelCurrentPositions[participantId] = start
                newPanel.setBounds(start.x, start.y, 0, PANEL_HEIGHT) // width set later
                newPanel
            }

            panel.updateScores(scoreObj.jsonObject, name, maxScores)
        }

        rankedParticipantIds = newRankedIds
        recalculateTargetPositions()

        // Clean up panels that have fully animated out (after animation completes)
        // We'll do this in animatePanels after movement
    }

    private fun updateMax(map: MutableMap<String, Double>, key: String, jsonElement: kotlinx.serialization.json.JsonElement?) {
        val value = jsonElement?.jsonPrimitive?.doubleOrNull ?: 0.0
        map[key] = max(map[key] ?: 0.0, value)
    }

    private fun recalculateTargetPositions() {
        if (rankedParticipantIds.isEmpty()) {
            backgroundPanel.preferredSize = Dimension(MIN_PANEL_WIDTH, 0)
            backgroundPanel.revalidate()
            return
        }

        val containerWidth = backgroundPanel.width.coerceAtLeast(MIN_PANEL_WIDTH)
        val panelWidth = max(
            MIN_PANEL_WIDTH,
            min(containerWidth - 20, MAX_PANEL_WIDTH)
        )
        val startX = (containerWidth - panelWidth) / 2

        var currentY = INTERVAL
        val activeIds = mutableSetOf<Int>()

        rankedParticipantIds.forEach { id ->
            val targetY = currentY
            val targetPos = Point(startX, targetY)
            panelTargetPositions[id] = targetPos
            activeIds.add(id)

            // Ensure new panels have correct initial Y if just created
            if (!panelCurrentPositions.containsKey(id)) {
                panelCurrentPositions[id] = Point(containerWidth + SLIDE_IN_OFFSET, targetY)
            }

            currentY += PANEL_HEIGHT + INTERVAL
        }

        // Update background height
        val totalHeight = currentY
        backgroundPanel.preferredSize = Dimension(containerWidth, totalHeight)
        backgroundPanel.revalidate()

        if (!animationTimer.isRunning) {
            animationTimer.start()
        }
    }

    private fun animatePanels() {
        var allInPlace = true
        val containerWidth = backgroundPanel.width.coerceAtLeast(MIN_PANEL_WIDTH)
        val panelWidth = max(MIN_PANEL_WIDTH, min(containerWidth - 20, MAX_PANEL_WIDTH))

        val toRemove = mutableListOf<Int>()

        robotPanels.entries.forEach { (id, panel) ->
            val current = panelCurrentPositions[id] ?: return@forEach
            val target = panelTargetPositions[id]

            if (target == null) {
                // This panel is being removed — animate out to the right
                val newX = (current.x + (containerWidth + SLIDE_IN_OFFSET - current.x) * ANIMATION_SPEED).roundToInt()
                panel.setBounds(newX, current.y, panelWidth, PANEL_HEIGHT)
                panelCurrentPositions[id] = Point(newX, current.y)

                // If fully off-screen, mark for removal
                if (newX >= containerWidth + SLIDE_IN_OFFSET - 5) {
                    toRemove.add(id)
                }
                allInPlace = false
            } else {
                // Normal animation toward target
                val newX = (current.x + (target.x - current.x) * ANIMATION_SPEED).roundToInt()
                val newY = (current.y + (target.y - current.y) * ANIMATION_SPEED).roundToInt()

                panel.setBounds(newX, newY, panelWidth, PANEL_HEIGHT)
                panelCurrentPositions[id] = Point(newX, newY)

                if (newX != target.x || newY != target.y) {
                    allInPlace = false
                }
            }
        }

        // Remove panels that have slid out
        toRemove.forEach { id ->
            robotPanels.remove(id)?.let { panel ->
                backgroundPanel.remove(panel)
            }
            panelCurrentPositions.remove(id)
            panelTargetPositions.remove(id)
        }

        if (allInPlace && toRemove.isEmpty()) {
            animationTimer.stop()
        }

        backgroundPanel.repaint()
    }

    fun clearPanels() {
        backgroundPanel.removeAll()
        robotPanels.clear()
        panelCurrentPositions.clear()
        panelTargetPositions.clear()
        rankedParticipantIds = emptyList()
        backgroundPanel.preferredSize = Dimension(MIN_PANEL_WIDTH, 0)
        backgroundPanel.revalidate()
        backgroundPanel.repaint()
    }

    fun stop() {
        dataUpdateTimer.stop()
        animationTimer.stop()
    }
}