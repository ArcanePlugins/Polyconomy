package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.subroutine

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder

object ListAccountsSubcommand : InternalCmd {
    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("list-accounts")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_SUBROUTINE.toString())
            .executes(CommandExecutor { sender, _ ->
                // TODO: Translatable Messages
                plugin.server.scheduler.runTaskAsynchronously(plugin) { _ ->
                    sender.spigot().sendMessage(ComponentBuilder("Fetching...").color(ChatColor.GREEN).build())

                    run {
                        val compBuilder = ComponentBuilder("\n\nPlayer Accounts\n").color(ChatColor.GREEN)
                            .append("=".repeat(30) + "\n")
                            .color(ChatColor.DARK_GRAY)

                        val strBuilder = StringBuilder()

                        runBlocking {
                            plugin.storageManager.handler.getPlayerAccountIds().forEach {
                                strBuilder.append(" -> ${it}: ${plugin.storageManager.handler.playerCacheGetName(it) ?: "N/A"}\n")
                            }
                        }

                        sender.spigot()
                            .sendMessage(compBuilder.append(strBuilder.toString()).color(ChatColor.GRAY).build())
                    }

                    run {
                        val compBuilder = ComponentBuilder("\n\nNon-Player Accounts\n").color(ChatColor.GREEN)
                            .append("=".repeat(30) + "\n")
                            .color(ChatColor.DARK_GRAY)

                        val strBuilder = StringBuilder()

                        runBlocking {
                            plugin.storageManager.handler.getNonPlayerAccountIds().forEach {
                                strBuilder.append(" -> ${it}\n")
                            }
                        }

                        sender.spigot()
                            .sendMessage(compBuilder.append(strBuilder.toString()).color(ChatColor.GRAY).build())
                    }

                    sender.spigot().sendMessage(ComponentBuilder("Accounts listed.").color(ChatColor.GREEN).build())
                }
            })
    }
}