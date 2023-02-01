package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.permission.PolyAccountPermission
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.PolyTransaction
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyNamespacedKey
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyTriState
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

class PolyNonPlayerAccount(
    val id: PolyNamespacedKey
) : PolyAccount() {

    override fun name(): CompletableFuture<PolyResponse<String?>> {
        return StorageManager.currentHandler!!.retrieveNameAsync(this)
    }

    override fun retrieveBalance(
        currency: PolyCurrency
    ): CompletableFuture<PolyResponse<BigDecimal>> {
        return StorageManager.currentHandler!!.retrieveBalanceAsync(this, currency)
    }

    override fun doTransaction(
        transaction: PolyTransaction
    ): CompletableFuture<PolyResponse<BigDecimal>> {
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
        return StorageManager.currentHandler!!.retrieveMemberIdsAsync(this)
    }

    override fun isMember(
        player: UUID
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return StorageManager.currentHandler!!.isMemberAsync(this, player)
    }

    override fun setPermissions(
        player: UUID,
        permissionValue: PolyTriState,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return StorageManager.currentHandler!!.setPermissionsAsync(
            this,
            player,
            permissionValue,
            *permissions
        )
    }

    override fun retrievePermissions(
        player: UUID
    ): CompletableFuture<PolyResponse<Map<PolyAccountPermission, PolyTriState>>> {
        return StorageManager.currentHandler!!.retrievePermissionsAsync(this, player)
    }

    override fun retrievePermissionsMap(): CompletableFuture<PolyResponse<Map<UUID, Map<PolyAccountPermission, PolyTriState>>>> {
        return StorageManager.currentHandler!!.retrievePermissionsMapAsync(this)
    }

    override fun hasPermissions(
        player: UUID,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return StorageManager.currentHandler!!.hasPermissionsAsync(this, player, *permissions)
    }
}