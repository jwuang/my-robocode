package dev.robocode.tankroyale.gui.ui.livescore

import dev.robocode.tankroyale.gui.ui.components.RcImages
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.MediaTracker
import java.awt.RenderingHints
import javax.swing.JPanel

class BackgroundPanel : JPanel() {

    private var backgroundImage: Image? = null
    private var imgWidth = -1
    private var imgHeight = -1

    init {
        isOpaque = true
        layout = null

        try {
            val img = RcImages.scoreBackgroundImage// Wait for image to load (simple approach)
            val tracker = MediaTracker(this)
            tracker.addImage(img, 0)
            tracker.waitForID(0, 5000) // timeout 5s

            if (tracker.statusID(0, true) == MediaTracker.COMPLETE) {
                backgroundImage = img
                imgWidth = img.getWidth(this)
                imgHeight = img.getHeight(this)
            } else {
                System.err.println("Background image failed to load in time.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

        backgroundImage?.let { img ->
            val imgW = img.getWidth(this)
            val imgH = img.getHeight(this)
            if (imgW <= 0 || imgH <= 0 || width <= 0 || height <= 0) return

            // 计算缩放比例：确保图像覆盖整个面板
            val scaleX = width.toDouble() / imgW
            val scaleY = height.toDouble() / imgH
            val scale = maxOf(scaleX, scaleY) // 取较大者，确保覆盖

            // 计算绘制尺寸
            val drawW = (imgW * scale).toInt()
            val drawH = (imgH * scale).toInt()

            // 居中裁剪：计算偏移，使图像中心对齐面板中心
            val drawX = (width - drawW) / 2
            val drawY = (height - drawH) / 2

            g2d.drawImage(img, drawX, drawY, drawW, drawH, this)
        }
    }
}