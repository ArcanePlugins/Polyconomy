package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage

import io.github.arcaneplugins.polyconomy.api.Economy
import java.util.*

abstract class StorageHandler(
    val id: String,
) : Economy {

    var connected: Boolean = false
        protected set

    abstract fun connect()

    abstract fun disconnect()

    abstract suspend fun playerCacheGetName(uuid: UUID): String?

    abstract suspend fun playerCacheSetName(uuid: UUID, name: String)

    abstract suspend fun playerCacheIsPlayer(uuid: UUID): Boolean

}