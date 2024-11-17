package io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.impl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.PolyListener
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ExecutionManager
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom

class PlayerJoinListener(
    plugin: Polyconomy,
) : PolyListener(
    plugin,
) {

    @EventHandler
    fun handle(event: PlayerJoinEvent) {
        // TODO: Handle any caching required for the player.

        handleDebugTest()
    }

    //TODO remove this method once debugging is done.
    private fun handleDebugTest() {
        if(plugin.debugManager.enabled(DebugCategory.DEBUG_TEST)) return

        plugin.debugLog(DebugCategory.DEBUG_TEST) { "Handling debug method" }

        for(i in 1..5) {
            val randomTime = ThreadLocalRandom.current().nextLong(1, 3 + 1)

            CompletableFuture
                .supplyAsync(
                    {
                        plugin.debugLog(DebugCategory.DEBUG_TEST) { "#${i}: supplying async." }
                        plugin.debugLog(DebugCategory.DEBUG_TEST) { "#${i}: waiting ${randomTime} seconds." }
                        Thread.sleep(randomTime * 1000)
                        plugin.debugLog(DebugCategory.DEBUG_TEST) { "#${i}: supplied async." }
                    },
                    ExecutionManager.execSvc
                )
                .thenAccept {
                    plugin.debugLog(DebugCategory.DEBUG_TEST) { "#${i}: accepted; done." }
                }

            plugin.debugLog(DebugCategory.DEBUG_TEST) { "CF #${i} sent" }
        }

        plugin.debugLog(DebugCategory.DEBUG_TEST) { "Finished debug handle method" }
    }

}