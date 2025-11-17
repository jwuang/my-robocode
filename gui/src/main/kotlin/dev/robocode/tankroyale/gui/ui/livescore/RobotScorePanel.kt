package dev.robocode.tankroyale.gui.ui.livescore

import dev.robocode.tankroyale.gui.ui.Strings
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import java.awt.Component
import javax.swing.SwingConstants

class RobotScorePanel(val participantId: Int) : JPanel() {

    private val rankLabel = JLabel("No.1")
    private val nameLabel = JLabel("Bot Name")
    private val totalScoreBar = ScoreBar()
    private val survivalBar = ScoreBar()
    private val lastSurvivorBonusBar = ScoreBar()
    private val bulletDamageBar = ScoreBar()
    private val bulletKillBonusBar = ScoreBar()
    private val ramDamageBar = ScoreBar()
    private val ramKillBonusBar = ScoreBar()

    init {
        isOpaque = true
        layout = BorderLayout(8, 4)
        border = BorderFactory.createEmptyBorder(6, 8, 6, 8)
        background = Color(80, 80, 80) // Dark background

        // Left panel for rank and total score
        val leftPanel = JPanel()
        leftPanel.isOpaque = false
        leftPanel.layout = BoxLayout(leftPanel, BoxLayout.Y_AXIS)

        // Rank
        rankLabel.font = Font("Consolas", Font.BOLD, 20)
        rankLabel.foreground = Color(255, 210, 0) // Bright yellow
        rankLabel.horizontalAlignment = SwingConstants.CENTER
        rankLabel.alignmentX = Component.CENTER_ALIGNMENT
        leftPanel.add(rankLabel)

        // Total Score Bar below rank
        totalScoreBar.alignmentX = Component.CENTER_ALIGNMENT
        totalScoreBar.maximumSize = Dimension(120,24) // Limit width to make it compact
        totalScoreBar.barColor = Color(51, 153, 255) // Light blue
        leftPanel.add(totalScoreBar)

        add(leftPanel, BorderLayout.WEST)

        // Center panel for name and bars
        val centerPanel = JPanel()
        centerPanel.isOpaque = false
        centerPanel.layout = BoxLayout(centerPanel, BoxLayout.Y_AXIS)
        add(centerPanel, BorderLayout.CENTER)

        // Name
        nameLabel.font = Font(Font.DIALOG, Font.BOLD, 12)
        nameLabel.foreground = Color(200, 200, 200) // Light gray
        nameLabel.alignmentX = Component.CENTER_ALIGNMENT
        centerPanel.add(nameLabel)

        // Bars panel
        val barsPanel = JPanel(FlowLayout(FlowLayout.CENTER, 6, 3))
        barsPanel.isOpaque = false
        centerPanel.add(barsPanel)

        // Configure and add bars
        survivalBar.barColor = Color(50, 200, 100)     // Green
        lastSurvivorBonusBar.barColor = Color(241, 196, 15) // Yellow
        bulletDamageBar.barColor = Color(255, 140, 0)  // Orange
        bulletKillBonusBar.barColor = Color(243, 156, 18) // Darker orange
        ramDamageBar.barColor = Color(220, 20, 60)     // Red
        ramKillBonusBar.barColor = Color(192, 57, 43)   // Dark red

        barsPanel.add(createLabeledBar(Strings.get("results.survival_score"), survivalBar))
        barsPanel.add(createLabeledBar(Strings.get("results.last_survivor_bonus"), lastSurvivorBonusBar))
        barsPanel.add(createLabeledBar(Strings.get("results.bullet_damage_score"), bulletDamageBar))
        barsPanel.add(createLabeledBar(Strings.get("results.bullet_kill_bonus"), bulletKillBonusBar))
        barsPanel.add(createLabeledBar(Strings.get("results.ram_damage"), ramDamageBar))
        barsPanel.add(createLabeledBar(Strings.get("results.ram_kill_bonus"), ramKillBonusBar))
    }

