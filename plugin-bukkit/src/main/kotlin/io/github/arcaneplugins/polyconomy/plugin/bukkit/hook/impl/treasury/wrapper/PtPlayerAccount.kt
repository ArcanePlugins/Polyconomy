package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper

import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryEconomyProvider
import kotlinx.coroutines.runBlocking
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.CompletableFuture

class PtPlayerAccount(
    val provider: TreasuryEconomyProvider,
    val polyObj: PlayerAccount
): me.lokka30.treasury.api.economy.account.PlayerAccount {

    override fun getName(): Optional<String> {
        return runBlocking {
            Optional.ofNullable(polyObj.getName())
        }
    }

    override fun retrieveBalance(currency: Currency): CompletableFuture<BigDecimal> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj.getBalance(
                    currency = provider.getPolyCurrency(currency)!!
                )
            }
        }
    }

    override fun doTransaction(economyTransaction: EconomyTransaction): CompletableFuture<BigDecimal> {
        TODO("Not yet implemented")
    }

    override fun deleteAccount(): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj.deleteAccount()
                true
            }
        }
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<Collection<String>> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj.getHeldCurrencies()
                    .map { it.name }
            }
        }
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal,
    ): CompletableFuture<Collection<EconomyTransaction>> {
        TODO("Not yet implemented")
    }

    override fun identifier(): UUID = polyObj.uuid

}