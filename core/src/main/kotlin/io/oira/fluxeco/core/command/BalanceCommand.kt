package io.oira.fluxeco.core.command

import io.oira.fluxeco.FluxEco
import io.oira.fluxeco.core.manager.EconomyManager
import io.oira.fluxeco.core.manager.ConfigManager
import io.oira.fluxeco.core.manager.MessageManager
import io.oira.fluxeco.core.util.Placeholders
import io.oira.fluxeco.core.util.format
import io.oira.fluxeco.core.lamp.AsyncOfflinePlayer
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.annotation.CommandPermission
import io.oira.fluxeco.core.util.Threads

@Command("balance", "bal")
class BalanceCommand {

    private val plugin: FluxEco = FluxEco.instance
    private val messageManager: MessageManager = MessageManager.getInstance()
    private val configManager = ConfigManager(plugin, "messages.yml")
    private val foliaLib = FluxEco.instance.foliaLib

    @Description("Shows your current balance.")
    @CommandPermission("fluxeco.command.balance")
    fun balance(sender: Player, @Named("target") @Optional target: AsyncOfflinePlayer?) {
        val player = target ?: AsyncOfflinePlayer.from(sender)

        Threads.runAsync {
            val offlinePlayer = player.getOrFetch()
            val balance = EconomyManager.getBalance(offlinePlayer.uniqueId)
            foliaLib.scheduler.run {
                val placeholders = Placeholders()
                    .add("player", player.getName())
                    .add("balance", balance.format())

                if (target == null) {
                    messageManager.sendMessageFromConfig(sender, "balance.self", placeholders, config = configManager)
                } else {
                    messageManager.sendMessageFromConfig(sender, "balance.other", placeholders, config = configManager)
                }
            }
        }
    }
}
