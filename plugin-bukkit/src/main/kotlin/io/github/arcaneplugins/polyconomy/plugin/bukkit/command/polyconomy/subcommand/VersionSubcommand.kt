package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder

object VersionSubcommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("version")
            .withAliases("about", "info", "ver")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_VERSION.toString())
            .executes(CommandExecutor { sender, _ ->
                val pdf = plugin.description
                sender.spigot().sendMessage(
                    ComponentBuilder(
                        "${pdf.name} v${pdf.version} by ${pdf.authors.joinToString(", ")}"
                    ).color(ChatColor.GREEN).build()
                )
            })
    }

}