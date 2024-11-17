package io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.impl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory
import io.github.arcaneplugins.polyconomy.plugin.bukkit.listener.PolyListener
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ExecutionManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom

object PlayerJoinListener : PolyListener(
    imperative = true
) {

    @EventHandler
    fun handleEvent(event: PlayerJoinEvent) {
        // TODO: Handle any caching required for the player.

        handleDebugTest()
    }

    private fun handleDebugTest() {
        if(DebugCategory.DEBUG_TEST.disabled()) return

        Log.i("Handling debug method")

        for(i in 1..5) {
            val randomTime = ThreadLocalRandom.current().nextLong(1, 3 + 1)

            CompletableFuture
                .supplyAsync(
                    {
                        Log.i("#${i}: supplying async.")
                        Log.i("#${i}: waiting ${randomTime} seconds.")
                        Thread.sleep(randomTime * 1000)
                        Log.i("#${i}: supplied async.")
                    },
                    ExecutionManager.execSvc
                )
                .thenAccept {
                    Log.i("#${i}: accepted; done.")
                }

            Log.i("CF #${i} sent")
        }

        Log.i("Finished debug handle method")
    }

}