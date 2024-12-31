package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.subroutine

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit

object DbCleanupSubcommand : InternalCmd {
    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("db-cleanup")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_SUBROUTINE.toString())
            .executes(CommandExecutor { sender, _ ->
                Bukkit.getScheduler().runTaskAsynchronously(plugin) { _ ->
                    sender.spigot().sendMessage(ComponentBuilder("Cleaning DB...").color(ChatColor.GREEN).build())
                    runBlocking {
                        plugin.storageManager.handler.cleanup()
                    }
                    sender.spigot().sendMessage(ComponentBuilder("DB cleaned.").color(ChatColor.GREEN).build())
                }
            })
    }
}