    private fun createLabeledBar(labelText: String, bar: ScoreBar): JPanel {
        val panel = JPanel(BorderLayout(4, 2))
        panel.isOpaque = false

        val label = JLabel(labelText)
        label.font = Font(Font.DIALOG, Font.PLAIN, 10)
        label.foreground = Color(140, 140, 140) // Soft gray
        label.horizontalAlignment = SwingConstants.CENTER
        panel.add(label, BorderLayout.NORTH)
        panel.add(bar, BorderLayout.CENTER)

        return panel
    }

    override fun getPreferredSize(): Dimension {
        val rankFm = getFontMetrics(rankLabel.font)
        val nameFm = getFontMetrics(nameLabel.font)

        // Estimate left panel width (rank + score bar)
        val rankTextWidth = rankFm.stringWidth("#999") + 12
        val totalScoreBarWidth = totalScoreBar.preferredSize.width
        val leftWidth = maxOf(rankTextWidth, totalScoreBarWidth) + 4

        // Center: name + bars
        val nameHeight = nameFm.height + 8
        val barHeights = listOf(survivalBar, lastSurvivorBonusBar, bulletDamageBar, bulletKillBonusBar, ramDamageBar, ramKillBonusBar)
            .map { it.preferredSize.height }
        val maxBarHeight = barHeights.maxOrNull() ?: 18

        val barRowHeight = maxBarHeight + 6 // bar + label
        val centerHeight = nameHeight + barRowHeight + 12

        // Total width estimate
        val barsPanelWidth = listOf(survivalBar, lastSurvivorBonusBar, bulletDamageBar, bulletKillBonusBar, ramDamageBar, ramKillBonusBar)
            .map { it.preferredSize.width }
            .sum() + 6 * 5 + 16 // gaps and margins

        val totalWidth = (leftWidth + barsPanelWidth).coerceIn(200, 320) // Narrower range

        return Dimension(totalWidth, centerHeight.coerceAtLeast(80))
    }

    fun updateScores(scoreData: JsonObject, name: String, maxScores: Map<String, Double>) {
        val total = scoreData["totalScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val survival = scoreData["survivalScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val lastSurvivorBonus = scoreData["lastSurvivorBonus"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val bulletDamage = scoreData["bulletDamageScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val bulletKillBonus = scoreData["bulletKillBonus"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val ramDamage = scoreData["ramDamageScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val ramKillBonus = scoreData["ramKillBonus"]?.jsonPrimitive?.doubleOrNull ?: 0.0

        val maxTotal = maxScores["total"] ?: 1.0
        val maxSurvival = maxScores["survival"] ?: 1.0
        val maxLastSurvivorBonus = maxScores["lastSurvivor"] ?: 1.0
        val maxBulletDamage = maxScores["bulletDmg"] ?: 1.0
        val maxBulletKillBonus = maxScores["bulletKill"] ?: 1.0
        val maxRamDamage = maxScores["ramDmg"] ?: 1.0
        val maxRamKillBonus = maxScores["ramKill"] ?: 1.0

        val rank = scoreData["rank"]?.jsonPrimitive?.content ?: "N/A"
        rankLabel.text = "Rank ${rank}"
        nameLabel.text = name

        totalScoreBar.update(total, maxTotal, String.format("%.0f", total))
        survivalBar.update(survival, maxSurvival, String.format("%.0f", survival))
        lastSurvivorBonusBar.update(lastSurvivorBonus, maxLastSurvivorBonus, String.format("%.0f", lastSurvivorBonus))
        bulletDamageBar.update(bulletDamage, maxBulletDamage, String.format("%.0f", bulletDamage))
        bulletKillBonusBar.update(bulletKillBonus, maxBulletKillBonus, String.format("%.0f", bulletKillBonus))
        ramDamageBar.update(ramDamage, maxRamDamage, String.format("%.0f", ramDamage))
        ramKillBonusBar.update(ramKillBonus, maxRamKillBonus, String.format("%.0f", ramKillBonus))

        revalidate()
        repaint()
    }
}