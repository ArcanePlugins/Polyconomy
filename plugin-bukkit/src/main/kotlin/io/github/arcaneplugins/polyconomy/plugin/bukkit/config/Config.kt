package io.github.arcaneplugins.polyconomy.plugin.bukkit.config

import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path

abstract class Config(
    val name: String,
    relativePath: Path
) {

    private val loader: YamlConfigurationLoader = YamlConfigurationLoader.builder()
        .path(relativePath)
        .build()

    var rootNode: CommentedConfigurationNode? = null

    abstract fun load()

    /*
    Overwrite current root node object from the disk's contents

    Make sure to check for any exceptions when running this method, as users can make simple
    syntax errors in their configuration files.
     */
    protected fun read() {
        rootNode = loader.load()
    }

    /**
     * Write current root node object to the disk
     */
    fun save() {
        loader.save(rootNode)
    }
}