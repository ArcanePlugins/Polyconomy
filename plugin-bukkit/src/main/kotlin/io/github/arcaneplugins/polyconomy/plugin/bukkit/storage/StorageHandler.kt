package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage

import io.github.arcaneplugins.polyconomy.api.Economy

abstract class StorageHandler(
    val id: String,
) : Economy {

    var connected: Boolean = false
        protected set

    abstract fun connect()

    abstract fun disconnect()

}