package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.subroutine

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder

object SupportDigestSubcommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("support-digest")
            .withPermission(PolyPermission.COMMAND_POLYCONOMY_SUBROUTINE.toString())
            .executes(CommandExecutor { sender, _ ->
                sender.spigot().sendMessage(
                    ComponentBuilder("Plugin: ")
                        .color(ChatColor.GREEN)
                        .append("Polyconomy v${plugin.description.version}")
                        .color(ChatColor.GRAY)
                        .append("\nServer: ")
                        .color(ChatColor.GREEN)
                        .append(plugin.server.version)
                        .color(ChatColor.GRAY)
                        .append("\nVault Lib: ")
                        .color(ChatColor.GREEN)
                        .append(plugin.server.pluginManager.getPlugin("Vault")?.description?.version ?: "Not Installed")
                        .color(ChatColor.GRAY)
                        .append("\nTreasury Lib: ")
                        .color(ChatColor.GREEN)
                        .append(plugin.server.pluginManager.getPlugin("Treasury")?.description?.version ?: "Not Installed")
                        .color(ChatColor.GRAY)
                        .append("\nStorage Implementation: ")
                        .color(ChatColor.GREEN)
                        .append(plugin.settings.storageImplementation())
                        .color(ChatColor.GRAY)
                        .build()
                )
            })
    }

}