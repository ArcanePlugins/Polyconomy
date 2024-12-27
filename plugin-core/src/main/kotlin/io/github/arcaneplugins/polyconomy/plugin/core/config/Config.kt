package io.github.arcaneplugins.polyconomy.plugin.core.config

import io.github.arcaneplugins.polyconomy.plugin.core.Platform
import io.github.arcaneplugins.polyconomy.plugin.core.util.FileUtil
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name

abstract class Config(
    val plugin: Platform,
    val name: String,
    val resourcePath: Path,
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
        if (absolutePath().exists()) {
            return
        }

        FileUtil.saveResource(resourcePath.name, plugin.dataFolder().absolutePathString())
    }

    fun absolutePath(): Path {
        return Path(
            "${plugin.dataFolder().absolutePathString()}${File.separator}${resourcePath}"
        )
    }

}