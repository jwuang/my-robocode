package dev.robocode.tankroyale.gui.ui.livescore

import dev.robocode.tankroyale.gui.ui.components.RcImages
import java.awt.AlphaComposite
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import javax.imageio.ImageIO
import javax.swing.JPanel

class BackgroundPanel : JPanel() {

    private var backgroundImage: Image? = null

    // 1. Add an alpha property (0.0f = transparent, 1.0f = opaque)
    var alpha: Float = 0.7f
        set(value) {
            field = value.coerceIn(0f, 1f) // Ensure value is between 0 and 1
            repaint() // Repaint when alpha changes
        }

    init {
        layout = null // Use null layout for absolute positioning
        try {
            backgroundImage = RcImages.scoreBackgroundImage
        } catch (e: Exception) {
            println("Could not load background image: ${e.message}")
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        backgroundImage?.let {
            val g2d = g as Graphics2D

            // 2. Set the composite with the desired alpha before drawing
            val originalComposite = g2d.composite
            g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)

            g2d.drawImage(it, 0, 0, width, height, this)

            // 3. Restore the original composite to not affect other components
            g2d.composite = originalComposite
        }
    }
}