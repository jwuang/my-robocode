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
import java.awt.Component
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull

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

    // Store the max scores observed to keep bar scaling consistent
    private var maxTotalScore = 1.0
    private var maxSurvivalScore = 1.0
    private var maxLastSurvivorBonus = 1.0
    private var maxBulletScore = 1.0
    private var maxBulletKillBonus = 1.0
    private var maxRamScore = 1.0
    private var maxRamKillBonus = 1.0

    init {
        isOpaque = true // Crucial for background image
        layout = BorderLayout(10, 0)
        border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
        background = Color(20, 20, 20, 100)
        // Rank
        rankLabel.font = Font("Consolas", Font.ITALIC, 24)
        rankLabel.foreground = Color(255, 200, 0)
        rankLabel.preferredSize = Dimension(60, 0)
        add(rankLabel, BorderLayout.WEST)

        // Center panel for name and bars
        val centerPanel = JPanel()
        centerPanel.isOpaque = true
        centerPanel.background = Color.PINK
        centerPanel.layout = BoxLayout(centerPanel, BoxLayout.Y_AXIS)
        add(centerPanel, BorderLayout.CENTER)

        // Name
        nameLabel.font = Font(Font.DIALOG, Font.PLAIN, 16)
        nameLabel.foreground = Color.WHITE
        nameLabel.alignmentX = Component.CENTER_ALIGNMENT
        centerPanel.add(nameLabel)

        // Bars panel
        val barsPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        barsPanel.isOpaque = true
        barsPanel.background = Color.YELLOW
        barsPanel.alignmentX = Component.CENTER_ALIGNMENT
        centerPanel.add(barsPanel)

        // Configure and add bars 兰亭黑/Yu gothic
        totalScoreBar.barColor = Color(51, 153, 255, 200) // Light blue for total
        survivalBar.barColor = Color(40, 180, 99, 200)      // Green for survival
        lastSurvivorBonusBar.barColor = Color(241, 196, 15, 200) // Yellow for bonus
        bulletDamageBar.barColor = Color(255, 165, 0, 200)  // Orange for bullet damage
        bulletKillBonusBar.barColor = Color(243, 156, 18, 200)  // Darker orange for bonus
        ramDamageBar.barColor = Color(220, 20, 60, 200)      // Red for ram damage
        ramKillBonusBar.barColor = Color(192, 57, 43, 200)   // Darker red for bonus

        barsPanel.add(createLabeledBar(Strings.get("results.total_score"), totalScoreBar))
        barsPanel.add(createLabeledBar(Strings.get("results.survival_score"), survivalBar))
        barsPanel.add(createLabeledBar(Strings.get("results.last_survivor_bonus"), lastSurvivorBonusBar))
        barsPanel.add(createLabeledBar(Strings.get("results.bullet_damage_score"), bulletDamageBar))
        barsPanel.add(createLabeledBar(Strings.get("results.bullet_kill_bonus"), bulletKillBonusBar))
        barsPanel.add(createLabeledBar(Strings.get("results.ram_damage"), ramDamageBar))
        barsPanel.add(createLabeledBar(Strings.get("results.ram_kill_bonus"), ramKillBonusBar))

        // totalScoreBar.textAlignment = ScoreBar.LEFT
    }

    private fun createLabeledBar(labelText: String, bar: ScoreBar): JPanel {
        val panel = JPanel(BorderLayout())
        panel.isOpaque = true
        panel.background = Color.orange

        val label = JLabel(labelText)
        label.foreground = Color.WHITE
        // 设置标签文本水平居中对齐
        label.horizontalAlignment = 0
        label.verticalAlignment = 0
        panel.add(label, BorderLayout.NORTH)
        panel.add(bar, BorderLayout.CENTER)
        return panel
    }

    fun updateScores(scoreData: JsonObject, name: String, maxScores: Map<String, Double>) {
        // Update max scores for consistent scaling across all panels
        maxTotalScore = maxScores["total"] ?: 1.0
        maxSurvivalScore = maxScores["survival"] ?: 1.0
        maxLastSurvivorBonus = maxScores["lastSurvivor"] ?: 1.0
        maxBulletScore = maxScores["bulletDmg"] ?: 1.0
        maxBulletKillBonus = maxScores["bulletKill"] ?: 1.0
        maxRamScore = maxScores["ramDmg"] ?: 1.0
        maxRamKillBonus = maxScores["ramKill"] ?: 1.0

        // Extract values using .doubleOrNull for safety
        val rank = scoreData["rank"]?.jsonPrimitive?.content ?: "N/A"
        val totalScore = scoreData["totalScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val survival = scoreData["survivalScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val lastSurvivorBonus = scoreData["lastSurvivorBonus"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val bulletDamage = scoreData["bulletDamageScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val bulletKillBonus = scoreData["bulletKillBonus"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val ramDamage = scoreData["ramDamageScore"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val ramKillBonus = scoreData["ramKillBonus"]?.jsonPrimitive?.doubleOrNull ?: 0.0

        // Update UI components
        rankLabel.text = "#$rank"
        nameLabel.text = name

        totalScoreBar.update(totalScore, maxTotalScore, String.format("%.0f", totalScore))
        survivalBar.update(survival, maxSurvivalScore, String.format("%.0f", survival))
        lastSurvivorBonusBar.update(lastSurvivorBonus, maxLastSurvivorBonus, String.format("%.0f", lastSurvivorBonus))
        bulletDamageBar.update(bulletDamage, maxBulletScore, String.format("%.0f", bulletDamage))
        bulletKillBonusBar.update(bulletKillBonus, maxBulletKillBonus, String.format("%.0f", bulletKillBonus))
        ramDamageBar.update(ramDamage, maxRamScore, String.format("%.0f", ramDamage))
        ramKillBonusBar.update(ramKillBonus, maxRamKillBonus, String.format("%.0f", ramKillBonus))
    }
}