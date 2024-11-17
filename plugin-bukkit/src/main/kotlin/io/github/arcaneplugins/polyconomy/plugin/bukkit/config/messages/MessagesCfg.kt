package io.github.arcaneplugins.polyconomy.plugin.bukkit.config.messages

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.Config
import kotlin.io.path.Path

class MessagesCfg(
    val plugin: Polyconomy,
) : Config(
    name = "Messages",
    relativePath = Path("messages.yml")
) {

    override fun load() {
        read()
    }

}