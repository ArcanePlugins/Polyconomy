package io.github.arcaneplugins.polyconomy.plugin.bukkit

import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.CommandManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.ConfigManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.EconomyManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.ListenerManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ExecutionManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.MetricsManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.TerminateLoadException
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

    val hookManager = HookManager(this)
    val listenerManager = ListenerManager(this)
    val metricsManager = MetricsManager(this)

    /**
     * Implements [JavaPlugin.onLoad].
     *
     * This method performs miscellaneous startup (pre-onEnable) measures.
     */
    override fun onLoad() {
        try {
            CommandManager.loadOnLoad()
        } catch(ex: Exception) {
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
            hookManager.ensureHardDependencies()
            ConfigManager.load()
            ExecutionManager.startup()
            EconomyManager.load()
            StorageManager.load()
            listenerManager.load()
            hookManager.registerAll()
            CommandManager.loadOnEnable()
            metricsManager.load()
        } catch(ex: TerminateLoadException) {
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
            CommandManager.unloadOnDisable()
            hookManager.unregisterAll()
            ExecutionManager.shutdown()
            StorageManager.disconnect()
        } catch(ex: Exception) {
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
            StorageManager.disconnect()

            /* re-loading */
            ConfigManager.load()
            ExecutionManager.startup()
            EconomyManager.load()
            StorageManager.load()
            hookManager.registerAll()
            CommandManager.reload()
        } catch(ex: Exception) {
            throw ThrowableUtil.explainHelpfully(
                this,
                ex,
                otherInfo = "Issues during reloads are often caused by user configuration errors.",
                action = "soft-reloading"
            )
        }

        logger.info("Plugin reloaded successfully.")
    }

    //TODO Use function
    /**
     * Logs the given message if the given [DebugCategory] is enabled.
     *
     * **Note:** Under normal circumstances, this method will not log any debug categories until
     * the DebugHandler has loaded the enabled categories from the Settings config. You can
     * forcefully allow debug logs to happen at any point after [Polyconomy.onLoad] is called
     * by programatically modifying [DebugManager.enabledCategories].
     *
     * @param dCat Debug category associated with the message being supplied
     * @param msg  Supplier of the message which will be accessed if the category is enabled
     */
    @Suppress("unused")
    fun debugLog(dCat: DebugCategory, msg: Supplier<Any>) {
        if(dCat.disabled()) {
            return
        }

        val output = "[DEBUG: ${dCat}] ${msg.get()}"

        logger.info(output)

        if(DebugCategory.DEBUG_BROADCAST_OPS.enabled()) {
            Bukkit.getOnlinePlayers()
                .filter(Player::isOp)
                .forEach { it.sendMessage("${ChatColor.DARK_GRAY}${output}") }
        }
    }

}