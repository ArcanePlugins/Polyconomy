package io.github.arcaneplugins.polyconomy.api.account

import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.cause.Cause
import java.math.BigDecimal
import java.time.Instant

data class AccountTransaction(
    val amount: BigDecimal,
    val currency: Currency,
    val cause: Cause,
    val reason: String?,
    val importance: TransactionImportance,
    val type: TransactionType,
    val timestamp: Instant,
) {

    init {
        if (amount < BigDecimal.ZERO) {
            throw IllegalArgumentException("Transaction amount can't be less than zero")
        }
    }

    suspend fun resultingBalance(oldBalance: BigDecimal): BigDecimal {
        return when (type) {
            TransactionType.SET -> amount
            TransactionType.RESET -> currency.getStartingBalance()
            TransactionType.WITHDRAW -> oldBalance - amount
            TransactionType.DEPOSIT -> oldBalance + amount
        }
    }

}
