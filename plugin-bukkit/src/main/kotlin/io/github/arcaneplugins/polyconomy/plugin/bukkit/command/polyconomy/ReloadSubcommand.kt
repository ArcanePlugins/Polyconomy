package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.DescribedThrowable

object ReloadSubcommand: InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("reload")
            .withPermission(PolyPermission.COMMAND_POLYCONOMY_RELOAD.toString())
            .executes(CommandExecutor { sender, _ ->
                sender.sendMessage("Reloading...")

                try {
                    plugin.softReload()
                    sender.sendMessage("Reloaded successfully!")
                } catch (ex: Throwable) {
                    sender.sendMessage("An error occurred! Check console for more details. Message: ${ex.message}")
                    if (ex !is DescribedThrowable) {
                        plugin.logger.severe("An error occurred whilst reloading Polyconomy via the `reload` subcommand. Stack trace:")
                        ex.printStackTrace()
                    }
                }

                //TODO Translatable message
            })
    }

}