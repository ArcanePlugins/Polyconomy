package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.PolyAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.initiator.PolyTransactionInitiator
import java.math.BigDecimal
import java.time.Instant

@Suppress("unused") //TODO use
class PolyTransaction(
    val account: PolyAccount,
    val currencyId: String,
    val initiator: PolyTransactionInitiator<out Any>,
    val timestamp: Instant?,
    val type: PolyTransactionType,
    val reason: String?,
    val amount: BigDecimal,
    val importance: PolyTransactionImportance,
)