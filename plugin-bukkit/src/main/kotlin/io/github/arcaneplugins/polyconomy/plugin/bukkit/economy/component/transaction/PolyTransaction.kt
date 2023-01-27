package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction

import java.math.BigDecimal
import java.time.Instant

class PolyTransaction(
    val id: Int,
    val currencyId: String,
    val initiator: PolyTransactionInitiator<Any>,
    val timestamp: Instant?,
    val transactionType: PolyTransactionType,
    val reason: String?,
    val amount: BigDecimal,
    val importance: PolyTransactionImportance,
)