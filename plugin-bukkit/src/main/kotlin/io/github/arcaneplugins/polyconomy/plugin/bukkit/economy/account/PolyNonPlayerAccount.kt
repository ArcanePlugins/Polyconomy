package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager.currentHandlerNotNull
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ExecutionManager.execSvc
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
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
    val id: NamespacedKey,
) : NonPlayerAccount {

    override fun getName(): Optional<String> {
        return currentHandlerNotNull().retrieveNameSync(this)
    }

    override fun setName(
        name: String?,
    )
            : CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .setNameSync(this, name)
            },
            execSvc
        )
    }

    override fun retrieveBalance(
        currency: Currency,
    ): CompletableFuture<BigDecimal> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .retrieveBalanceSync(this, currency)
            },
            execSvc
        )
    }

    override fun doTransaction(
        economyTransaction: EconomyTransaction,
    ): CompletableFuture<BigDecimal> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .doTransactionSync(this, economyTransaction)
            },
            execSvc
        )
    }

    override fun deleteAccount(): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .deleteAccountSync(this)
            },
            execSvc
        )
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<Collection<String>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .retrieveHeldCurrenciesSync(this)
            },
            execSvc
        )
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal,
    ): CompletableFuture<Collection<EconomyTransaction>> {
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

    override fun retrieveMemberIds(): CompletableFuture<Collection<UUID>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull().retrieveMemberIdsSync(this)
            },
            execSvc
        )
    }

    override fun isMember(player: UUID): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .isMemberSync(this, player)
            },
            execSvc
        )
    }

    override fun setPermissions(
        player: UUID,
        permissionValue: TriState,
        vararg permissions: AccountPermission,
    ): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .setPermissionsSync(this, player, permissionValue, *permissions)
            },
            execSvc
        )
    }

    override fun setPermissions(
        player: UUID,
        permissionsMap: Map<AccountPermission, TriState?>,
    ): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync(
            {
                @Suppress("UNCHECKED_CAST")
                return@supplyAsync currentHandlerNotNull()
                    .setPermissionsSync(
                        this,
                        player,
                        permissionsMap.withDefault { TriState.UNSPECIFIED } as Map<AccountPermission, TriState>
                    )
            },
            execSvc
        )
    }

    override fun retrievePermissions(
        player: UUID,
    ): CompletableFuture<Map<AccountPermission, TriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .retrievePermissionsSync(this, player)
            },
            execSvc
        )
    }

    override fun retrievePermissionsMap(): CompletableFuture<Map<UUID, Map<AccountPermission, TriState>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .retrievePermissionsMapSync(this)
            },
            execSvc
        )
    }

    override fun hasPermissions(
        player: UUID,
        vararg permissions: AccountPermission,
    ): CompletableFuture<TriState> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .hasPermissionsSync(this, player, *permissions)
            },
            execSvc
        )
    }

    override fun identifier(): NamespacedKey {
        return id
    }

}