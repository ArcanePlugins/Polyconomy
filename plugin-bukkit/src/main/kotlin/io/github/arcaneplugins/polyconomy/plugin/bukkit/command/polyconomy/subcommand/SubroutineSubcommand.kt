package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

import dev.jorel.commandapi.CommandAPICommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission

object SubroutineSubcommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("subroutine")
            .withPermission(PolyPermission.COMMAND_POLYCONOMY_SUBROUTINE.toString())
            .withSubcommands(
                SupportDigestSubcommand.build(plugin),
                SupportZipSubcommand.build(plugin),
            )
    }

}