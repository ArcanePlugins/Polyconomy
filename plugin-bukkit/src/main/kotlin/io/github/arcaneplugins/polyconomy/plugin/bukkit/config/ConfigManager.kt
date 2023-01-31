package io.github.arcaneplugins.polyconomy.plugin.bukkit.config

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.messages.MessagesCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.CONFIG_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log

object ConfigManager {

    val configs: LinkedHashSet<Config> = linkedSetOf(
        SettingsCfg,
        MessagesCfg,
    )

    fun load() {
        Log.i("Loading configs.")

        if(StorageManager.connected()) {
            Log.d(CONFIG_MANAGER) { "Storage manager was connected - disconnecting." }
            StorageManager.disconnect()
            Log.d(CONFIG_MANAGER) { "Disconnected storage manager; continuing." }
        }

        try {
            configs.forEach { config ->
                Log.d(CONFIG_MANAGER) { "Loading config ${config.name}." }
                config.load()
                Log.d(CONFIG_MANAGER) { "Loaded config ${config.name}." }
            }
        } catch(ex: Exception) {
            Log.d(CONFIG_MANAGER) { "Caught exception ${ex::class.simpleName}; re-throwing." }

            Log.s("Unable to load configs. You have most likely created an accidental " +
                    "syntax error, such as a stray apostrophe or misaligned indentation. Prior " +
                    "to seeking assistance with this error, please use a YAML parser website to " +
                    "validate all of your YAML config files.")

            throw ex
        }
    }
}