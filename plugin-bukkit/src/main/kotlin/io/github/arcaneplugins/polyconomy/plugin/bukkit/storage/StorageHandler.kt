package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage

import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.economy.account.AccountPermission
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import java.math.BigDecimal
import java.time.Instant
import java.util.*

abstract class StorageHandler(
    val id: String,
) {

    protected var connected: Boolean = false

    abstract fun connect()

    abstract fun disconnect()

    abstract fun hasPlayerAccountSync(
        player: UUID,
    ): Boolean

    abstract fun hasNonPlayerAccountSync(
        id: NamespacedKey,
    ): Boolean

    abstract fun retrieveNameSync(
        account: PlayerAccount,
    ): Optional<String>

    abstract fun retrieveNameSync(
        account: NonPlayerAccount,
    ): Optional<String>

    abstract fun setNameSync(
        account: PlayerAccount,
        name: String?,
    ): Boolean

    abstract fun setNameSync(
        account: NonPlayerAccount,
        name: String?,
    ): Boolean

    abstract fun deleteAccountSync(
        account: PlayerAccount,
    ): Boolean

    abstract fun deleteAccountSync(
        account: NonPlayerAccount,
    ): Boolean

    abstract fun retrieveBalanceSync(
        account: PlayerAccount,
        currency: Currency,
    ): BigDecimal

    abstract fun retrieveBalanceSync(
        account: NonPlayerAccount,
        currency: Currency,
    ): BigDecimal

    abstract fun doTransactionSync(
        account: PlayerAccount,
        transaction: EconomyTransaction,
    ): BigDecimal

    abstract fun doTransactionSync(
        account: NonPlayerAccount,
        transaction: EconomyTransaction,
    ): BigDecimal

    abstract fun retrieveHeldCurrenciesSync(
        account: PlayerAccount,
    ): Collection<String>

    abstract fun retrieveHeldCurrenciesSync(
        account: NonPlayerAccount,
    ): Collection<String>

    abstract fun retrieveTransactionHistorySync(
        account: PlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant,
    ): Collection<EconomyTransaction>

    abstract fun retrieveTransactionHistorySync(
        account: NonPlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant,
    ): Collection<EconomyTransaction>

    abstract fun retrieveMemberIdsSync(
        account: NonPlayerAccount,
    ): Collection<UUID>

    abstract fun isMemberSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
    ): Boolean

    abstract fun setPermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
        permissionValue: TriState,
        vararg permissions: AccountPermission,
    ): Boolean

    abstract fun setPermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
        permissionsMap: Map<AccountPermission, TriState>,
    ): Boolean


    abstract fun retrievePermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
    ): Map<AccountPermission, TriState>

    abstract fun retrievePermissionsMapSync(
        account: NonPlayerAccount,
    ): Map<UUID, Map<AccountPermission, TriState>>

    abstract fun hasPermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
        vararg permissions: AccountPermission,
    ): TriState

    abstract fun retrievePlayerAccountIdsSync(): Collection<UUID>

    abstract fun retrieveNonPlayerAccountIdsSync(): Collection<NamespacedKey>

}