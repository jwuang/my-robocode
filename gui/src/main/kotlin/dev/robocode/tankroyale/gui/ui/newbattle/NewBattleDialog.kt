package dev.robocode.tankroyale.gui.ui.newbattle

import dev.robocode.tankroyale.client.model.BotInfo
import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.client.Client
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.GamesSettings
import dev.robocode.tankroyale.gui.ui.Hints
import dev.robocode.tankroyale.gui.ui.MainFrame
import dev.robocode.tankroyale.gui.ui.Messages
import dev.robocode.tankroyale.gui.ui.Strings
import dev.robocode.tankroyale.gui.ui.components.RcDialog
import dev.robocode.tankroyale.gui.ui.config.SetupRulesDialog
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addCancelButton
import dev.robocode.tankroyale.gui.ui.extensions.JComponentExt.addLabel
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.util.MessageDialog
import dev.robocode.tankroyale.gui.ui.components.ToggleSwitch
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import java.awt.event.ItemEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel

object NewBattleDialog : RcDialog(MainFrame, "new_battle_dialog") {

    private val selectBotsAndStartPanel = NewBattlePanel()

    init {
        contentPane.add(selectBotsAndStartPanel)
        size = Dimension(950, 750)
        setLocationRelativeTo(owner) // center on owner window

        ServerEvents.onStopped.subscribe(this) {
            MessageDialog.showError(Messages.get("battle_lost_server_connection"))
            dispose()
        }
    }
}

class NewBattlePanel : JPanel(MigLayout("fill", "[]", "[][grow][][]")) {

    private val onStartBattle = Event<JButton>()
    private val onCancel = Event<JButton>()
    private val onSetupRules = Event<JButton>()

    private val startBattleButton: JButton

    private var selectedBots = emptyList<BotInfo>()
    private var gameTypeDropdown = GameTypeDropdown()

    init {
        // Left: Select game type group
        val topLeftPanel = JPanel(MigLayout("left, insets 5")).apply {
            border = BorderFactory.createTitledBorder(Strings.get("select_game_type"))

            val hint = Hints.get("new_battle.game_type")
            addLabel("game_type").apply {
                toolTipText = hint
            }
            gameTypeDropdown.toolTipText = hint
            add(gameTypeDropdown)

            addButton("setup_rules", onSetupRules).apply {
                toolTipText = Hints.get("new_battle.setup_rules")
            }
        }

        // Right: Recording group
        val topRightPanel = JPanel(MigLayout("left, insets 5")).apply {
            border = BorderFactory.createTitledBorder(Strings.get("recording"))

            val autoRecordHint = Hints.get("new_battle.auto_record")
            addLabel("auto_record").apply {
                toolTipText = autoRecordHint
            }
            val autoRecordSwitch = ToggleSwitch(ConfigSettings.enableAutoRecording).apply {
                toolTipText = autoRecordHint
                addSwitchHandler { isSelected ->
                    ConfigSettings.enableAutoRecording = isSelected
                }
            }
            add(autoRecordSwitch)
        }

        // Row container for placing both groups side-by-side with minimal widths and equal heights
        val topRow = JPanel(MigLayout("insets 0", "[pref!][pref!]", "[]")).apply {
            add(topLeftPanel, "growy")
            add(topRightPanel, "growy, wrap")
        }

        val buttonPanel = JPanel(MigLayout("center, insets 0"))

        add(topRow, "growx, wrap")
        add(BotSelectionPanel, "grow, wrap")
        add(BotInfoPanel, "grow, wrap")
        add(buttonPanel, "center")

        buttonPanel.apply {
            startBattleButton = addButton("start_battle", onStartBattle)
            addCancelButton(onCancel)
        }
        startBattleButton.isEnabled = false
        updateStartButtonHint()

        BotSelectionEvents.onSelectedBotListUpdated.subscribe(this) {
            selectedBots = it

            val selectedCount = calcNumberOfParticipants(it)
            val maxParticipants = maxNumberOfParticipants()

            startBattleButton.isEnabled = selectedCount >= minNumberOfParticipants() &&
                    (maxParticipants == null || selectedCount <= maxParticipants)
        }

        onStartBattle.subscribe(this) { startGame() }
        onCancel.subscribe(this) { NewBattleDialog.dispose() }
        onSetupRules.subscribe(this) { SetupRulesDialog.isVisible = true }

        gameTypeDropdown.apply {
            addActionListener {
                ConfigSettings.apply {
                    gameType = gameTypeDropdown.getSelectedGameType()
                    save()
                }
                updateStartButtonHint()
            }

            addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    BotSelectionPanel.update()
                }
            }
        }

        BotSelectionPanel.update()
    }

    // Calculate the number of participants (team of bots) and individual bots
    private fun calcNumberOfParticipants(bots: Collection<BotInfo>): Int {

        data class Participant(val name: String, val version: String, val id: Int? = null)

        val participants = HashSet<Participant>()
        var fakeId = 1

        bots.forEach {
            participants.add(
                if (it.teamId != null) {
                    Participant(it.teamName!!, it.teamVersion!!, it.teamId)
                } else {
                    Participant(it.name, it.version, fakeId++)
                }
            )
        }
        return participants.size
    }

    private fun minNumberOfParticipants(): Int =
        GamesSettings.games[ConfigSettings.gameType.displayName]?.minNumberOfParticipants ?: 2

    private fun maxNumberOfParticipants(): Int? =
        GamesSettings.games[ConfigSettings.gameType.displayName]?.maxNumberOfParticipants

    private fun updateStartButtonHint() {
        startBattleButton.toolTipText = Hints.get("new_battle.start_button")
            .format(minNumberOfParticipants(), maxNumberOfParticipants()?.toString() ?: Strings.get("unlimited"))
    }

    private fun startGame() {
        isVisible = true

        val botAddresses = selectedBots.map { it.botAddress }
        Client.startGame(botAddresses.toSet())

        NewBattleDialog.dispose()
    }
}