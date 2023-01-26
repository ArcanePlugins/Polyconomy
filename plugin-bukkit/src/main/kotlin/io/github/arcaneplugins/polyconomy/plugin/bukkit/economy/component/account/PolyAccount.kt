package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.permission.PolyAccountPermission
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.PolyTransaction
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyTriState
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.CompletableFuture

sealed class PolyAccount(
    var name: String? = null
) {

    abstract fun retrieveBalance(currency: PolyCurrency): CompletableFuture<PolyResponse<BigDecimal>>

    abstract fun doTransaction(transaction: PolyTransaction): CompletableFuture<PolyResponse<BigDecimal>>

    abstract fun delete(): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun retrieveHeldCurrencies(): CompletableFuture<PolyResponse<Collection<String>>>

    abstract fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal
    ): CompletableFuture<PolyResponse<Collection<PolyTransaction>>>

    abstract fun retrieveMemberIds(): CompletableFuture<PolyResponse<Collection<UUID>>>

    abstract fun isMember(player: UUID): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun setPermissions(
        player: UUID,
        permissionValue: PolyTriState,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun retrievePermissions(
        player: UUID
    ): CompletableFuture<PolyResponse<Map<PolyAccountPermission, PolyTriState>>>

    abstract fun retrievePermissionsMap(): CompletableFuture<PolyResponse<Map<UUID, Set<Map.Entry<PolyAccountPermission, PolyTriState>>>>>

    abstract fun hasPermissions(
        player: UUID,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>>

}