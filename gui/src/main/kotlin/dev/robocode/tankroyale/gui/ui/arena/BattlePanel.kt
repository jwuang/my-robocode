package dev.robocode.tankroyale.gui.ui.arena

import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JPanel
import dev.robocode.tankroyale.gui.ui.livescore.LiveScorePanel

object BattlePanel : JPanel() {

    // Expose a single LiveScorePanel instance so other managers can access it
    val liveScorePanel: LiveScorePanel = LiveScorePanel()

    init {
        layout = BorderLayout()
        add(ArenaPanel, BorderLayout.CENTER)
        add(liveScorePanel, BorderLayout.EAST)

        liveScorePanel.isVisible = true
    }

    // 确保单例在 Java 序列化时得到保持
    @Throws(java.io.ObjectStreamException::class)
    private fun readResolve(): Any = BattlePanel
}