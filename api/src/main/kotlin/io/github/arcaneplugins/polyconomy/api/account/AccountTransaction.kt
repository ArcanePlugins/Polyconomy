package io.github.arcaneplugins.polyconomy.api.account

import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.cause.Cause
import java.math.BigDecimal

class AccountTransaction(
    val by: Account?,
    val amount: BigDecimal,
    val currency: Currency,
    val cause: Cause,
    val reason: String?,
    val importance: TransactionImportance,
    val method: TransactionMethod,
) {
}