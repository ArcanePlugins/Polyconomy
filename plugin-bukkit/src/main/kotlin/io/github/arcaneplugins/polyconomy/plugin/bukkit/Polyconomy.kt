package io.github.arcaneplugins.polyconomy.plugin.bukkit

import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.CommandManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.ConfigManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.EconomyManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.ListenerManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ConcurrentManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.MetricsManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.TerminateLoadException
import org.bukkit.plugin.java.JavaPlugin

/**
Welcome to Polyconomy's main class.

This serves as the 'hub' for the plugin. It's role is simple: handle the loading, enabling,
disabling, and soft-reloading of the plugin.

Being the main class of the plugin, this is instantiated by Bukkit's plugin management system. An
instance of this class may be replaced by Bukkit's plugin manager during runtime.
 */
class Polyconomy : JavaPlugin() {

    /**
     * Implements [JavaPlugin.onLoad].
     *
     * This method performs miscellaneous startup (pre-onEnable) measures.
     */
    override fun onLoad() {
        try {
            CommandManager.loadOnLoad()
        } catch(ex: Exception) {
            Log.s(
                """
                
                An error occurred whilst attempting to load Polyconomy (is it up to date?).
                
                This error may or may not be caused by Polyconomy itself - users commonly make spelling mistakes when editing config files.
                
                If you are unable to resolve this error yourself, feel free to ask our support team for help.
                Discord: https://discord.gg/HqZwdcJ
                
                A stack trace will be printed below to aid advanced users in resolving this issue:
                """.trimIndent()
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
            HookManager.ensureHardDependencies()
            ConfigManager.load()
            ConcurrentManager.startup()
            EconomyManager.load()
            StorageManager.connect()
            ListenerManager.registerAll()
            HookManager.registerAll()
            CommandManager.loadOnEnable()
            MetricsManager.register()
        } catch(ex: TerminateLoadException) {
            isEnabled = false
            return
        } catch (ex: Exception) {
            Log.s(
                """
                
                An error occurred whilst attempting to enable Polyconomy (is it up to date?).
                
                This error may or may not be caused by Polyconomy itself - users commonly make spelling mistakes when editing config files.
                
                If you are unable to resolve this error yourself, feel free to ask our support team for help.
                Discord: https://discord.gg/HqZwdcJ
                
                A stack trace will be printed below to aid advanced users in resolving this issue:
                """.trimIndent()
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
        CommandManager.unloadOnDisable()
        HookManager.unregisterAll()
        ConcurrentManager.shutdown()
        StorageManager.disconnect()
    }

    /**
     * Performs an internal 'soft reload':
     *
     * This reloads the entire hook and configuration system, and all systems within those (i.e.,
     * storage management, economy management, and so on). It also restarts the [ConcurrentManager].
     *
     * This method is called by Polyconomy's 'reload' subcommand so that administrators can easily
     * apply changes to their configuration during runtime, saving them minutes of each interruption
     * where they would otherwise restart their server after each config adjustment.
     */
    //TODO use
    @Suppress("unused")
    fun softReload() {
        Log.i("Reloading Polyconomy v${description.version}")
        try {
            /* soft-disabling */
            HookManager.unregisterAll()
            ConcurrentManager.shutdown()
            StorageManager.disconnect()

            /* re-loading */
            ConfigManager.load()
            ConcurrentManager.startup()
            EconomyManager.load()
            StorageManager.connect()
            HookManager.registerAll()
            CommandManager.reload()
        } catch(ex: Exception) {
            Log.s(
                """
                
                Error occurred while reloading Polyconomy v${description.version} (Is it up to date?).
                
                This error may or may not be caused by Polyconomy itself - users commonly make spelling mistakes when editing config files.
                
                If you are unable to resolve this error yourself, feel free to ask our support team for help.
                Discord: https://discord.gg/HqZwdcJ
                
                A stack trace will be printed below to aid advanced users in resolving this issue:
                """.trimIndent())
            ex.printStackTrace()
            throw ex
        }

        Log.i("Plugin reloaded successfully.")
    }

}