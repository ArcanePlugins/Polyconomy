package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.PolyNonPlayerAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.PolyPlayerAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.permission.PolyAccountPermission
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.PolyTransaction
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyTriState
import org.bukkit.NamespacedKey
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class StorageHandler(
    val id: String
) {

    var connected: Boolean = false
        protected set

    abstract fun connect()

    abstract fun disconnect()

    abstract fun hasPlayerAccount(
        player: UUID
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun hasNonPlayerAccount(
        id: NamespacedKey
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun retrieveName(
        account: PolyPlayerAccount
    ): CompletableFuture<PolyResponse<String?>>

    abstract fun retrieveName(
        account: PolyNonPlayerAccount
    ): CompletableFuture<PolyResponse<String?>>

    abstract fun rename(
        account: PolyPlayerAccount,
        name: String?
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun rename(
        account: PolyNonPlayerAccount,
        name: String?
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun deleteAccount(
        account: PolyPlayerAccount,
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun deleteAccount(
        account: PolyNonPlayerAccount,
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun retrieveBalance(
        account: PolyPlayerAccount,
        currency: PolyCurrency
    ): CompletableFuture<PolyResponse<BigDecimal>>

    abstract fun retrieveBalance(
        account: PolyNonPlayerAccount,
        currency: PolyCurrency
    ): CompletableFuture<PolyResponse<BigDecimal>>

    abstract fun doTransaction(
        account: PolyPlayerAccount,
        transaction: PolyTransaction
    ): CompletableFuture<PolyResponse<BigDecimal>>

    abstract fun doTransaction(
        account: PolyNonPlayerAccount,
        transaction: PolyTransaction
    ): CompletableFuture<PolyResponse<BigDecimal>>

    abstract fun retrieveHeldCurrencies(
        account: PolyPlayerAccount
    ): CompletableFuture<PolyResponse<Collection<String>>>

    abstract fun retrieveHeldCurrencies(
        account: PolyNonPlayerAccount
    ): CompletableFuture<PolyResponse<Collection<String>>>

    abstract fun retrieveTransactionHistory(
        account: PolyPlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): CompletableFuture<PolyResponse<Collection<PolyTransaction>>>

    abstract fun retrieveTransactionHistory(
        account: PolyNonPlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): CompletableFuture<PolyResponse<Collection<PolyTransaction>>>

    abstract fun retrieveMemberIds(
        account: PolyNonPlayerAccount
    ): CompletableFuture<PolyResponse<Collection<UUID>>>

    abstract fun isMember(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun setPermissions(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID,
        permissionValue: PolyTriState,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun retrievePermissions(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID
    ): CompletableFuture<PolyResponse<Map<PolyAccountPermission, PolyTriState>>>

    abstract fun retrievePermissionsMap(
        account: PolyNonPlayerAccount
    ): CompletableFuture<PolyResponse<Map<UUID, Map<PolyAccountPermission, PolyTriState>>>>

    abstract fun hasPermissions(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun findPlayerAccount(
        player: UUID
    ): CompletableFuture<PolyResponse<PolyPlayerAccount>>

    abstract fun findNonPlayerAccount(
        id: NamespacedKey
    ): CompletableFuture<PolyResponse<PolyNonPlayerAccount>>

}