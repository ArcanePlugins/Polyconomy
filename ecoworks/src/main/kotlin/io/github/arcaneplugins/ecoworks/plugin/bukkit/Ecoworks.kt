package io.github.arcaneplugins.ecoworks.plugin.bukkit

import io.github.arcaneplugins.ecoworks.plugin.bukkit.command.CommandManager
import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.TestManager
import org.bukkit.plugin.java.JavaPlugin

class Ecoworks : JavaPlugin() {

    val cmdMgr = CommandManager(this)
    val testMgr = TestManager(this)

    override fun onLoad() {
        cmdMgr.init()
    }

    override fun onEnable() {
        cmdMgr.enable()
    }

    override fun onDisable() {
        cmdMgr.disable()
    }

}