package dev.robocode.tankroyale.gui.ui.extensions

import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.ui.MenuTitles
import javax.swing.JMenu
import javax.swing.JMenuItem

object JMenuExt {

    fun JMenu.addNewMenuItem(menuResourceName: String, event: Event<JMenuItem>): JMenuItem {
        val menuItem = JMenuItem(MenuTitles.get(menuResourceName))
        add(menuItem)
        menuItem.addActionListener { event.fire(menuItem) }
        return menuItem
    }
}
