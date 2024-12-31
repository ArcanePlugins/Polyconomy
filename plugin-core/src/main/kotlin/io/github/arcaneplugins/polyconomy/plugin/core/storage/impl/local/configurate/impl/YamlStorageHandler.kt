package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.configurate.impl

import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.configurate.ConfigurateStorageHandler
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path

class YamlStorageHandler(
    absolutePath: Path,
    manager: StorageManager,
) : ConfigurateStorageHandler(
    absolutePath = absolutePath,
    manager = manager,
    id = "yaml",
) {

    override fun buildLoader(): AbstractConfigurationLoader<out ScopedConfigurationNode<*>> {
        return YamlConfigurationLoader
            .builder()
            .path(absolutePath)
            .build()
    }

}