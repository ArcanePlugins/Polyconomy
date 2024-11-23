package io.github.arcaneplugins.polyconomy.plugin.bukkit

import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.CommandManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.Config
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.messages.MessagesCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.ListenerManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ExecutionManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.MetricsManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.DescribedThrowable
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.ThrowableUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Supplier

/**
Welcome to Polyconomy's main class.

This serves as the 'hub' for the plugin. It's role is simple: handle the loading, enabling,
disabling, and soft-reloading of the plugin.

Being the main class of the plugin, this is instantiated by Bukkit's plugin management system. An
instance of this class may be replaced by Bukkit's plugin manager during runtime.
 */
class Polyconomy : JavaPlugin() {

    val debugManager = DebugManager(this)
    val storageManager = StorageManager(this)
    val commandManager = CommandManager(this)
    val hookManager = HookManager(this)
    val listenerManager = ListenerManager(this)
    val metricsManager = MetricsManager(this)

    val settings = SettingsCfg(this)
    val messages = MessagesCfg(this)

    val configs: LinkedHashSet<Config> = linkedSetOf(
        settings,
        messages,
    )

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
            throw ThrowableUtil.explainHelpfully(
                this,
                ex,
                otherInfo = "Issues during initialisation are sometimes caused by incompatible server software / versions.",
                action = "initialising"
            )
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
            ExecutionManager.startup()
            storageManager.load()
            listenerManager.load()
            hookManager.registerAll()
            commandManager.load()
            metricsManager.load()
        } catch (_: DescribedThrowable) {
            // error that's been described already - disable plugin
            isEnabled = false
            return
        } catch (ex: Exception) {
            throw ThrowableUtil.explainHelpfully(
                this,
                ex,
                otherInfo = "Issues during startup are often caused by user configuration errors.",
                action = "enabling"
            )
        }
    }

    /**
     * Implements [JavaPlugin.onDisable].
     *
     * This method performs all of the shutdown behaviour.
     */
    override fun onDisable() {
        try {
            commandManager.disable()
            hookManager.unregisterAll()
            ExecutionManager.shutdown()
            storageManager.disconnect()
        } catch(_: DescribedThrowable) {
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
     * storage management, economy management, and so on). It also restarts the [ExecutionManager].
     *
     * This method is called by Polyconomy's 'reload' subcommand so that administrators can easily
     * apply changes to their configuration during runtime, saving them minutes of each interruption
     * where they would otherwise restart their server after each config adjustment.
     */
    //TODO use function for the reload subcommand once implemented.
    @Suppress("unused")
    fun softReload() {
        logger.info("Reloading Polyconomy v${description.version}")
        try {
            /* soft-disabling */
            hookManager.unregisterAll()
            ExecutionManager.shutdown()
            storageManager.disconnect()

            /* re-loading */
            loadConfigs()
            ExecutionManager.startup()
            storageManager.load()
            hookManager.registerAll()
            commandManager.reload()
        } catch (ex: Exception) {
            throw ThrowableUtil.explainHelpfully(
                this,
                ex,
                otherInfo = "Issues during reloads are often caused by user configuration errors.",
                action = "soft-reloading"
            )
        }

        logger.info("Plugin reloaded successfully.")
    }

    /**
     * Logs the given message if the given [DebugCategory] is enabled.
     *
     * **Note:** Under normal circumstances, this method will not log any debug categories until
     * the DebugHandler has loaded the enabled categories from the Settings config. You can
     * forcefully allow debug logs to happen at any point after [Polyconomy.onLoad] is called
     * by programatically modifying [DebugManager.enabledCategories].
     *
     * @param cat Debug category associated with the message being supplied
     * @param msg Supplier of the message which will be accessed if the category is enabled
     */
    @Suppress("unused")
    fun debugLog(cat: DebugCategory, msg: Supplier<Any>) {
        if (debugManager.enabled(cat)) {
            return
        }

        val output = "[DEBUG: ${cat}] ${msg.get()}"

        logger.info(output)

        if (debugManager.enabled(DebugCategory.DEBUG_BROADCAST_OPS)) {
            Bukkit.getOnlinePlayers()
                .filter(Player::isOp)
                .forEach { it.sendMessage("${ChatColor.DARK_GRAY}${output}") }
        }
    }

    fun loadConfigs() {
        if (storageManager.connected()) {
            debugLog(DebugCategory.CONFIG_MANAGER) { "Storage manager was connected - disconnecting." }
            storageManager.disconnect()
            debugLog(DebugCategory.CONFIG_MANAGER) { "Disconnected storage manager; continuing." }
        }

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

}