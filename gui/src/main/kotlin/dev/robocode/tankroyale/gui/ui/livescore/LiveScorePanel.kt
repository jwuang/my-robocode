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
        layout = null // absolute positioning used for animation
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
        const val MIN_PANEL_WIDTH = 450
        const val MAX_PANEL_WIDTH = 500
        // PANEL_HEIGHT removed as fixed; use each panel's preferredSize.height
        const val INTERVAL = 8
        const val ANIMATION_SPEED = 0.22f
        const val SLIDE_IN_OFFSET = 60 // pixels off-screen for entrance
    }

    init {
        layout = BorderLayout()
        minimumSize = Dimension(MIN_PANEL_WIDTH, 0)
        maximumSize = Dimension(MAX_PANEL_WIDTH, 1080)

        val scrollPane = JScrollPane(backgroundPanel).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            border = null
        }
        add(scrollPane, BorderLayout.CENTER)


        subscribeToEvents()
        updateParticipantsFromCurrentGame()

        dataUpdateTimer.start()
    }

    // Dynamic preferred width based on parent
    override fun getPreferredSize(): Dimension {
        return Dimension(582, 700)
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
        // Extract ranked IDs
        val newRankedIds = tickScores.mapNotNull {
            it.jsonObject["participantId"]?.jsonPrimitive?.int
        }

        // Compute max scores for normalization
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

        // Build score map
        val scoresByParticipantId = tickScores.associateBy {
            it.jsonObject["participantId"]?.jsonPrimitive?.int ?: -1
        }

        // Remove panels for participants no longer in ranking (animate out)
        val panelsToRemove = robotPanels.keys - newRankedIds.toSet()
        panelsToRemove.forEach { id ->
            val panel = robotPanels[id] ?: return@forEach
            val current = panelCurrentPositions[id] ?: Point(backgroundPanel.width + SLIDE_IN_OFFSET, 0)
            // Set target x to off-screen right
            panelTargetPositions[id] = Point(backgroundPanel.width + SLIDE_IN_OFFSET, current.y)
        }

        // Update or create panels for current ranked participants
        newRankedIds.forEachIndexed { index, participantId ->
            val scoreObj = scoresByParticipantId[participantId] ?: return@forEachIndexed
            val participant = participants[participantId]
            val name = participant?.name ?: "Bot $participantId"

            val panel = robotPanels.getOrPut(participantId) {
                val newPanel = RobotScorePanel(participantId)
                // Add before measuring preferred size
                backgroundPanel.add(newPanel)
                newPanel.revalidate()
                newPanel
            }

            panel.updateScores(scoreObj.jsonObject, name, maxScores)
        }

        rankedParticipantIds = newRankedIds
        recalculateTargetPositions()
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

        val containerWidth = (backgroundPanel.width.takeIf { it > 0 } ?: (parent?.width ?: MIN_PANEL_WIDTH)).coerceAtLeast(MIN_PANEL_WIDTH)
        val panelWidth = max(
            MIN_PANEL_WIDTH,
            min(containerWidth - 40, MAX_PANEL_WIDTH)
        )
        val startX = (containerWidth - panelWidth) / 2

        // Compute per-panel heights using preferredSize
        val panelHeights = rankedParticipantIds.map { id ->
            val p = robotPanels[id]
            val h = p?.preferredSize?.height ?: (getDefaultPanelHeight())
            h
        }

        val totalPanelHeight = panelHeights.sum() + INTERVAL * (panelHeights.size - 1)
        val availableHeight = backgroundPanel.parent?.height ?: totalPanelHeight
        val startY = max(INTERVAL, (availableHeight - totalPanelHeight) / 2)

        var currentY = startY

        val activeIds = mutableSetOf<Int>()

        rankedParticipantIds.forEachIndexed { idx, id ->
            val h = panelHeights.getOrNull(idx) ?: getDefaultPanelHeight()
            val targetPos = Point(startX, currentY)
            panelTargetPositions[id] = targetPos
            activeIds.add(id)

            // If no current pos, set it off-screen right (for slide-in)
            if (!panelCurrentPositions.containsKey(id)) {
                panelCurrentPositions[id] = Point(containerWidth + SLIDE_IN_OFFSET, currentY)
            }

            // Ensure panel bounds reflect computed width/height immediately so scrollbars and painting are correct
            robotPanels[id]?.let { panel ->
                panel.setBounds(panelCurrentPositions[id]!!.x, panelCurrentPositions[id]!!.y, panelWidth, h)
            }

            currentY += h + INTERVAL
        }

        // Remove any leftover target positions for panels not active
        val removed = panelTargetPositions.keys - activeIds
        removed.forEach { k -> panelTargetPositions.remove(k) }

        // Set background content size
        val contentHeight = max(totalPanelHeight, availableHeight)
        backgroundPanel.preferredSize = Dimension(containerWidth, contentHeight)
        backgroundPanel.revalidate()

        if (!animationTimer.isRunning) {
            animationTimer.start()
        }
    }

    private fun getDefaultPanelHeight(): Int {
        // Fallback height when preferred is unavailable
        return 52
    }

    private fun animatePanels() {
        var allInPlace = true
        val containerWidth = backgroundPanel.width.coerceAtLeast(MIN_PANEL_WIDTH)
        val panelWidth = max(MIN_PANEL_WIDTH, min(containerWidth - 40, MAX_PANEL_WIDTH))

        val toRemove = mutableListOf<Int>()

        robotPanels.entries.forEach { (id, panel) ->
            val current = panelCurrentPositions[id] ?: return@forEach
            val target = panelTargetPositions[id]

            val panelHeight = panel.preferredSize.height.takeIf { it > 0 } ?: getDefaultPanelHeight()

            if (target == null) {
                // Animate out to the right
                val newX = (current.x + (containerWidth + SLIDE_IN_OFFSET - current.x) * ANIMATION_SPEED).roundToInt()
                panel.setBounds(newX, current.y, panelWidth, panelHeight)
                panelCurrentPositions[id] = Point(newX, current.y)

                if (newX >= containerWidth + SLIDE_IN_OFFSET - 5) {
                    toRemove.add(id)
                }
                allInPlace = false
            } else {
                // Move toward target
                val newX = (current.x + (target.x - current.x) * ANIMATION_SPEED).roundToInt()
                val newY = (current.y + (target.y - current.y) * ANIMATION_SPEED).roundToInt()

                panel.setBounds(newX, newY, panelWidth, panelHeight)
                panelCurrentPositions[id] = Point(newX, newY)

                if (newX != target.x || newY != target.y) {
                    allInPlace = false
                }
            }
        }

        // Remove fully slid out panels
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

        backgroundPanel.revalidate()
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