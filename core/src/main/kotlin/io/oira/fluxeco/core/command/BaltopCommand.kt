package io.oira.fluxeco.core.command

import io.oira.fluxeco.FluxEco
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.bukkit.annotation.CommandPermission

class BaltopCommand {

    @Command("baltop")
    @Description("Opens the balance leaderboard GUI.")
    @CommandPermission("fluxeco.command.baltop")
    fun baltop(sender: Player) {
        FluxEco.instance.baltopGui.open(sender)
    }
}
