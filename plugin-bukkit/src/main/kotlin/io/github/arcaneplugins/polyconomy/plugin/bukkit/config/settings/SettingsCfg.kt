package io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.Config
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugManager
import kotlin.io.path.Path

class SettingsCfg(
    val plugin: Polyconomy,
) : Config(
    name = "Settings",
    relativePath = Path("settings.yml")
) {
    override fun load() {
        read()
        DebugManager.load()
    }
}