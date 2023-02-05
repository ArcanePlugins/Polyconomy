package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.PolyNonPlayerAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.account.AccountPermission
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.CompletableFuture

class NonPlayerAccountImpl(
    val poly: PolyNonPlayerAccount
) : NonPlayerAccount {

    override fun getName(): Optional<String> {
        val polyResponse = poly.retrieveNameSync()

        if(!polyResponse.successful()) {
            polyResponse.error!!.throwEx()
        }

        return Optional.ofNullable(polyResponse.result)
    }

    override fun setName(name: String?): CompletableFuture<Response<TriState>> {
        return poly
            .renameAsync(name)
            .thenApply { response -> response.toTreasury() }
            .thenApply { response ->
                if(response.isSuccessful) {
                    return@thenApply Response.success(response.result!!.toTreasury())
                } else {
                    return@thenApply Response.failure(response.failureReason!!)
                }
            }
    }

    override fun retrieveBalance(currency: Currency): CompletableFuture<Response<BigDecimal>> {
        return poly
            .retrieveBalance(PolyCurrency.fromTreasury(currency))
            .thenApply {  }
    }

    override fun doTransaction(economyTransaction: EconomyTransaction): CompletableFuture<Response<BigDecimal>> {
        TODO("Not yet implemented")
    }

    override fun deleteAccount(): CompletableFuture<Response<TriState>> {
        TODO("Not yet implemented")
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<Response<MutableCollection<String>>> {
        TODO("Not yet implemented")
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal
    ): CompletableFuture<Response<MutableCollection<EconomyTransaction>>> {
        TODO("Not yet implemented")
    }

    override fun retrieveMemberIds(): CompletableFuture<Response<MutableCollection<UUID>>> {
        TODO("Not yet implemented")
    }

    override fun isMember(player: UUID): CompletableFuture<Response<TriState>> {
        TODO("Not yet implemented")
    }

    override fun setPermission(
        player: UUID,
        permissionValue: TriState,
        vararg permissions: AccountPermission
    ): CompletableFuture<Response<TriState>> {
        TODO("Not yet implemented")
    }

    override fun retrievePermissions(player: UUID): CompletableFuture<Response<MutableMap<AccountPermission, TriState>>> {
        TODO("Not yet implemented")
    }

    override fun retrievePermissionsMap(): CompletableFuture<Response<MutableMap<UUID, MutableSet<MutableMap.MutableEntry<AccountPermission, TriState>>>>> {
        TODO("Not yet implemented")
    }

    override fun hasPermission(
        player: UUID,
        vararg permissions: AccountPermission
    ): CompletableFuture<Response<TriState>> {
        TODO("Not yet implemented")
    }

    override fun getIdentifier(): NamespacedKey {
        TODO("Not yet implemented")
    }

}