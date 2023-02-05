package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager.currentHandlerNotNull
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ConcurrentManager.execSvc
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.account.AccountPermission
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.CompletableFuture

class PolyNonPlayerAccount(
    val id: NamespacedKey
) : NonPlayerAccount {

    override fun getName(): Optional<String> {
        val response = currentHandlerNotNull()
            .retrieveNameSync(this)

        if(response.isSuccessful) {
            return response.result!!
        } else {
            throw RuntimeException(response.failureReason!!.description)
        }
    }

    override fun setName(name: String?): CompletableFuture<Response<TriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull().setNameSync(this, name)
            },
            execSvc
        )
    }

    override fun retrieveBalance(currency: Currency): CompletableFuture<Response<BigDecimal>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull().retrieveBalanceSync(this, currency)
            },
            execSvc
        )
    }

    override fun doTransaction(economyTransaction: EconomyTransaction): CompletableFuture<Response<BigDecimal>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull().doTransactionSync(this, economyTransaction)
            },
            execSvc
        )
    }

    override fun deleteAccount(): CompletableFuture<Response<TriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull().deleteAccountSync(this)
            },
            execSvc
        )
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<Response<Collection<String>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull().retrieveHeldCurrenciesSync(this)
            },
            execSvc
        )
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal
    ): CompletableFuture<Response<Collection<EconomyTransaction>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .retrieveTransactionHistorySync(
                        this,
                        transactionCount,
                        Instant.from(from),
                        Instant.from(to)
                    )
            },
            execSvc
        )
    }

    override fun retrieveMemberIds(): CompletableFuture<Response<Collection<UUID>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull().retrieveMemberIdsSync(this)
            },
            execSvc
        )
    }

    override fun isMember(player: UUID): CompletableFuture<Response<TriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .isMemberSync(this, player)
            },
            execSvc
        )
    }

    override fun setPermission(
        player: UUID,
        permissionValue: TriState,
        vararg permissions: AccountPermission
    ): CompletableFuture<Response<TriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .setPermissionsSync(this, player, permissionValue, *permissions)
            },
            execSvc
        )
    }

    override fun retrievePermissions(
        player: UUID
    ): CompletableFuture<Response<Map<AccountPermission, TriState>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .retrievePermissionsSync(this, player)
            },
            execSvc
        )
    }

    override fun retrievePermissionsMap(): CompletableFuture<Response<Map<UUID, Set<Map.Entry<AccountPermission, TriState>>>>> {
        return CompletableFuture.supplyAsync(
            {
                val response = currentHandlerNotNull()
                    .retrievePermissionsMapSync(this)

                if(!response.isSuccessful) {
                    return@supplyAsync Response.failure { response.failureReason!!.description }
                }

                val uuidMap = mutableMapOf<UUID, Set<Map.Entry<AccountPermission, TriState>>>()

                response.result!!.forEach { (uuid, permMap) ->
                    uuidMap[uuid] = permMap.entries
                }

                return@supplyAsync Response.success(uuidMap)
            },
            execSvc
        )
    }

    override fun hasPermission(
        player: UUID,
        vararg permissions: AccountPermission
    ): CompletableFuture<Response<TriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .hasPermissionsSync(this, player, *permissions)
            },
            execSvc
        )
    }

    override fun getIdentifier(): NamespacedKey {
        return id
    }

}