package io.github.arcaneplugins.polyconomy.plugin.bukkit.scheduling.task

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import kotlinx.coroutines.runBlocking
import org.bukkit.scheduler.BukkitRunnable

class DbCleanupTask(
    val plugin: Polyconomy,
) : BukkitRunnable(), Task {

    var running = false

    override fun start() {
        if (running || !plugin.settingsCfg.dbShouldRunCleanupTask()) {
            return
        }

        runTaskTimerAsynchronously(plugin, 0L, plugin.settingsCfg.dbCleanupTaskPeriod())
        running = true
    }

    override fun stop() {
        if (!running || isCancelled) {
            return
        }

        cancel()
    }

    override fun run() {
        runBlocking {
            plugin.storageManager.handler.cleanup()
        }
    }

}