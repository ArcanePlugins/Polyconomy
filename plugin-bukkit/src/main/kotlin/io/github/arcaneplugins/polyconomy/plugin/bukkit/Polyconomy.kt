package io.github.arcaneplugins.polyconomy.plugin.bukkit

import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.CommandManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.translations.TranslationHandlerImpl
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.ListenerManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.MetricsManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.scheduling.TaskManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.DescribedThrowable
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.ThrowableUtil
import io.github.arcaneplugins.polyconomy.plugin.core.Platform
import io.github.arcaneplugins.polyconomy.plugin.core.config.Config
import io.github.arcaneplugins.polyconomy.plugin.core.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.core.config.translations.TranslationsCfg
import io.github.arcaneplugins.polyconomy.plugin.core.debug.DebugCategory
import io.github.arcaneplugins.polyconomy.plugin.core.debug.DebugManager
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageManager
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path
import java.util.logging.Logger

/**
Welcome to Polyconomy's main class.

This serves as the 'hub' for the plugin. It's role is simple: handle the loading, enabling,
disabling, and soft-reloading of the plugin.

Being the main class of the plugin, this is instantiated by Bukkit's plugin management system. An
instance of this class may be replaced by Bukkit's plugin manager during runtime.
 */
class Polyconomy : JavaPlugin(), Platform {

    override val debugManager = DebugManager(this)
    override lateinit var storageManager: StorageManager
    val commandManager = CommandManager(this)
    val hookManager = HookManager(this)
    val listenerManager = ListenerManager(this)
    val metricsManager = MetricsManager(this)
    val taskManager = TaskManager(this)
    override val settingsCfg = SettingsCfg(this)
    override val translationsCfg = TranslationsCfg(this)
    override val configs: LinkedHashSet<Config> = linkedSetOf(
        settingsCfg,
        translationsCfg,
    )
    override val nativeLogger: Logger
        get() = super.getLogger()
    val translations = TranslationHandlerImpl(this)

    /**
     * Implements [JavaPlugin.onLoad].
     *
     * This method performs miscellaneous startup (pre-onEnable) measures.
     */
    override fun onLoad() {
        try {
            commandManager.init()
        } catch (_: DescribedThrowable) {
            isEnabled = false
            // described throwable happened here - disable plugin
            return
        } catch (ex: Exception) {
            ThrowableUtil.explainHelpfully(
                this,
                ex,
                otherInfo = "Issues during initialisation are sometimes caused by incompatible server software / versions.",
                action = "initialising"
            )

            throw ex
        }
    }

    /**
     * Implements [JavaPlugin.onEnable].
     *
     * Runs the majority of the startup behaviour.
     */
    override fun onEnable() {
        try {
            loadConfigs()
            storageManager = StorageManager(
                plugin = this,
                dataFolder = dataFolder,
                minimumBalance = settingsCfg.minimumBalance(),
                primaryCurrencyId = settingsCfg.primaryCurrencyId()
            )
            storageManager.startup(settingsCfg.storageImplementation())
            listenerManager.load()
            hookManager.registerAll()
            commandManager.load()
            metricsManager.load()
            taskManager.start()
        } catch (_: DescribedThrowable) {
            // error that's been described already - disable plugin
            isEnabled = false
            return
        } catch (ex: Exception) {
            ThrowableUtil.explainHelpfully(
                this,
                ex,
                otherInfo = "Issues during startup are often caused by user configuration errors.",
                action = "enabling"
            )

            throw ex
        }
    }

    /**
     * Implements [JavaPlugin.onDisable].
     *
     * This method performs all of the shutdown behaviour.
     */
    override fun onDisable() {
        try {
            taskManager.stop()
            commandManager.disable()
            hookManager.unregisterAll()
            storageManager.shutdown()
        } catch (_: DescribedThrowable) {
            // error that's been described already - already disabling, no action needed.
            return
        } catch (ex: Exception) {
            throw ThrowableUtil.explainHelpfully(
                this,
                ex,
                action = "disabling"
            )
        }
    }

    /**
     * Performs an internal 'soft reload':
     *
     * This reloads the entire hook and configuration system, and all systems within those (i.e.,
     * storage management, economy management, and so on).
     *
     * This method is called by Polyconomy's 'reload' subcommand so that administrators can easily
     * apply changes to their configuration during runtime, saving them minutes of each interruption
     * where they would otherwise restart their server after each config adjustment.
     */
    @Suppress("unused")
    fun softReload() {
        nativeLogger.info("Reloading Polyconomy v${description.version}")
        try {
            /* soft-disabling */
            taskManager.stop()
            hookManager.unregisterAll()
            storageManager.shutdown()

            /* re-loading */
            loadConfigs()
            storageManager.startup(settingsCfg.storageImplementation())
            hookManager.registerAll()
            taskManager.start()
        } catch (ex: Exception) {
            throw ThrowableUtil.explainHelpfully(
                this,
                ex,
                otherInfo = "Issues during reloads are often caused by user configuration errors.",
                action = "soft-reloading"
            )
        }

        nativeLogger.info("Plugin reloaded successfully.")
    }

    fun loadConfigs() {
        try {
            configs.forEach { config ->
                debugLog(DebugCategory.CONFIG_MANAGER) { "Loading config ${config.name}." }
                config.load()
                debugLog(DebugCategory.CONFIG_MANAGER) { "Loaded config ${config.name}." }
            }
        } catch (ex: DescribedThrowable) {
            throw ex
        } catch (ex: Exception) {
            debugLog(DebugCategory.CONFIG_MANAGER) { "Caught exception ${ex::class.simpleName}; re-throwing." }

            throw ThrowableUtil.explainHelpfully(
                plugin = this,
                throwable = ex,
                otherInfo = "Unable to load configs. You have most likely created an accidental " +
                        "syntax error, such as a stray apostrophe or misaligned indentation. Prior " +
                        "to seeking assistance with this error, please use a YAML parser website to " +
                        "validate all of your YAML config files.",
                otherContext = "Loading configs"
            )
        }
    }

    override fun dataFolder(): Path {
        return dataFolder.toPath()
    }

}