package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.subroutine

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm

object SupportZipSubcommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("support-zip")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_SUBROUTINE.toString())
            .executes(CommandExecutor { sender, _ ->
                plugin.translations.commandGenericNotYetImplemented.sendTo(sender)
                throw plugin.translations.commandApiFailure()
            })
    }

}