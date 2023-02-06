package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager.currentHandlerNotNull
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ConcurrentManager.execSvc
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionInitiator
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.CompletableFuture

class PolyPlayerAccount(
    val player: UUID
) : PlayerAccount {

    val asTransactionInitiator = EconomyTransactionInitiator.createInitiator(
        EconomyTransactionInitiator.Type.PLAYER,
        player
    )

    override fun getName(): Optional<String> {
        val response = currentHandlerNotNull()
            .retrieveNameSync(this)

        if(response.isSuccessful) {
            return response.result!!
        } else {
            throw RuntimeException(response.failureReason!!.description)
        }
    }

    override fun retrieveBalance(currency: Currency): CompletableFuture<Response<BigDecimal>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .retrieveBalanceSync(this, currency)
            },
            execSvc
        )
    }

    override fun doTransaction(economyTransaction: EconomyTransaction): CompletableFuture<Response<BigDecimal>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .doTransactionSync(this, economyTransaction)
            },
            execSvc
        )
    }

    override fun deleteAccount(): CompletableFuture<Response<TriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync currentHandlerNotNull()
                    .deleteAccountSync(this)
            },
            execSvc
        )
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<Response<Collection<String>>> {
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

    override fun getUniqueId(): UUID {
        return player
    }

    override fun getAsTransactionInitiator(): EconomyTransactionInitiator<UUID> {
        return asTransactionInitiator
    }

}