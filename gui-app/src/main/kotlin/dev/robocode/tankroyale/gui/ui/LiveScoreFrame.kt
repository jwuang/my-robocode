package dev.robocode.tankroyale.gui.ui

import dev.robocode.tankroyale.gui.client.ClientEvents
import dev.robocode.tankroyale.gui.ui.components.RcFrame
import dev.robocode.tankroyale.gui.ui.components.RcToolTip
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.Component
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import dev.robocode.tankroyale.client.model.TickEvent
import dev.robocode.tankroyale.client.model.Participant
import kotlinx.serialization.json.*

class LiveScoreFrame : RcFrame("Live Score Board", isTitlePropertyName = false) {

    private val tableModel: DefaultTableModel
    private val table: JTable
    private val participants = mutableMapOf<Int, Participant>()

    init {
        // Initialize columns
        val columns = arrayOf(
            Strings.get("results.rank"),
            Strings.get("results.name"),
            Strings.get("results.total_score"),
            Strings.get("results.survival_score"),
            Strings.get("results.last_survivor_bonus"),
            Strings.get("results.bullet_damage_score"),
            Strings.get("results.bullet_kill_bonus"),
            Strings.get("results.ram_damage"),
            Strings.get("results.ram_kill_bonus")
        )

        tableModel = object : DefaultTableModel(columns, 0) {
            override fun isCellEditable(row: Int, column: Int) = false
        }

        table = object : JTable(tableModel) {
            override fun createToolTip() = RcToolTip()
        }

        table.apply {
            preferredScrollableViewportSize = Dimension(800, 400)
            setDefaultRenderer(Object::class.java, CellRendererWithToolTip)
        }

        val scrollPane = JScrollPane(table)
        contentPane.add(scrollPane)
        pack()
        setLocationRelativeTo(null)
        // 添加实时排行榜窗口关闭监听器
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                UIManager.hideLiveScoreFrame()
            }
        })
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        // Listen for participant information
        ClientEvents.onGameStarted.subscribe(this) { event ->
            participants.clear()
            event.participants.forEach { participant ->
                participants[participant.id] = participant
            }
            // 新比赛开始时清空之前的数据
            clearScores()
        }

    }

    private fun updateLiveScores(event: TickEvent) {
        // This method is not used because we handle tick scores directly in Client.onMessage
        // See Client.kt for the implementation that detects TickEvent messages with tickScores

    }

    fun updateWithTickScores(tickScoresJson: String) {
        try {
            val jsonObject = Json.parseToJsonElement(tickScoresJson).jsonObject
            if (jsonObject.containsKey("tickScores")) {
                val tickScores = jsonObject["tickScores"]!!.jsonArray
                updateTableWithScores(tickScores)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateTableWithScores(tickScores: JsonArray) {
        // Clear existing data
        tableModel.rowCount = 0

        // Add new data
        for (element in tickScores) {
            val score = element.jsonObject
            val participantId = score["participantId"]?.jsonPrimitive?.int ?: 0
            val participant = participants[participantId]
            val name = participant?.name ?: "Bot $participantId"

            val row = arrayOf(
                score["rank"]?.jsonPrimitive?.content ?: "N/A",
                name,
                String.format("%.2f", score["totalScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.2f", score["survivalScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.2f", score["lastSurvivorBonus"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.2f", score["bulletDamageScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.2f", score["bulletKillBonus"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.2f", score["ramDamageScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.2f", score["ramKillBonus"]?.jsonPrimitive?.double ?: 0.0)
            )

            tableModel.addRow(row)
        }

        tableModel.fireTableDataChanged()
    }

    private fun updateTable() {
        tableModel.fireTableDataChanged()
    }

    fun clearScores() {
        tableModel.rowCount = 0
        tableModel.fireTableDataChanged()
    }
}