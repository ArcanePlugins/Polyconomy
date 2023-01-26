package io.github.arcaneplugins.polyconomy.plugin.bukkit.listener

import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.impl.PlayerJoinListener
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.impl.PlayerQuitListener

object ListenerManager {

    val listeners: LinkedHashSet<PolyListener> = linkedSetOf(
        PlayerJoinListener,
        PlayerQuitListener
    )

    fun registerAll() {
        listeners.forEach(PolyListener::register)
    }

}