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

    var barColor: Color = Color(0x33, 0x99, 0xFF, 200) // A nice semi-transparent blue
    var textColor: Color = Color.WHITE
    var textAlignment: Int = CENTER

    companion object {
        const val LEFT = 0
        const val CENTER = 1
        const val RIGHT = 2
    }

    init {
        font = Font("Tahoma", Font.PLAIN, 12)
        isOpaque = true
        background = Color.GREEN
    }

    fun update(value: Double, maximumValue: Double, text: String) {
        this.value = value
        this.maximumValue = if (maximumValue > 0) maximumValue else 1.0 // Avoid division by zero
        this.text = text
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Calculate bar width
        val barWidth = if (maximumValue > 0) (width * (value / maximumValue)).toInt() else 0

        // Draw the filled portion of the bar
        g2d.color = barColor
        g2d.fillRect(0, 0, barWidth, height)

        // Draw the text on top of the bar
        g2d.color = textColor
        val fm = g2d.fontMetrics
        val textWidth = fm.stringWidth(text)

        val x = when (textAlignment) {
            LEFT -> 5 // 左对齐，5px 边距
            RIGHT -> width - textWidth - 5 // 右对齐，5px 边距
            else -> (width - textWidth) / 2 // 居中（默认）
        }
        val y = (height - fm.height) / 2 + fm.ascent
        g2d.drawString(text, x, y)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(95, 20) // Default size
    }
}