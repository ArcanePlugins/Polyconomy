package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.local.configurate.impl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.local.configurate.ConfigurateStorageHandler
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

class YamlStorageHandler(
    plugin: Polyconomy
) : ConfigurateStorageHandler(
    plugin = plugin,
    fileExtension = "yml",
    id = "yaml",
) {

    override fun buildLoader(): AbstractConfigurationLoader<out ScopedConfigurationNode<*>> {
        return YamlConfigurationLoader
            .builder()
            .path(absolutePath())
            .build()
    }

}