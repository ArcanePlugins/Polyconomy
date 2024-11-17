package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.STORAGE_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.impl.local.YamlStorageHandler

class StorageManager(
    val plugin: Polyconomy,
) {

    val availableHandlers: MutableSet<StorageHandler> = mutableSetOf(
        /* Local Storage Handlers */
        YamlStorageHandler(plugin),
        // ...... TODO

        /* Remote Storage Handlers */
        // ...... TODO
    )

    var currentHandler: StorageHandler? = null

    fun load() {
        // make sure we're not already connected
        if (connected())
            throw IllegalStateException(
                """
                Unable to connect via StorageManager: Already connected via handler with ID '${currentHandler!!.id}'.
                """.trimIndent()
            )

        // figure out what storage handler ID they want to connect with
        val id: String? = plugin.settings
            .rootNode
            .node("storage", "implementation")
            .string

        if (id == null) {
            throw IllegalStateException(
                """
                Unable to connect via StorageManager: Storage implementation is not defined in the Settings config file.
                """.trimIndent()
            )
        }

        // fingers crossed, there is a handler available with that ID.
        try {
            currentHandler = availableHandlers.first { it.id.equals(id, ignoreCase = true) }
        } catch (ex: NoSuchElementException) {
            throw NoSuchElementException(
                """
                Unable to connect via StorageManager: There is no available storage handler with the ID '${id}'. Did you make a spelling mistake?
                Available storage handler IDs: ${
                    availableHandlers.joinToString(
                        separator = ", ",
                        prefix = "[",
                        postfix = "]"
                    ) { it.id }
                }
                """.trimMargin()
            )
        }

        // let's connect
        currentHandler!!.connect()
    }

    fun disconnect() {
        if (!connected()) {
            plugin.debugLog(STORAGE_MANAGER) {
                """
                Unable to disconnect via StorageManager: already disconnected.
                """.trimIndent()
            }
            return
        }

        currentHandler!!.disconnect()
    }

    @Suppress("BooleanMethodIsAlwaysInverted")
    fun connected(): Boolean {
        return currentHandler != null
    }

}