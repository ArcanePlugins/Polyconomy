package io.github.arcaneplugins.polyconomy.plugin.bukkit.misc

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object ConcurrentManager {

    val storageSystemExecSvc: ExecutorService = Executors.newSingleThreadExecutor()

    fun shutdown() {
        storageSystemExecSvc.awaitTermination(30, TimeUnit.SECONDS)
    }

}