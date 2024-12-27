package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.balancetop

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission

object BalancetopCommand : InternalCmd {
    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("balancetop")
            .withAliases("baltop")
            .withPermission(PolyPermission.COMMAND_BALANCETOP.toString())
            .withOptionalArguments(
                IntegerArgument("page")
            )
            .executes(CommandExecutor { _, _ ->
                throw CommandAPI.failWithString("Not yet implemented!")
            })
    }
}