package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.permission.PolyAccountPermission
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyStandardResponseError
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.PolyTransaction
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyTriState
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

class PolyPlayerAccount(
    val player: UUID
) : PolyAccount() {

    val allPermissions: Map<UUID, Map<PolyAccountPermission, PolyTriState>> = mapOf(
        Pair(
            player,
            PolyAccountPermission.allPermissions
        )
    )

    override fun name(): CompletableFuture<PolyResponse<String?>> {
        return StorageManager.currentHandler!!.retrieveName(this)
    }

    override fun retrieveBalance(currency: PolyCurrency): CompletableFuture<PolyResponse<BigDecimal>> {
        return StorageManager.currentHandler!!.retrieveBalance(this, currency)
    }

    override fun doTransaction(transaction: PolyTransaction): CompletableFuture<PolyResponse<BigDecimal>> {
        return StorageManager.currentHandler!!.doTransaction(this, transaction)
    }

    override fun delete(): CompletableFuture<PolyResponse<PolyTriState>> {
        return StorageManager.currentHandler!!.deleteAccount(this)
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<PolyResponse<Collection<String>>> {
        return StorageManager.currentHandler!!.retrieveHeldCurrencies(this)
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): CompletableFuture<PolyResponse<Collection<PolyTransaction>>> {
        return StorageManager.currentHandler!!.retrieveTransactionHistory(
            this,
            transactionCount,
            from,
            to
        )
    }

    override fun retrieveMemberIds(): CompletableFuture<PolyResponse<Collection<UUID>>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "retrieveMemberIds: ${name()}",
                result = Collections.singleton(player),
                error = null
            )
        )
    }

    override fun isMember(player: UUID): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "isMember: ${name()}",
                result = PolyTriState.fromBool(player == this.player),
                error = null
            )
        )
    }

    override fun setPermissions(
        player: UUID,
        permissionValue: PolyTriState,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "setPermissions: ${name()}",
                result = null,
                error = PolyStandardResponseError.PLAYER_ACCOUNT_PERMISSION_MODIFICATION_NOT_SUPPORTED
            )
        )
    }

    override fun retrievePermissions(player: UUID): CompletableFuture<PolyResponse<Map<PolyAccountPermission, PolyTriState>>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "retrievePermissions: ${name()}",
                result = let {
                    if(player == this.player) PolyAccountPermission.allPermissions else mapOf()
                },
                error = null
            )
        )
    }

    override fun retrievePermissionsMap(): CompletableFuture<PolyResponse<Map<UUID, Map<PolyAccountPermission, PolyTriState>>>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "retrievePermisionsMap: ${name()}",
                result = allPermissions,
                error = null
            )
        )
    }

    override fun hasPermissions(
        player: UUID,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "hasPermissions: ${name()}",
                result = PolyTriState.fromBool(player == this.player),
                error = null
            )
        )
    }
}