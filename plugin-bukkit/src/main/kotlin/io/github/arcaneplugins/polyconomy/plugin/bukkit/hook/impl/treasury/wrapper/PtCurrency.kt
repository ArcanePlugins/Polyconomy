package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper

import io.github.arcaneplugins.polyconomy.api.Economy
import kotlinx.coroutines.runBlocking
import me.lokka30.treasury.api.common.response.TreasuryException
import me.lokka30.treasury.api.economy.account.Account
import me.lokka30.treasury.api.economy.currency.Currency
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

class PtCurrency(
    val currency: io.github.arcaneplugins.polyconomy.api.currency.Currency
) : Currency {

    override fun getIdentifier(): String {
        return currency.name
    }

    override fun getSymbol(): String {
        return runBlocking {
            return@runBlocking currency.getSymbol()
        }
    }

    override fun getDecimal(locale: Locale?): Char {
        return runBlocking {
            return@runBlocking currency.getDecimal(
                locale = locale ?: Locale.getDefault()
            ).first()
        }
    }

    override fun getLocaleDecimalMap(): Map<Locale, Char> {
        return runBlocking {
            return@runBlocking currency.getLocaleDecimalMap()
                .mapValues { it.value.first() }
                .toMap()
        }
    }

    override fun getDisplayName(value: BigDecimal, locale: Locale?): String {
        return runBlocking {
            return@runBlocking currency.getDisplayName(
                plural = value.compareTo(BigDecimal.ONE) != 0,
                locale = locale ?: Locale.getDefault()
            )
        }
    }

    override fun getPrecision(): Int {
        return Economy.PRECISION
    }

    override fun isPrimary(): Boolean {
        return runBlocking {
            return@runBlocking currency.isPrimary()
        }
    }

    // Polyconomy ignores account parameter
    override fun getStartingBalance(account: Account): BigDecimal {
        return runBlocking {
            return@runBlocking currency.getStartingBalance()
        }
    }

    override fun getConversionRate(): BigDecimal {
        return runBlocking {
            return@runBlocking currency.getConversionRate()
        }
    }

    override fun parse(formattedAmount: String, locale: Locale?): CompletableFuture<BigDecimal> {
        return CompletableFuture.supplyAsync {
            throw TreasuryException("Polyconomy does not support the Currency#parse API in Treasury")
        }
    }

    override fun format(amount: BigDecimal, locale: Locale?): String {
        return runBlocking {
            return@runBlocking currency.format(amount, locale ?: Locale.getDefault())
        }
    }

    // Polyconomy ignores precision parameter
    override fun format(amount: BigDecimal, locale: Locale?, precision: Int): String {
        return format(amount, locale)
    }

}