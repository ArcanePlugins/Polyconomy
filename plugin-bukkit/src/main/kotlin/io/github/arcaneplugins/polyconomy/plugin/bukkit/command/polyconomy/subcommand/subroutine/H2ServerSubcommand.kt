package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.subroutine

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import io.github.arcaneplugins.polyconomy.plugin.bukkit.scheduling.task.DbServerTask
import io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2.H2StorageHandler
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder

object H2ServerSubcommand : InternalCmd {
    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("h2-server")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_SUBROUTINE.toString())
            .executes(CommandExecutor { sender, _ ->
                if (plugin.storageManager.handler !is H2StorageHandler) {
                    throw CommandAPI.failWithString("Current storage implementation is not H2. (Do you need to reload?)")
                }

                if (DbServerTask.running) {
                    throw CommandAPI.failWithString("H2 debug web server is already running.")
                }

                plugin.taskManager.start(DbServerTask(plugin))

                sender.spigot().sendMessage(
                    ComponentBuilder("Started H2 debug web server. To stop the server, please restart your server.")
                        .color(ChatColor.GREEN)
                        .build()
                )
            })
    }
}