package io.github.arcaneplugins.polyconomy.plugin.bukkit.listener

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.impl.PlayerJoinListener
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.impl.PlayerQuitListener

class ListenerManager(
    val plugin: Polyconomy,
) {

    val listeners: LinkedHashSet<PolyListener> = linkedSetOf(
        PlayerJoinListener(plugin),
        PlayerQuitListener(plugin)
    )

    fun load() {
        listeners.forEach(PolyListener::register)
    }

}