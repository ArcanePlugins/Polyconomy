package io.github.arcaneplugins.polyconomy.plugin.bukkit

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy.Companion.instance
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.CommandManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.ConfigManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.EconomyManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.ListenerManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ConcurrentManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.MetricsManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyStopwatch
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.TerminateLoadException
import org.bukkit.plugin.java.JavaPlugin

/**
Welcome to Polyconomy's main class.

This serves as the 'hub' for the plugin. It's role is simple: handle the loading, enabling,
disabling, and soft-reloading of the plugin.

Being the main class of the plugin, this is instantiated by Bukkit's plugin management system. An
instance of this class may be replaced by Bukkit's plugin manager during runtime, this is tracked
by the [instance] property within a companion object.
 */
class Polyconomy : JavaPlugin() {

    companion object {

        /**
         * Stores the latest instance of the Polyconomy main class as instantiated by Bukkit's
         * plugin manager.
         *
         *  1) This allows easy 'static' access to the main class without using patterns like
         *  dependency injection.
         *
         *  2) As the particular object this property references can change during events such as
         *  when an administrator uses the `/reload` command on their server, this vastly improves
         *  compatibility with the (convenient, but discouraged) `/reload` command.
         */
        lateinit var instance: Polyconomy
            private set
    }

    /**
     * Implements [JavaPlugin.onLoad].
     *
     * This method performs very basic start-up measures. At the moment, its only purpose is to
     * set the [instance] property in the companion object.
     */
    override fun onLoad() {
        val stopwatch = PolyStopwatch()

        instance = this

        try {
            HookManager.ensureHardDependencies()
            CommandManager.loadOnLoad()
        } catch(ex: TerminateLoadException) {
            isEnabled = false
            return
        }

        Log.i("Plugin initialized (took ${stopwatch.stop()}).")
    }

    /**
     * Implements [JavaPlugin.onEnable].
     *
     * Runs the majority of the startup behaviour.
     */
    override fun onEnable() {
        val stopwatch = PolyStopwatch()

        try {
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
        }

        Log.i("Plugin enabled (took ${stopwatch.stop()}).")
    }

    /**
     * Implements [JavaPlugin.onDisable].
     *
     * This method performs all of the shutdown behaviour.
     */
    override fun onDisable() {
        val stopwatch = PolyStopwatch()

        CommandManager.unloadOnDisable()
        HookManager.unregisterAll()
        ConcurrentManager.shutdown()
        StorageManager.disconnect()

        Log.i("Plugin disabled (took ${stopwatch.stop()}).")
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
        val stopwatch = PolyStopwatch()

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
            Log.s("""Error occurred while reloading Polyconomy v${description.version} (Is it up to date?)""")
            ex.printStackTrace()
            throw ex
        }

        Log.i("Plugin reloaded (took ${stopwatch.stop()}).")
    }

}