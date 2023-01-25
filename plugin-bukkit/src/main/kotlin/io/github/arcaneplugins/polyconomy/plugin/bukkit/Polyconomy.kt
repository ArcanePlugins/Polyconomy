package io.github.arcaneplugins.polyconomy.plugin.bukkit

import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class Polyconomy : JavaPlugin() {

    companion object {
        var instance: Polyconomy? = null
            private set
    }

    override fun onLoad() {
        instance = this

        logger.info("Plugin initialized.")
    }

    override fun onEnable() {
        logger.info("Plugin enabled.")
    }

    override fun onDisable() {
        logger.info("Plugin disabled.")
    }

}