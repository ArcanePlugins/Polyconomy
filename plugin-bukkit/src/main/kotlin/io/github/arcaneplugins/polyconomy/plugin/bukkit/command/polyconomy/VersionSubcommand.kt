package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission

object VersionSubcommand: InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("version")
            .withPermission(PolyPermission.COMMAND_POLYCONOMY_VERSION.toString())
            .executes(CommandExecutor { sender, _ ->
                val pdf = plugin.description
                sender.sendMessage("${pdf.name} v${pdf.version} by ${pdf.authors.joinToString(", ")}")
                //TODO Translatable message
            })
    }

}