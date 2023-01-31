package io.github.arcaneplugins.polyconomy.plugin.bukkit.misc

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object ConcurrentManager {

    lateinit var singleThreadExecSvc: ExecutorService
        private set

    /**
     * Performs a start-up for the concurrency management system.
     */
    fun startup() {
        singleThreadExecSvc = Executors.newSingleThreadExecutor()
    }

    /**
     * Performs a shut-down for the concurrency management system.
     */
    fun shutdown() {
        singleThreadExecSvc.shutdown()
        singleThreadExecSvc.awaitTermination(30, TimeUnit.SECONDS)
    }

}