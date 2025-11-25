package dev.robocode.tankroyale.gui.ui.tps

import dev.robocode.tankroyale.client.model.TpsChangedEvent
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.ui.components.RcSlider
import java.awt.Dimension
import java.util.*
import javax.swing.JLabel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

object TpsSlider : RcSlider() {

    init {
        minimum = 0
        maximum = 9

        paintLabels = true
        paintTicks = true
        majorTickSpacing = 1

        labelTable= Hashtable<Int, JLabel>().apply {
            this[0] = JLabel("0")
            this[1] = JLabel("5")
            this[2] = JLabel("10")
            this[3] = JLabel("15")
            this[4] = JLabel("20")
            this[5] = JLabel("25")
            this[6] = JLabel("30")
            this[7] = JLabel("40")
            this[8] = JLabel("60")
            this[9] = JLabel("max")
        }

        val size = preferredSize
        preferredSize = Dimension(size.width.coerceAtLeast(350), size.height) // width gets too small on Linux

        addChangeListener(TpsChangeListener())

        TpsEvents.onTpsChanged.subscribe(TpsSlider) {
            setTps(it.tps)
            ConfigSettings.tps = it.tps
        }

        setTps(ConfigSettings.tps)
    }

    private fun getTps(): Int {
        return when (value) {
            0 -> 0
            1 -> 5
            2 -> 10
            3 -> 15
            4 -> 20
            5 -> 25
            6 -> 30
            7 -> 40
            8 -> 60
            9 -> -1  // maximum
            else -> 20  // default fallback
        }
    }

    private fun setTps(tps: Int) {
        require(tps in -1..999) { "tps must be in the range -1..999" }
        value = when (tps) {
            -1 -> 9  // max
            0 -> 0
            in 1..4 -> 0  // round to 0
            in 5..7 -> 1  // round to 5
            in 8..12 -> 2  // round to 10
            in 13..17 -> 3  // round to 15
            in 18..22 -> 4  // round to 20
            in 23..27 -> 5  // round to 25
            in 28..34 -> 6  // round to 30
            in 35..49 -> 7  // round to 40
            in 50..999 -> 8  // round to 60
            else -> 4  // default to 20
        }
    }

    private class TpsChangeListener : ChangeListener {
        override fun stateChanged(e: ChangeEvent?) {
            if (!valueIsAdjusting) { // avoid events while dragging
                TpsEvents.onTpsChanged.fire(TpsChangedEvent(getTps()))
            }
        }
    }
}