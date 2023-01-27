package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction

import java.util.*

abstract class PolyTransactionInitiator<T>(
    val data: T,
    val type: Type
) {

    companion object {
        object ServerInitiator : PolyTransactionInitiator<String>(
            data = "Server",
            type = Type.SERVER
        )

        class PlayerInitiator(
            player: UUID
        ) : PolyTransactionInitiator<UUID>(
            data = player,
            type = Type.PLAYER
        )

        class PluginInitiator(
            plugin: String
        ) : PolyTransactionInitiator<String>(
            data = plugin,
            type = Type.PLUGIN
        )
    }

    enum class Type {
        SERVER,
        PLAYER,
        PLUGIN
    }
}