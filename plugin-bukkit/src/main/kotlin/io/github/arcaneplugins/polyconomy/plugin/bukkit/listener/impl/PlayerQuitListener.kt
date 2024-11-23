package io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.impl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.PolyListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(
    plugin: Polyconomy,
) : PolyListener(
    plugin,
) {

    @Suppress("EmptyMethod")
    @EventHandler
    fun handle(event: PlayerQuitEvent) {
    }

}