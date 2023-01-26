package io.github.arcaneplugins.polyconomy.plugin.bukkit.listener

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import org.bukkit.Bukkit
import org.bukkit.event.Listener

/**
 * Wraps over Bukkit's [Listener] interface to install some basic utilities for Polyconomy's
 * listener code.
 *
 * @param imperative whether this listener **must** be registered for standard plugin operation
 */
abstract class PolyListener(
    val imperative: Boolean
) : Listener {

    private var registered: Boolean = false

    fun register() {
        if(registered)
            throw IllegalStateException("${this::class.simpleName} is already registered.")

        try {
            Bukkit.getPluginManager().registerEvents(this, Polyconomy.instance!!)
            registered = true
        } catch(ex: Exception) {
            if(imperative) throw ex
        }
    }

}