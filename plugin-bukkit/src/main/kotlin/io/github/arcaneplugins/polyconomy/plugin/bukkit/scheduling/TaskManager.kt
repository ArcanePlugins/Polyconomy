package io.github.arcaneplugins.polyconomy.plugin.bukkit.scheduling

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.scheduling.task.DbCleanupTask
import io.github.arcaneplugins.polyconomy.plugin.bukkit.scheduling.task.Task

class TaskManager(
    val plugin: Polyconomy,
) {

    private val tasks = mutableListOf<Task>(
        DbCleanupTask(plugin),
    )

    fun start() {
        tasks.forEach(Task::start)
    }

    fun stop() {
        tasks.forEach(Task::stop)
    }

    fun start(task: Task) {
        tasks.add(task)
        task.start()
    }

}