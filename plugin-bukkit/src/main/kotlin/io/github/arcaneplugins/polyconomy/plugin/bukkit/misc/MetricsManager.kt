package io.github.arcaneplugins.polyconomy.plugin.bukkit.misc

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import org.bstats.bukkit.Metrics

class MetricsManager(
    val plugin: Polyconomy,
) {

    fun load() {
        Metrics(plugin, 23913)
    }

}