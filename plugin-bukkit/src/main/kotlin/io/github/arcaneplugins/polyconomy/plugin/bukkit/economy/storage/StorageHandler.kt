package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage

import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.account.AccountPermission
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import java.math.BigDecimal
import java.time.Instant
import java.util.*

abstract class StorageHandler(
    val id: String
) {

    protected var connected: Boolean = false

    abstract fun connect()

    abstract fun disconnect()

    abstract fun hasPlayerAccountSync(
        player: UUID
    ): Response<TriState>

    abstract fun hasNonPlayerAccountSync(
        id: NamespacedKey
    ): Response<TriState>

    abstract fun retrieveNameSync(
        account: PlayerAccount
    ): Response<Optional<String>>

    abstract fun retrieveNameSync(
        account: NonPlayerAccount
    ): Response<Optional<String>>

    abstract fun setNameSync(
        account: PlayerAccount,
        name: String?
    ): Response<TriState>

    abstract fun setNameSync(
        account: NonPlayerAccount,
        name: String?
    ): Response<TriState>

    abstract fun deleteAccountSync(
        account: PlayerAccount,
    ): Response<TriState>

    abstract fun deleteAccountSync(
        account: NonPlayerAccount,
    ): Response<TriState>

    abstract fun retrieveBalanceSync(
        account: PlayerAccount,
        currency: Currency
    ): Response<BigDecimal>

    abstract fun retrieveBalanceSync(
        account: NonPlayerAccount,
        currency: Currency
    ): Response<BigDecimal>

    abstract fun doTransactionSync(
        account: PlayerAccount,
        transaction: EconomyTransaction
    ): Response<BigDecimal>

    abstract fun doTransactionSync(
        account: NonPlayerAccount,
        transaction: EconomyTransaction
    ): Response<BigDecimal>

    abstract fun retrieveHeldCurrenciesSync(
        account: PlayerAccount
    ): Response<Collection<String>>

    abstract fun retrieveHeldCurrenciesSync(
        account: NonPlayerAccount
    ): Response<Collection<String>>

    abstract fun retrieveTransactionHistorySync(
        account: PlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): Response<Collection<EconomyTransaction>>

    abstract fun retrieveTransactionHistorySync(
        account: NonPlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): Response<Collection<EconomyTransaction>>

    abstract fun retrieveMemberIdsSync(
        account: NonPlayerAccount
    ): Response<Collection<UUID>>

    abstract fun isMemberSync(
        account: NonPlayerAccount,
        memberPlayer: UUID
    ): Response<TriState>

    abstract fun setPermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
        permissionValue: TriState,
        vararg permissions: AccountPermission
    ): Response<TriState>


    abstract fun retrievePermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID
    ): Response<Map<AccountPermission, TriState>>

    abstract fun retrievePermissionsMapSync(
        account: NonPlayerAccount
    ): Response<Map<UUID, Map<AccountPermission, TriState>>>

    abstract fun hasPermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
        vararg permissions: AccountPermission
    ): Response<TriState>

    abstract fun retrievePlayerAccountIdsSync(): Response<Collection<UUID>>

    abstract fun retrieveNonPlayerAccountIdsSync(): Response<Collection<NamespacedKey>>

}