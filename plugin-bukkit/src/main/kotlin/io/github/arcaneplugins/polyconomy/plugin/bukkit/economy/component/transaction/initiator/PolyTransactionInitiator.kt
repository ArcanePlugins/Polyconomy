package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.initiator

import java.util.*

abstract class PolyTransactionInitiator<T>(
    val data: T,
    val type: Type
) {

    companion object {
        @Suppress("unused") //TODO use
        class PlayerInitiator(
            player: UUID
        ) : PolyTransactionInitiator<UUID>(
            data = player,
            type = Type.PLAYER
        )

        @Suppress("unused") //TODO use
        class PluginInitiator(
            plugin: String
        ) : PolyTransactionInitiator<String>(
            data = plugin,
            type = Type.PLUGIN
        )

        enum class Type {
            SERVER,
            PLAYER,
            PLUGIN
        }
    }
}