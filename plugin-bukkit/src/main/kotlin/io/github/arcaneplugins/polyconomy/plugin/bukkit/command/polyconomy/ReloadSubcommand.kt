package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.DescribedThrowable
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder

object ReloadSubcommand: InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("reload")
            .withPermission(PolyPermission.COMMAND_POLYCONOMY_RELOAD.toString())
            .executes(CommandExecutor { sender, _ ->
                sender.spigot().sendMessage(ComponentBuilder(
                    "Reloading..."
                ).color(ChatColor.GREEN).build())

                try {
                    plugin.softReload()
                    sender.spigot().sendMessage(ComponentBuilder(
                        "Reloaded successfully."
                    ).color(ChatColor.GREEN).build())
                } catch (ex: Throwable) {
                    sender.spigot().sendMessage(ComponentBuilder(
                        "An error occurred! Check console for more details. Message: ${ex.message}"
                    ).color(ChatColor.RED).build())
                    if (ex !is DescribedThrowable) {
                        plugin.logger.severe("An error occurred whilst reloading Polyconomy via the `reload` subcommand. Stack trace:")
                        ex.printStackTrace()
                    }
                }

                //TODO Translatable message
            })
    }

}