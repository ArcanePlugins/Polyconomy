package io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.Config
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugManager
import kotlin.io.path.Path

object SettingsCfg : Config(
    name = "Settings",
    relativePath = Path("settings.yml")
) {
    override fun load() {
        read()
        DebugManager.load()
    }
}