package dev.robocode.tankroyale.gui.ui.livescore

import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent

class ScoreBar : JComponent() {

    var value: Double = 0.0
    var maximumValue: Double = 100.0
    var text: String = ""

    var barColor: Color = Color(0x33, 0x99, 0xFF) // A nice semi-transparent blue
    var trackColor: Color = Color(40, 40, 45) // Background of the bar
    var textColor: Color = Color(255, 200, 0)


    init {
        font = Font("Tahoma", Font.PLAIN, 11) // 稍微放大一点，便于缩放时阅读
        isOpaque = true
    }

    fun update(value: Double, maximumValue: Double, text: String) {
        this.value = value
        this.maximumValue = if (maximumValue > 0) maximumValue else 1.0 // Avoid division by zero
        this.text = text
        revalidate()
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw the track (background) of the bar
        g2d.color = trackColor
        g2d.fillRect(0, 0, width, height)

        // Calculate bar width
        val barWidth = if (maximumValue > 0) (width * (value / maximumValue)).coerceIn(0.0, width.toDouble()) else 0

        // Draw the filled portion of the bar
        g2d.color = barColor
        g2d.fillRect(0, 0, barWidth.toInt(), height)

        // Draw the text on top of the bar
        g2d.color = textColor
        val fm = g2d.fontMetrics
        val textWidth = fm.stringWidth(text)

        val x = ((width - textWidth) / 2).coerceAtLeast(6)
        val y = (height - fm.height) / 2 + fm.ascent
        g2d.drawString(text, x, y)
    }

    override fun getPreferredSize(): Dimension {
        // Preferred width: at least room for the text plus some breathing room.
        // Preferred height: based on font size for good scaling.
        val fm = getFontMetrics(font)
        val textWidth = fm.stringWidth(text.ifEmpty { "100" })
        val prefW = (textWidth + 16).coerceAtLeast(48) // 保证最小宽度，不会被挤成超窄
        val prefH = fm.height + 12
        return Dimension(prefW, prefH)
    }
}