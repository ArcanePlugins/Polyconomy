package io.github.arcaneplugins.polyconomy.plugin.bukkit.misc

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object ConcurrentManager {

    lateinit var execSvc: ExecutorService
        private set

    /**
     * Performs a start-up for the concurrency management system.
     */
    fun startup() {
        execSvc = Executors.newFixedThreadPool(10)
        //execSvc = Executors.newCachedThreadPool()
    }

    /**
     * Performs a shut-down for the concurrency management system.
     */
    fun shutdown() {
        listOf(
            execSvc
        ).forEach {
            it.shutdown()
            it.awaitTermination(5, TimeUnit.SECONDS)
        }
    }

}