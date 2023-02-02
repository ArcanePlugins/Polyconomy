package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.EconomyManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.PolyAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ConcurrentManager
import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.account.Account
import me.lokka30.treasury.api.economy.currency.Currency
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class PolyCurrency(
    val id: String,
    val symbol: String,
    val exchangeRate: BigDecimal,
    val displayNameSingular: String,
    val displayNamePlural: String,
    val decimalMap: Map<Locale, Char>
) {
    companion object {
        fun fromTreasury(
            treasuryCurrency: Currency
        ): PolyCurrency {
            return object : PolyCurrency(
                id = treasuryCurrency.identifier,
                decimalMap = treasuryCurrency.localeDecimalMap,
                displayNamePlural = treasuryCurrency.displayNamePlural,
                displayNameSingular = treasuryCurrency.displayNameSingular,
                exchangeRate = treasuryCurrency.conversionRate,
                symbol = treasuryCurrency.symbol
            ) {
                override fun startingBalance(
                    account: PolyAccount?
                ): BigDecimal {
                    return BigDecimal.ZERO
                }

                override fun format(
                    amount: BigDecimal,
                    locale: Locale?
                ): String {
                    return treasuryCurrency.format(amount, locale)
                }

                override fun parseSync(
                    formatted: String
                ): PolyResponse<BigDecimal> {
                    return PolyResponse.fromTreasury(treasuryCurrency.parse(formatted).join())
                }
            }
        }
    }

    abstract fun startingBalance(
        account: PolyAccount?
    ): BigDecimal

    abstract fun format(
        amount: BigDecimal,
        locale: Locale?
    ): String

    abstract fun parseSync(
        formatted: String
    ): PolyResponse<BigDecimal>

    //TODO Use
    @Suppress("unused")
    fun parseAsync(
        formatted: String
    ): CompletableFuture<PolyResponse<BigDecimal>> {
        return CompletableFuture.supplyAsync(
            {
                parseSync(formatted)
            },
            ConcurrentManager.execSvc
        )
    }

    fun dbId(): Int {
        return StorageManager.currentHandler!!.getOrGrantCurrencyDbIdSync(id)
    }

    fun dbIdStr(): String {
        return dbId().toString()
    }

    fun decimal(
        locale: Locale?
    ): Char {
        return if(locale == null || !decimalMap.containsKey(locale)) {
            decimalMap.getOrDefault(
                EconomyManager.primaryLocaleId,
                '.'
            )
        } else {
            decimalMap.getValue(locale)
        }
    }

    fun toTreasury(): Currency {
        return object : Currency {
            override fun getIdentifier(): String {
                return this@PolyCurrency.id
            }

            override fun getSymbol(): String {
                return this@PolyCurrency.symbol
            }

            override fun getDecimal(locale: Locale?): Char {
               return this@PolyCurrency.decimal(locale)
            }

            override fun getLocaleDecimalMap(): Map<Locale, Char> {
                return this@PolyCurrency.decimalMap
            }

            override fun getDisplayNameSingular(): String {
                return this@PolyCurrency.displayNameSingular
            }

            override fun getDisplayNamePlural(): String {
                return this@PolyCurrency.displayNamePlural
            }

            override fun getPrecision(): Int {
                return StorageManager.precision
            }

            override fun isPrimary(): Boolean {
                return this@PolyCurrency == EconomyManager.primaryCurrency
            }

            override fun getStartingBalance(account: Account): BigDecimal {
                return this@PolyCurrency.startingBalance(PolyAccount.fromTreasury(account))
            }

            override fun getConversionRate(): BigDecimal {
                return this@PolyCurrency.exchangeRate
            }

            override fun parse(
                formatted: String
            ): CompletableFuture<Response<BigDecimal>> {
                return CompletableFuture.supplyAsync(
                    { this@PolyCurrency.parseSync(formatted).toTreasury() },
                    ConcurrentManager.execSvc
                )
            }

            override fun format(
                amount: BigDecimal,
                localeId: Locale?
            ): String {
                return this@PolyCurrency.format(amount, localeId)
            }

            override fun format(amount: BigDecimal, locale: Locale?, precision: Int): String {
                // Polyconomy does not support custom precision. Redirect to the other method.
                return format(amount, locale)
            }

        }
    }
}