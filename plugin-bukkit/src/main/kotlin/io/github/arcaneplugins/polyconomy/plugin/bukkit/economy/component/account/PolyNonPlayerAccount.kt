package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.permission.PolyAccountPermission
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.PolyTransaction
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyTriState
import org.bukkit.NamespacedKey
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.CompletableFuture

class PolyNonPlayerAccount(
    val id: NamespacedKey
) : PolyAccount() {

    override fun retrieveBalance(currency: PolyCurrency): CompletableFuture<PolyResponse<BigDecimal>> {
        TODO("Not yet implemented")
    }

    override fun doTransaction(transaction: PolyTransaction): CompletableFuture<PolyResponse<BigDecimal>> {
        TODO("Not yet implemented")
    }

    override fun delete(): CompletableFuture<PolyResponse<PolyTriState>> {
        TODO("Not yet implemented")
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<PolyResponse<Collection<String>>> {
        TODO("Not yet implemented")
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal
    ): CompletableFuture<PolyResponse<Collection<PolyTransaction>>> {
        TODO("Not yet implemented")
    }

    override fun retrieveMemberIds(): CompletableFuture<PolyResponse<Collection<UUID>>> {
        TODO("Not yet implemented")
    }

    override fun isMember(player: UUID): CompletableFuture<PolyResponse<PolyTriState>> {
        TODO("Not yet implemented")
    }

    override fun setPermissions(
        player: UUID,
        permissionValue: PolyTriState,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        TODO("Not yet implemented")
    }

    override fun retrievePermissions(player: UUID): CompletableFuture<PolyResponse<Map<PolyAccountPermission, PolyTriState>>> {
        TODO("Not yet implemented")
    }

    override fun retrievePermissionsMap(): CompletableFuture<PolyResponse<Map<UUID, Map<PolyAccountPermission, PolyTriState>>>> {
        TODO("Not yet implemented")
    }

    override fun hasPermissions(
        player: UUID,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        TODO("Not yet implemented")
    }
}