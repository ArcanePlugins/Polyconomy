package io.github.arcaneplugins.polyconomy.plugin.bukkit.scheduling.task

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2.H2StorageHandler
import org.bukkit.scheduler.BukkitRunnable

class DbServerTask(val plugin: Polyconomy) : BukkitRunnable(), Task {

    companion object {
        var running = false
    }

    override fun run() {
        (plugin.storageManager.handler as H2StorageHandler).startDebugServer()
    }

    override fun start() {
        run()
        running = true
    }

    override fun stop() {
        cancel()
        running = false
    }

}