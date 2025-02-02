package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.DescribedThrowable
import java.util.function.Supplier

object ReloadSubcommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("reload")
            .withAliases("rl")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_RELOAD.toString())
            .executes(CommandExecutor { sender, _ ->
                plugin.translations.commandPolyconomyReloadStarted.sendTo(sender)

                try {
                    plugin.softReload()
                    plugin.translations.commandPolyconomyReloadCompleted.sendTo(sender)
                } catch (ex: Throwable) {
                    plugin.translations.commandPolyconomyReloadErrorGeneric.sendTo(sender, placeholders = mapOf(
                        "message" to Supplier { ex.message ?: ex::class.java.canonicalName },
                    ))
                    if (ex !is DescribedThrowable) {
                        plugin.nativeLogger.severe("An error occurred whilst reloading Polyconomy via the `reload` subcommand. Stack trace:")
                        ex.printStackTrace()
                    }
                }
            })
    }

}