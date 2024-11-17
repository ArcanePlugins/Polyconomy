package io.github.arcaneplugins.polyconomy.plugin.bukkit.misc

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import org.bstats.bukkit.Metrics

object MetricsManager {

    fun register(plugin: Polyconomy) {
        Metrics(plugin, 23913)
    }

}