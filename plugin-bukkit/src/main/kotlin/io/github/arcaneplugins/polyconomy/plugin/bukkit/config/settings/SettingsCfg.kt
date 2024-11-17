package io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.Config
import kotlin.io.path.Path

class SettingsCfg(
    plugin: Polyconomy,
) : Config(
    plugin = plugin,
    name = "Settings",
    relativePath = Path("settings.yml")
) {
    override fun load() {
        read()
        plugin.debugManager.load()
    }
}