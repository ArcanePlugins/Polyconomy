package io.github.arcaneplugins.polyconomy.plugin.bukkit.config

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

abstract class Config(
    val plugin: Polyconomy,
    val name: String,
    val relativePath: Path,
) {

    private lateinit var loader: YamlConfigurationLoader

    lateinit var rootNode: CommentedConfigurationNode

    abstract fun load()

    /*
    Overwrite current root node object from the disk's contents.

    Make sure to check for any exceptions when running this method, as users can make simple
    syntax errors in their configuration files.
     */
    protected fun read() {
        createIfNotExists()

        loader = YamlConfigurationLoader.builder()
            .path(absolutePath())
            .build()

        rootNode = loader.load()
    }

    /**
     * Write current root node object to the disk.
     */
    @Suppress("unused") //TODO use
    fun save() {
        loader.save(rootNode)
    }

    /**
     * If the file doesn't exist on the disk, the default version of the file is written.
     */
    private fun createIfNotExists() {
        if (absolutePath().exists()) return
        plugin.saveResource(relativePath.toString(), false)
    }

    fun absolutePath(): Path {
        return Path(
            "${plugin.dataFolder.absolutePath}${File.separator}${relativePath}"
        )
    }

}