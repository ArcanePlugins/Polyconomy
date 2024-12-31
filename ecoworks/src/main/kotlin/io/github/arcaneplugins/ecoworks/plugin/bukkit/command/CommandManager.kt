package io.github.arcaneplugins.ecoworks.plugin.bukkit.command

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import io.github.arcaneplugins.ecoworks.plugin.bukkit.Ecoworks

class CommandManager(
    val plugin: Ecoworks
) {

    fun init() {
        CommandAPI.onLoad(
            CommandAPIBukkitConfig(plugin)
                .silentLogs(true)
                .usePluginNamespace()
                .verboseOutput(false)
        )
    }

    fun enable() {
        CommandAPI.onEnable()
        registerCommands()
    }

    fun disable() {
        CommandAPI.onDisable()
    }

    private fun registerCommands() {
        EcoworksCommand(plugin).cmd.register()
    }

}