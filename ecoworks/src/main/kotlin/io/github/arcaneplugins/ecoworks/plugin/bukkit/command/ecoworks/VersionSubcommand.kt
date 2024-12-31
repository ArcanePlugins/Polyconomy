package io.github.arcaneplugins.ecoworks.plugin.bukkit.command.ecoworks

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.ecoworks.plugin.bukkit.Ecoworks
import io.github.arcaneplugins.ecoworks.plugin.bukkit.misc.EcoworksPerm
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder

class VersionSubcommand(plugin: Ecoworks) {

    val cmd: CommandAPICommand = CommandAPICommand("version")
        .withPermission(EcoworksPerm.COMMAND_ECOWORKS_VERSION.toString())
        .executes(CommandExecutor { sender, _ ->
            val name = plugin.description.name
            val version = plugin.description.version
            val authors = plugin.description.authors.joinToString(", ")

            val cb = ComponentBuilder("${name} v${version} by ${authors}")
                .color(ChatColor.GREEN)

            sender.spigot().sendMessage(cb.build())
        })
}