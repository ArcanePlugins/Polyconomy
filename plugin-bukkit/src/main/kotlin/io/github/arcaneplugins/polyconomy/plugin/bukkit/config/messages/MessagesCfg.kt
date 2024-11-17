package io.github.arcaneplugins.polyconomy.plugin.bukkit.config.messages

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.Config
import kotlin.io.path.Path

class MessagesCfg(
    plugin: Polyconomy,
) : Config(
    plugin = plugin,
    name = "Messages",
    relativePath = Path("messages.yml")
) {

    override fun load() {
        read()
    }

}