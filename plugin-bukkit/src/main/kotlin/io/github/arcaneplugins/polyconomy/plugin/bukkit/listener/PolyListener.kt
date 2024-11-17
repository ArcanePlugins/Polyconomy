package io.github.arcaneplugins.polyconomy.plugin.bukkit.listener

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.DescribedThrowable
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.ThrowableUtil
import org.bukkit.Bukkit
import org.bukkit.event.Listener

/**
 * Wraps over Bukkit's [Listener] interface to install some basic utilities for Polyconomy's
 * listener code.
 */
abstract class PolyListener(
    val plugin: Polyconomy,
) : Listener {

    private var registered: Boolean = false

    fun register() {
        if (registered) {
            throw IllegalStateException("${this::class.simpleName} is already registered.")
        }

        try {
            Bukkit.getPluginManager().registerEvents(this, plugin)
            registered = true
        } catch (ex: DescribedThrowable) {
            throw ex
        } catch (ex: Exception) {
            throw ThrowableUtil.explainHelpfully(
                plugin,
                ex,
                otherContext = "Whilst registering event listener: ${javaClass.simpleName}"
            )
        }
    }

}