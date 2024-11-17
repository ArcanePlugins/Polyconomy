package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage

abstract class StorageHandler(
    val id: String,
) {

    var connected: Boolean = false
        protected set

    abstract fun connect()

    abstract fun disconnect()

}