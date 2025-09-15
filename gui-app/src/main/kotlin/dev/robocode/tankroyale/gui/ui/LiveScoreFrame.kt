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
import javax.swing.SwingUtilities

class LiveScoreFrame : RcFrame("Live Score Board", isTitlePropertyName = false) {

    private val tableModel: DefaultTableModel
    private val table: JTable
    private val participants = mutableMapOf<Int, Participant>()

    // 用于限制刷新频率
    private var lastUpdateTime: Long = 0
    private val minUpdateInterval: Long = 100 // 最小更新间隔为100毫秒

    // 缓存最新的tickScores数据
    private var pendingTickScores: JsonArray? = null
    private val updateTimer = javax.swing.Timer(100) { // 每100ms检查一次是否需要更新
        pendingTickScores?.let { scores ->
            updateTableWithScores(scores)
            pendingTickScores = null
        }
    }

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
        // 启用双缓冲减少界面闪烁
        scrollPane.putClientProperty("JComponent.sizeVariant", "large")
        contentPane.add(scrollPane)
        pack()
        setLocationRelativeTo(null)

        // 启动定时器
        updateTimer.start()

        // 添加实时排行榜窗口关闭监听器
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                updateTimer.stop()
                UIManager.hideLiveScoreFrame()
            }
        })
        subscribeToEvents()

        // 如果窗口在比赛已经开始后打开，则需要获取当前的参与者信息
        updateParticipantsFromCurrentGame()
    }

    private fun subscribeToEvents() {
        // Listen for participant information
        ClientEvents.onGameStarted.subscribe(this) { event ->
            updateParticipants(event.participants)
            // 新比赛开始时清空之前的数据
            clearScores()
            // 重置更新时间，确保新比赛开始时能立即显示数据
            lastUpdateTime = 0
            // 清除待处理的数据
            pendingTickScores = null
        }

        // 监听比赛结束事件，确保最终得分被立即显示
        ClientEvents.onGameEnded.subscribe(this) { event ->
            // 立即处理任何待处理的tickScores数据
            pendingTickScores?.let { scores ->
                updateTableWithScoresImmediately(scores)
                pendingTickScores = null
            }
        }
    }

    fun updateParticipants(participantsList: List<Participant>) {
        participants.clear()
        participantsList.forEach { participant ->
            participants[participant.id] = participant
        }
        // 更新参与者信息后，如果有待处理的分数数据，则立即更新表格
        pendingTickScores?.let { scores ->
            updateTableWithScoresImmediately(scores)
        }
    }

    // 当窗口在比赛已经开始后打开时，需要获取当前比赛的参与者信息
    private fun updateParticipantsFromCurrentGame() {
        val currentParticipants = UIManager.getCurrentParticipants()
        if (currentParticipants.isNotEmpty()) {
            updateParticipants(currentParticipants)
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
                // 只缓存最新的数据，实际更新由定时器处理
                pendingTickScores = tickScores
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateTableWithScores(tickScores: JsonArray) {
        // 检查是否需要更新（控制刷新频率）
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < minUpdateInterval) {
            return
        }
        lastUpdateTime = currentTime

        // 使用事务方式更新表格以减少闪烁
        tableModel.dataVector.removeAllElements()

        // Add new data
        for (element in tickScores) {
            val score = element.jsonObject
            val participantId = score["participantId"]?.jsonPrimitive?.int ?: 0
            val participant = participants[participantId]
            val name = participant?.name ?: "Bot $participantId"

            val row = arrayOf(
                score["rank"]?.jsonPrimitive?.content ?: "N/A",
                name,
                String.format("%.0f", score["totalScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["survivalScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["lastSurvivorBonus"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["bulletDamageScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["bulletKillBonus"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["ramDamageScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["ramKillBonus"]?.jsonPrimitive?.double ?: 0.0)
            )

            tableModel.addRow(row)
        }

        // 通知表格数据已更改
        tableModel.fireTableDataChanged()
    }

    private fun updateTableWithScoresImmediately(tickScores: JsonArray) {
        // 绕过频率限制，立即更新
        lastUpdateTime = 0 // 重置时间检查，确保更新会执行

        // 使用事务方式更新表格以减少闪烁
        tableModel.dataVector.removeAllElements()

        // Add new data
        for (element in tickScores) {
            val score = element.jsonObject
            val participantId = score["participantId"]?.jsonPrimitive?.int ?: 0
            val participant = participants[participantId]
            val name = participant?.name ?: "Bot $participantId"

            val row = arrayOf(
                score["rank"]?.jsonPrimitive?.content ?: "N/A",
                name,
                String.format("%.0f", score["totalScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["survivalScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["lastSurvivorBonus"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["bulletDamageScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["bulletKillBonus"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["ramDamageScore"]?.jsonPrimitive?.double ?: 0.0),
                String.format("%.0f", score["ramKillBonus"]?.jsonPrimitive?.double ?: 0.0)
            )

            tableModel.addRow(row)
        }

        // 通知表格数据已更改
        tableModel.fireTableDataChanged()
    }

    private fun updateTable() {
        tableModel.fireTableDataChanged()
    }

    fun clearScores() {
        tableModel.rowCount = 0
        tableModel.fireTableDataChanged()
    }

    // 添加dispose方法确保资源正确释放
    override fun dispose() {
        updateTimer.stop()
        super.dispose()
    }

}