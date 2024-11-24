package io.github.arcaneplugins.polyconomy.api.account

import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.cause.Cause
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.Temporal
import java.util.*

interface Account {

    suspend fun getName(): String?

    suspend fun setName(newName: String?)

    suspend fun getBalance(
        currency: Currency,
    ): BigDecimal

    suspend fun has(
        amount: BigDecimal,
        currency: Currency,
    ): Boolean {
        return getBalance(currency) >= amount
    }

    suspend fun withdraw(
        amount: BigDecimal,
        currency: Currency,
        cause: Cause,
        importance: TransactionImportance,
        reason: String?,
    ) {
        makeTransaction(
            AccountTransaction(
                amount = amount,
                currency = currency,
                cause = cause,
                importance = importance,
                reason = reason,
                timestamp = Instant.now(),
                type = TransactionType.WITHDRAW,
            )
        )
    }

    suspend fun deposit(
        amount: BigDecimal,
        currency: Currency,
        cause: Cause,
        importance: TransactionImportance,
        reason: String?,
    ) {
        makeTransaction(
            AccountTransaction(
                amount = amount,
                currency = currency,
                cause = cause,
                importance = importance,
                reason = reason,
                timestamp = Instant.now(),
                type = TransactionType.DEPOSIT,
            )
        )
    }

    suspend fun resetBalance(
        currency: Currency,
        cause: Cause,
        importance: TransactionImportance,
        reason: String,
    ) {
        makeTransaction(
            AccountTransaction(
                amount = (getBalance(currency) - currency.getStartingBalance()).abs(),
                cause = cause,
                currency = currency,
                importance = importance,
                type = TransactionType.RESET,
                reason = reason,
                timestamp = Instant.now()
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
        permissions: Collection<AccountPermission>,
    ): Boolean

    suspend fun addMember(
        player: UUID,
    )

    suspend fun removeMember(
        player: UUID,
    )

}
