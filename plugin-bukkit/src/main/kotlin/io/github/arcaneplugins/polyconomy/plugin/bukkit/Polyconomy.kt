package io.github.arcaneplugins.polyconomy.plugin.bukkit

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.ConfigManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.ListenerManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.MetricsManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyStopwatch
import org.bukkit.plugin.java.JavaPlugin

/**
Main class

Note - we are unable to declare this as an `object` type, since it is instantiated by Bukkit.
 */
class Polyconomy : JavaPlugin() {

    companion object {
        var instance: Polyconomy? = null
            private set
    }

    override fun onLoad() {
        val stopwatch = PolyStopwatch()

        instance = this

        Log.i("Plugin initialized (took ${stopwatch.stop()}).")
    }

    override fun onEnable() {
        val stopwatch = PolyStopwatch()

        ConfigManager.load()
        ListenerManager.registerAll()
        HookManager.registerAll()
        MetricsManager.register()

        Log.i("Plugin enabled (took ${stopwatch.stop()}).")
    }

    override fun onDisable() {
        val stopwatch = PolyStopwatch()

        HookManager.unregisterAll()

        Log.i("Plugin disabled (took ${stopwatch.stop()}).")
    }

    /**
     * Performs a 'soft reload'; only the configuration and storage systems are reloaded.
     */
    //TODO use
    @Suppress("unused")
    fun reload() {
        Log.i("Reloading Polyconomy v${description.version}")
        val stopwatch = PolyStopwatch()

        try {
            /* soft-disabling */
            HookManager.unregisterAll()

            /* re-loading */
            ConfigManager.load()
            HookManager.registerAll()
        } catch(ex: Exception) {
            Log.s("""Error occurred while reloading Polyconomy v${description.version} (Is it up to date?)""")
            ex.printStackTrace()
            throw ex
        }

        Log.i("Plugin reloaded (took ${stopwatch.stop()}).")
    }

}