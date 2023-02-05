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

    override fun retrieveNameAsync(): CompletableFuture<PolyResponse<String?>> {
        return StorageManager.currentHandler!!.retrieveNameAsync(this)
    }

    override fun retrieveNameSync(): PolyResponse<String?> {
        return StorageManager.currentHandler!!.retrieveNameSync(this)
    }

    override fun renameAsync(name: String?): CompletableFuture<PolyResponse<PolyTriState>> {
        return StorageManager.currentHandler!!.renameAsync(this, name)
    }

    override fun renameSync(name: String?): PolyResponse<PolyTriState> {
        return StorageManager.currentHandler!!.renameSync(this, name)
    }

    override fun retrieveBalance(currency: PolyCurrency): CompletableFuture<PolyResponse<BigDecimal>> {
        return StorageManager.currentHandler!!.retrieveBalanceAsync(this, currency)
    }

    override fun doTransaction(transaction: PolyTransaction): CompletableFuture<PolyResponse<BigDecimal>> {
        return StorageManager.currentHandler!!.doTransactionAsync(this, transaction)
    }

    override fun delete(): CompletableFuture<PolyResponse<PolyTriState>> {
        return StorageManager.currentHandler!!.deleteAccountAsync(this)
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<PolyResponse<Collection<String>>> {
        return StorageManager.currentHandler!!.retrieveHeldCurrenciesAsync(this)
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): CompletableFuture<PolyResponse<Collection<PolyTransaction>>> {
        return StorageManager.currentHandler!!.retrieveTransactionHistoryAsync(
            this,
            transactionCount,
            from,
            to
        )
    }

    override fun retrieveMemberIds(): CompletableFuture<PolyResponse<Collection<UUID>>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "retrieveMemberIds: ${retrieveNameAsync()}",
                result = Collections.singleton(player),
                error = null
            )
        )
    }

    override fun isMember(player: UUID): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "isMember: ${retrieveNameAsync()}",
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
                name = "setPermissions: ${retrieveNameAsync()}",
                result = null,
                error = PolyStandardResponseError.PLAYER_ACCOUNT_PERMISSION_MODIFICATION_NOT_SUPPORTED
            )
        )
    }

    override fun retrievePermissions(player: UUID): CompletableFuture<PolyResponse<Map<PolyAccountPermission, PolyTriState>>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "retrievePermissions: ${retrieveNameAsync()}",
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
                name = "retrievePermisionsMap: ${retrieveNameAsync()}",
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
                name = "hasPermissions: ${retrieveNameAsync()}",
                result = PolyTriState.fromBool(player == this.player),
                error = null
            )
        )
    }
}