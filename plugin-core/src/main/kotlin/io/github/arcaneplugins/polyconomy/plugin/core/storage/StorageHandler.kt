package io.github.arcaneplugins.polyconomy.plugin.core.storage

import io.github.arcaneplugins.polyconomy.api.Economy
import java.util.*

abstract class StorageHandler(
    val id: String,
    val manager: StorageManager,
) : Economy {

    var connected = false
        protected set

    abstract fun startup()

    abstract fun shutdown()

    abstract suspend fun playerCacheGetName(uuid: UUID): String?

    abstract suspend fun playerCacheSetName(uuid: UUID, name: String)

    abstract suspend fun playerCacheIsPlayer(uuid: UUID): Boolean

}