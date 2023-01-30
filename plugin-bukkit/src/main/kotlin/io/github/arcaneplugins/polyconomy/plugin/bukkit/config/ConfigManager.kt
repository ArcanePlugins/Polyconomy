package io.github.arcaneplugins.polyconomy.plugin.bukkit.config

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.messages.MessagesCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log

object ConfigManager {

    val configs: LinkedHashSet<Config> = linkedSetOf(
        SettingsCfg,
        MessagesCfg,
    )

    fun load() {
        Log.i("Loading configs.")

        if(StorageManager.connected())
            StorageManager.disconnect()

        try {
            configs.forEach { config ->
                Log.i("Loading config '${config.relativePath}'.")
                config.load()
            }
        } catch(ex: Exception) {
            Log.s("Unable to load configs. You have most likely created an accidental " +
                    "syntax error, such as a stray apostrophe or misaligned indentation. Prior " +
                    "to seeking assistance with this error, please use a YAML parser website to " +
                    "validate all of your YAML config files.")
            throw ex
        }
    }
}