package io.github.arcaneplugins.polyconomy.plugin.bukkit.config

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.messages.MessagesCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.CONFIG_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.DescribedThrowable
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.ThrowableUtil

class ConfigManager(
    val plugin: Polyconomy,
) {

    val settings = SettingsCfg(plugin)
    val messages = MessagesCfg(plugin)

    val configs: LinkedHashSet<Config> = linkedSetOf(
        settings,
        messages,
    )

    fun load() {
        if(plugin.storageManager.connected()) {
            plugin.debugLog(CONFIG_MANAGER) { "Storage manager was connected - disconnecting." }
            plugin.storageManager.disconnect()
            plugin.debugLog(CONFIG_MANAGER) { "Disconnected storage manager; continuing." }
        }

        try {
            configs.forEach { config ->
                plugin.debugLog(CONFIG_MANAGER) { "Loading config ${config.name}." }
                config.load()
                plugin.debugLog(CONFIG_MANAGER) { "Loaded config ${config.name}." }
            }
        } catch(ex: DescribedThrowable) {
            throw ex
        } catch(ex: Exception) {
            plugin.debugLog(CONFIG_MANAGER) { "Caught exception ${ex::class.simpleName}; re-throwing." }

            throw ThrowableUtil.explainHelpfully(
                plugin = plugin,
                throwable = ex,
                otherInfo = "Unable to load configs. You have most likely created an accidental " +
                        "syntax error, such as a stray apostrophe or misaligned indentation. Prior " +
                        "to seeking assistance with this error, please use a YAML parser website to " +
                        "validate all of your YAML config files.",
                otherContext = "Loading configs"
            )
        }
    }
}