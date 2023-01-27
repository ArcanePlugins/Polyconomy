package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.permission.PolyAccountPermission
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyStandardResponseError
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.PolyTransaction
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyTriState
import java.math.BigDecimal
import java.time.temporal.Temporal
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
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "retrieveMemberIds: ${name}",
                result = Collections.singleton(player),
                error = null
            )
        )
    }

    override fun isMember(player: UUID): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "isMember: ${name}",
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
                name = "setPermissions: ${name}",
                result = null,
                error = PolyStandardResponseError.PLAYER_ACCOUNT_PERMISSION_MODIFICATION_NOT_SUPPORTED
            )
        )
    }

    override fun retrievePermissions(player: UUID): CompletableFuture<PolyResponse<Map<PolyAccountPermission, PolyTriState>>> {
        return CompletableFuture.completedFuture(
            PolyResponse(
                name = "retrievePermissions: ${name}",
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
                name = "retrievePermisionsMap: ${name}",
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
                name = "hasPermissions: ${name}",
                result = PolyTriState.fromBool(player == this.player),
                error = null
            )
        )
    }
}