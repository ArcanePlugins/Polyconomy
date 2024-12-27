package io.github.arcaneplugins.polyconomy.plugin.core.config.messages

import io.github.arcaneplugins.polyconomy.plugin.core.Platform
import io.github.arcaneplugins.polyconomy.plugin.core.config.Config
import kotlin.io.path.Path

class MessagesCfg(
    plugin: Platform,
) : Config(
    plugin = plugin,
    name = "Messages",
    resourcePath = Path("messages.yml")
) {

    override fun load() {
        read()
    }

}