package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.local.configurate.impl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.local.configurate.ConfigurateStorageHandler
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.AbstractConfigurationLoader

class JsonStorageHandler(
    plugin: Polyconomy
) : ConfigurateStorageHandler(
    plugin = plugin,
    fileExtension = "json",
    id = "json",
) {

    override fun buildLoader(): AbstractConfigurationLoader<out ScopedConfigurationNode<*>> {
        return GsonConfigurationLoader
            .builder()
            .path(absolutePath())
            .build()
    }

}