package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager.currentHandlerNotNull
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ExecutionManager.execSvc
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.CompletableFuture

class PolyPlayerAccount(
    val player: UUID
) : PlayerAccount {

    override fun getName(): Optional<String> {
        return currentHandlerNotNull().retrieveNameSync(this)
    }

    override fun retrieveBalance(
        currency: Currency
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
        economyTransaction: EconomyTransaction
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
        to: Temporal
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

    override fun identifier(): UUID {
        return player
    }

}