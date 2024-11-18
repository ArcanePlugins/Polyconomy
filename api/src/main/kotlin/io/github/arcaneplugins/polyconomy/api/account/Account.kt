package io.github.arcaneplugins.polyconomy.api.account

import io.github.arcaneplugins.polyconomy.api.currency.Currency
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*

abstract class Account(
    val name: String?,
) {

    abstract suspend fun getBalance(
        currency: Currency,
    ): BigDecimal

    abstract suspend fun makeTransaction(
        transaction: AccountTransaction,
    )

    abstract suspend fun deleteAccount()

    abstract suspend fun getHeldCurrencies(): Set<Currency>

    abstract suspend fun getTransactionHistory(
        maxCount: Int,
        dateFrom: Temporal,
        dateTo: Temporal,
    ): List<AccountTransaction>

    abstract suspend fun getMemberIds(): Set<UUID>

    abstract suspend fun isMember(
        player: UUID,
    ): Boolean

    abstract suspend fun setPermissions(
        player: UUID,
        perms: Map<AccountPermission, Boolean?>
    )

    abstract suspend fun getPermissions(
        player: UUID
    ): Map<AccountPermission, Boolean?>

    abstract suspend fun getPermissionsMap(): Map<UUID, Map<AccountPermission, Boolean?>>

    abstract suspend fun hasPermissions(
        player: UUID,
        vararg permissions: AccountPermission,
    )

}
