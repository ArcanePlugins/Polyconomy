package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.EconomyManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.currency.Currency
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.CompletableFuture

class PolyCurrency(
    val id: String,
    val dbId: Int,
    val enabled: Boolean,
    val startingBalance: BigDecimal,
    val symbol: String,
    val locales: MutableList<CurrencyLocale>
) {
    companion object {
        fun fromTreasury(treasuryCurrency: Currency): PolyCurrency {
            TODO("Not yet implemented; stoppingIdeFromComplaining=${treasuryCurrency}")
        }
    }

    class CurrencyLocale(
        val id: Locale,
        val displayName: String,
        val wordFormatSingular: String?,
        val wordFormatPlural: String?,
        val decimalChar: String,
        val amountFormat: DecimalFormat,
        val presentationFormat: String
    ) {
        fun format(
            amount: BigDecimal
        ): String {
            TODO("Not implemented; amountToStopIdeFromComplaining=${amount}")
        }
    }

    fun getFavourableLocale(): CurrencyLocale {
        val primaryLocale: CurrencyLocale? = locales.firstOrNull { locale ->
            locale.id == EconomyManager.primaryLocaleId
        }

        if(primaryLocale != null)
            return primaryLocale

        return locales.first()
    }

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

                https://discord.com/channels/752310043214479462/921225503531204658/1069559814939742258
             */
            override fun getDecimal(): Char {
                return this@PolyCurrency.getFavourableLocale().decimalChar[0]
            }

            override fun getDisplayNameSingular(): String {
                return this@PolyCurrency.getFavourableLocale().wordFormatSingular!!
            }

            override fun getDisplayNamePlural(): String {
                return this@PolyCurrency.getFavourableLocale().wordFormatPlural!!
            }

            override fun getPrecision(): Int {
                return StorageManager.precision
            }

            override fun isPrimary(): Boolean {
                return this@PolyCurrency == EconomyManager.primaryCurrency
            }

            override fun getConversionRate(): BigDecimal {
                TODO("Overhaul the conversion rates in polyconomy to match treasury's universal conversion rate system.")
            }

            /*
            TODO: Discussing the removal of this method from Treasury with Ivan
            https://discord.com/channels/752310043214479462/921225503531204658/1069561660462874664
             */
            override fun supportsNegativeBalances(): Boolean {
                return SettingsCfg
                    .rootNode
                    .node("advanced", "signed-balances")
                    .getBoolean(true)
            }

            override fun parse(
                formatted: String
            ): CompletableFuture<Response<BigDecimal>> {
                TODO("Discussing with Ivan: https://discord.com/channels/752310043214479462/921225503531204658/1069566468301271080")
            }

            override fun getStartingBalance(
                playerID: UUID?
            ): BigDecimal {
                return this@PolyCurrency.startingBalance
            }

            override fun format(
                amount: BigDecimal,
                localeId: Locale?
            ): String {
                val polyLocale: CurrencyLocale = if(
                    localeId == null ||
                    locales.none { locale -> locale.id == localeId }
                ) {
                    getFavourableLocale()
                } else {
                    locales.first { locale -> locale.id == localeId }
                }

                return polyLocale.format(amount)
            }

            override fun format(amount: BigDecimal, locale: Locale?, precision: Int): String {
                TODO("Not yet implemented")
            }

        }
    }
}