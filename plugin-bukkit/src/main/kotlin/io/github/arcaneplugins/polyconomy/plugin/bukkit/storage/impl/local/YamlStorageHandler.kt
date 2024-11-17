package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.local

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.STORAGE_YAML
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.StorageHandler
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists

class YamlStorageHandler(
    val plugin: Polyconomy,
) : StorageHandler("Yaml") {

    private val relativePath: Path = Path("data${File.separator}data.yml")

    private lateinit var loader: YamlConfigurationLoader

    private lateinit var rootNode: CommentedConfigurationNode

    private fun read() {
        plugin.debugLog(STORAGE_YAML) { "Reading data." }
        plugin.debugLog(STORAGE_YAML) { "Absolute path: ${absolutePath()}" }
        createIfNotExists()
        rootNode = loader.load()
        plugin.debugLog(STORAGE_YAML) { "Read data." }
    }

    private fun write() {
        plugin.debugLog(STORAGE_YAML) { "Writing data." }
        loader.save(rootNode)
        plugin.debugLog(STORAGE_YAML) { "Written data." }
    }

    private fun absolutePath(): Path {
        return Path(
            "${plugin.dataFolder.absolutePath}${File.separator}${relativePath}"
        )
    }

    private fun createIfNotExists() {
        val exists: Boolean = absolutePath().exists()
        plugin.debugLog(STORAGE_YAML) { "Data file exists: ${if (exists) "Yes" else "No"}" }
        if (exists) return

        plugin.debugLog(STORAGE_YAML) { "File doesn't exist; creating." }
        absolutePath().parent.createDirectories()
        absolutePath().createFile()
        plugin.debugLog(STORAGE_YAML) { "File created." }
    }

    override fun connect() {
        plugin.debugLog(STORAGE_YAML) { "Connecting." }

        if (connected)
            throw IllegalStateException("Attempted to connect whilst already connected")

        plugin.debugLog(STORAGE_YAML) { "Initialising loader." }
        loader = YamlConfigurationLoader.builder()
            .path(absolutePath())
            .build()
        plugin.debugLog(STORAGE_YAML) { "Initialised loader." }

        plugin.debugLog(STORAGE_YAML) { "Checking if file has not been created yet." }
        createIfNotExists()
        plugin.debugLog(STORAGE_YAML) { "File present; continuing." }

        plugin.debugLog(STORAGE_YAML) { "Initialising root node: reading data." }
        read()
        plugin.debugLog(STORAGE_YAML) { "Initialised root node." }

        connected = true
        plugin.debugLog(STORAGE_YAML) { "Connected." }
    }

    override fun disconnect() {
        plugin.debugLog(STORAGE_YAML) { "Disconnecting." }

        if (!connected) {
            plugin.debugLog(STORAGE_YAML) { "Attempted to disconnect, but is already disconnected." }
            return
        }

        /*
        YAML does not need any disconnect behaviour. The underlying libraries handle the file
        connection being closed with the operating system.
         */
        connected = false

        plugin.debugLog(STORAGE_YAML) { "Disconnected." }
    }

}