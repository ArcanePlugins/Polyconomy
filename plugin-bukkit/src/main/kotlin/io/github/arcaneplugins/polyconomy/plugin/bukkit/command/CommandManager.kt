package io.github.arcaneplugins.polyconomy.plugin.bukkit.command

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.balance.BalanceCommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.PolyconomyCommand

class CommandManager(
    val plugin: Polyconomy,
) {

    fun init() {
        CommandAPI.onLoad(
            CommandAPIBukkitConfig(plugin)
                .silentLogs(true)
                .verboseOutput(false)
                .usePluginNamespace()
        )
    }

    fun load() {
        CommandAPI.onEnable()

        registerCommands()
    }

    fun disable() {
        CommandAPI.onDisable()
    }

    fun reload() {
        disable()
        load()
    }

    private fun registerCommands() {
        listOf(
            BalanceCommand,
            PolyconomyCommand,
        ).forEach {
            it.build(plugin).register(plugin)
        }
    }

}