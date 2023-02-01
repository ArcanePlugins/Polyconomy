package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.EconomyManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import me.lokka30.treasury.api.common.response.Response
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
    val decimal: Map<Locale, String>
) {
    companion object {
        fun fromTreasury(
            treasuryCurrency: Currency
        ): PolyCurrency {
            return object : PolyCurrency(
                id = treasuryCurrency.identifier,

                // TODO discussing adjusting the decimal getter with Ivan, see relevant method below
                decimal = mapOf(
                    Pair(
                        EconomyManager.primaryLocaleId,
                        "${treasuryCurrency.decimal}"
                    )
                ),

                displayNamePlural = treasuryCurrency.displayNamePlural,
                displayNameSingular = treasuryCurrency.displayNameSingular,
                exchangeRate = treasuryCurrency.conversionRate,
                symbol = treasuryCurrency.symbol
            ) {
                override fun startingBalance(
                    player: UUID?
                ): BigDecimal {
                    return treasuryCurrency.getStartingBalance(player)
                }

                override fun format(
                    amount: BigDecimal,
                    locale: Locale?
                ): String {
                    return treasuryCurrency.format(amount, locale)
                }
            }
        }
    }

    abstract fun startingBalance(
        player: UUID?
    ): BigDecimal

    abstract fun format(
        amount: BigDecimal,
        locale: Locale?
    ): String

    fun toTreasury(): Currency {
        return object : Currency {
            override fun getIdentifier(): String {
                return this@PolyCurrency.id
            }

            override fun getSymbol(): String {
                return this@PolyCurrency.symbol
            }

            /*
            TODO:
                Discussing the adjustment of this method with Ivan.
                - Make the return type a String.
                - Accept a Locale parameter, since the decimal character is not the same with all
                  locales.

                https://github.com/ArcanePlugins/Treasury/issues/246
             */
            override fun getDecimal(): Char {
                return this@PolyCurrency.decimal.getValue(EconomyManager.primaryLocaleId).first()
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

            override fun getConversionRate(): BigDecimal {
                return this@PolyCurrency.exchangeRate
            }

            /*
            TODO: Discussing the removal of this method from Treasury with Ivan
            https://github.com/ArcanePlugins/Treasury/issues/246
             */
            override fun supportsNegativeBalances(): Boolean {
                return SettingsCfg
                    .rootNode
                    .node("advanced", "signed-balances")
                    .getBoolean(true)
            }

            /*
            TODO: Discussing the removal of this method from Treasury with Ivan
            https://github.com/ArcanePlugins/Treasury/issues/246
             */
            override fun parse(
                formatted: String
            ): CompletableFuture<Response<BigDecimal>> {
                return CompletableFuture.completedFuture(
                    Response.failure {
                        """
                        Polyconomy does not support currency amount parsing, as it may be removed by Treasury in the near future.
                        """
                    }
                )
            }

            override fun getStartingBalance(
                playerID: UUID?
            ): BigDecimal {
                return this@PolyCurrency.startingBalance(playerID)
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

    fun dbId(): Int {
        return StorageManager.currentHandler!!.getOrGrantCurrencyDbIdSync(id)
    }

    fun dbIdStr(): String {
        return dbId().toString()
    }
}