package io.github.arcaneplugins.polyconomy.plugin.bukkit.config.messages

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.Config
import kotlin.io.path.Path

object MessagesCfg : Config(
    name = "Messages",
    relativePath = Path("messages.yml")
) {

    override fun load() {
        read()
    }

}