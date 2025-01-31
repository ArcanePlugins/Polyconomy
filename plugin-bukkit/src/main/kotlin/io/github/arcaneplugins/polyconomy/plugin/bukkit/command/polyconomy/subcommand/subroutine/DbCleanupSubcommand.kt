package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.subroutine

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit

object DbCleanupSubcommand : InternalCmd {
    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("db-cleanup")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_SUBROUTINE.toString())
            .executes(CommandExecutor { sender, _ ->
                plugin.translations.commandPolyconomySubroutineDbCleanupStart.sendTo(sender)
                Bukkit.getScheduler().runTaskAsynchronously(plugin) { _ ->
                    runBlocking {
                        plugin.storageManager.handler.cleanup()
                        plugin.translations.commandPolyconomySubroutineDbCleanupComplete.sendTo(sender)
                    }
                }
            })
    }
}