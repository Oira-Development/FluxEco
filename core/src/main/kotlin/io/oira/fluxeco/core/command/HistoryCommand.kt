package io.oira.fluxeco.core.command

import io.oira.fluxeco.FluxEco
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Named
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("history")
class HistoryCommand {

    @Description("Opens the transaction history GUI.")
    @CommandPermission("fluxeco.command.history")
    fun history(sender: Player) {
        FluxEco.instance.historyGui.open(sender)
    }

    @Description("Opens the transaction history GUI for a specific player.")
    @CommandPermission("fluxeco.command.history.others")
    fun historyOther(sender: Player, @Named("target") target: Player) {
        FluxEco.instance.historyGui.openForPlayer(sender, target.uniqueId)
    }
}
