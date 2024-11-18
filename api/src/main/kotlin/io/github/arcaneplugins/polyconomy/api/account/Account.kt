package io.github.arcaneplugins.polyconomy.api.account

import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.cause.Cause
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*

interface Account {

    suspend fun getName(): String?

    suspend fun setName(newName: String?)

    suspend fun getBalance(
        currency: Currency,
    ): BigDecimal

    suspend fun resetBalance(
        currency: Currency,
        cause: Cause,
        importance: TransactionImportance,
        reason: String,
    ) {
        makeTransaction(
            AccountTransaction(
                amount = getBalance(currency),
                by = null,
                cause = cause,
                currency = currency,
                importance = importance,
                method = TransactionMethod.RESET,
                reason = reason,
            )
        )
    }

    suspend fun makeTransaction(
        transaction: AccountTransaction,
    )

    suspend fun deleteAccount()

    suspend fun getHeldCurrencies(): Collection<Currency>

    suspend fun getTransactionHistory(
        maxCount: Int,
        dateFrom: Temporal,
        dateTo: Temporal,
    ): List<AccountTransaction>

    suspend fun getMemberIds(): Collection<UUID>

    suspend fun isMember(
        player: UUID,
    ): Boolean

    suspend fun setPermissions(
        player: UUID,
        perms: Map<AccountPermission, Boolean?>,
    )

    suspend fun getPermissions(
        player: UUID,
    ): Map<AccountPermission, Boolean?>

    suspend fun getPermissionsMap(): Map<UUID, Map<AccountPermission, Boolean?>>

    suspend fun hasPermissions(
        player: UUID,
        vararg permissions: AccountPermission,
    ): Boolean

}
