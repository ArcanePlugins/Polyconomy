package io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.impl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.PolyListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

object PlayerJoinListener : PolyListener(
    imperative = true
) {

    @EventHandler
    fun handleEvent(event: PlayerJoinEvent) {
        // TODO: If the player doesn't have an economy account, then create one.
        // TODO: Handle any caching required for the player.
    }

}