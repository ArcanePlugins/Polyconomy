package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.configurate.impl

import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.configurate.ConfigurateStorageHandler
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import java.nio.file.Path

class JsonStorageHandler(
    absolutePath: Path,
    manager: StorageManager,
) : ConfigurateStorageHandler(
    absolutePath = absolutePath,
    manager = manager,
    id = "json",
) {

    override fun buildLoader(): AbstractConfigurationLoader<out ScopedConfigurationNode<*>> {
        return GsonConfigurationLoader
            .builder()
            .path(absolutePath)
            .build()
    }

}