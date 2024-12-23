package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy

import dev.jorel.commandapi.CommandAPICommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission

object PolyconomyCommand: InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("polyconomy")
            .withPermission(PolyPermission.COMMAND_POLYCONOMY.toString())
            .withSubcommand(VersionSubcommand.build(plugin))
    }

}