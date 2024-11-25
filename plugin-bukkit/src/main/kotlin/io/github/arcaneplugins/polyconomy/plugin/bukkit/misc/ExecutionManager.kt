package io.github.arcaneplugins.polyconomy.plugin.bukkit.misc

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object ExecutionManager {

    var execSvc: ExecutorService? = null
        private set

    /**
     * Performs a start-up for the concurrency management system.
     */
    fun startup() {
        execSvc = Executors.newSingleThreadExecutor()
    }

    /**
     * Performs a shut-down for the concurrency management system.
     */
    fun shutdown() {
        listOf(
            execSvc
        ).forEach {
            if (it == null) return@forEach

            it.shutdown()
            it.awaitTermination(
                30,
                TimeUnit.SECONDS
            )
        }
    }

}