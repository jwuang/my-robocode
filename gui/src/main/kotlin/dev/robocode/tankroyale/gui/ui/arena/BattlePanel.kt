package dev.robocode.tankroyale.gui.ui.arena

import java.awt.BorderLayout
import javax.swing.JPanel
import dev.robocode.tankroyale.gui.ui.livescore.LiveScorePanel

object BattlePanel : JPanel() {

    // Expose a single LiveScorePanel instance so other managers can access it
    val liveScorePanel: LiveScorePanel = LiveScorePanel()

    init {
        layout = BorderLayout()
        add(ArenaPanel, BorderLayout.CENTER)
        add(liveScorePanel, BorderLayout.EAST)

        liveScorePanel.isVisible = false
    }

    // Ensure singleton is preserved during Java serialization
    @Throws(java.io.ObjectStreamException::class)
    private fun readResolve(): Any = BattlePanel
}