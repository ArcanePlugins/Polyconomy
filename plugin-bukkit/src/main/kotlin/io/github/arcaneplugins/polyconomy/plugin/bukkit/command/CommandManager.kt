package io.github.arcaneplugins.polyconomy.plugin.bukkit.command

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.balance.BalanceCommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.balancetop.BalancetopCommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.pay.PayCommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.PolyconomyCommand

class CommandManager(
    val plugin: Polyconomy,
) {

    fun init() {
        CommandAPI.onLoad(
            CommandAPIBukkitConfig(plugin)
                .silentLogs(true)
                .verboseOutput(false)
                .beLenientForMinorVersions(true)
        )
    }

    fun load() {
        CommandAPI.onEnable()

        registerCommands()
    }

    fun disable() {
        CommandAPI.onDisable()
    }

    private fun registerCommands() {
        listOf(
            BalanceCommand,
            BalancetopCommand,
            PayCommand,
            PolyconomyCommand,
        ).forEach {
            it.build(plugin).register(plugin)
        }
    }

}