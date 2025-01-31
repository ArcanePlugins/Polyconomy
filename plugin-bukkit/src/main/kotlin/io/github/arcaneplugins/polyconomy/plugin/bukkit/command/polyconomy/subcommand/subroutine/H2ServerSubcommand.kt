package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.subroutine

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import io.github.arcaneplugins.polyconomy.plugin.bukkit.scheduling.task.DbServerTask
import io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2.H2StorageHandler

object H2ServerSubcommand : InternalCmd {
    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("h2-server")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_SUBROUTINE.toString())
            .executes(CommandExecutor { sender, _ ->
                if (plugin.storageManager.handler !is H2StorageHandler) {
                    plugin.translations.commandPolyconomySubroutineH2ServerErrorImplementation.sendTo(sender)
                    throw plugin.translations.commandApiFailure()
                }

                if (DbServerTask.running) {
                    plugin.translations.commandPolyconomySubroutineH2ServerErrorAlreadyRunning.sendTo(sender)
                    throw plugin.translations.commandApiFailure()
                }

                plugin.taskManager.start(DbServerTask(plugin))

                plugin.translations.commandPolyconomySubroutineH2ServerStarted.sendTo(sender)
            })
    }
}