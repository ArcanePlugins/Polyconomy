package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission

object BackupSubcommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("backup")
            .withPermission(PolyPermission.COMMAND_POLYCONOMY_BACKUP.toString())
            .executes(CommandExecutor { _, _ ->
                // TODO: Implement
                throw CommandAPI.failWithString("Not yet implemented!")
            })
    }

}