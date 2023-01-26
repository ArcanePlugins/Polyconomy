package io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.impl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.PolyListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

object PlayerQuitListener : PolyListener(
    imperative = true
) {

    @EventHandler
    fun handleEvent(event: PlayerQuitEvent) {
        // TODO: Handle any caching required for the player.
    }

}