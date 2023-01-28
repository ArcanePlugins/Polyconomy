package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.initiator

object PolyServerTransactionInitiator : PolyTransactionInitiator<String>(
    data = "Server",
    type = PolyTransactionInitiator.Companion.Type.SERVER
)