package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.ECONOMY_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrencyConversion
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

object EconomyManager {

    var primaryCurrency: PolyCurrency? = null
        private set

    var primaryLocale: Locale? = null
        private set

    val currencies: MutableList<PolyCurrency> = mutableListOf()

    val conversions: MutableList<PolyCurrencyConversion> = mutableListOf()

    fun load() {
        loadPrimaryLocale()
        loadCurrencies()
        loadPrimaryCurrency()
        loadConversions()
    }

    private fun loadCurrencies() {
        currencies.clear()
        SettingsCfg
            .rootNode!!
            .node("currencies")
            .childrenList()
            .forEach { currencyNode ->
                currencies.add(
                    PolyCurrency(
                        id = currencyNode
                            .node("currency")
                            .string!!,

                        dbId = currencyNode
                            .node("db-id")
                            .int,

                        enabled = currencyNode
                            .node("enabled")
                            .boolean,

                        startingBalance = BigDecimal(
                            currencyNode
                                .node("starting-balance")
                                .double
                        ),

                        locales = let {
                            val locales: MutableList<PolyCurrency.CurrencyLocale> = mutableListOf()

                            currencyNode
                                .node("locale")
                                .childrenList()
                                .forEach { localeNode ->
                                    locales.add(
                                        PolyCurrency.CurrencyLocale(
                                            id = Locale(
                                                localeNode
                                                    .node("id")
                                                    .string!!
                                            ),

                                            displayName = localeNode
                                                .node("display-name")
                                                .string!!,

                                            decimalChar = localeNode
                                                .node("decimal-char")
                                                .getString("."),

                                            amountFormat = DecimalFormat(
                                                localeNode
                                                    .node("amount-format")
                                                    .getString("#,##0.00")
                                            ),

                                            presentationFormat = localeNode
                                                .node("presentation-format")
                                                .getString("%symbol%%amount-format%"),

                                            wordFormatSingular = localeNode
                                                .node("word-format", "singular")
                                                .string,

                                            wordFormatPlural = localeNode
                                                .node("word-format", "plural")
                                                .string
                                        )
                                    )
                                }

                            locales // returned
                        },
                        symbol = currencyNode.node("symbol").string!!
                    )
                )
            }
    }

    private fun loadPrimaryCurrency() {
        primaryCurrency = findCurrencyByIdNonNull(
            SettingsCfg
                .rootNode!!
                .node("primary-currency")
                .string!!
        )

        if(!primaryCurrency!!.enabled) {
            throw IllegalStateException(
                """
                The primary currency you have configured (currently with ID '${primaryCurrency!!.id}') must be enabled, but you have disabled it.
                """.trimIndent()
            )
        }
    }

    private fun loadConversions() {
        conversions.clear()

        SettingsCfg
            .rootNode!!
            .node("conversions")
            .childrenList()
            .forEach { conversionNode ->
                conversions.add(
                    PolyCurrencyConversion(
                        from = findCurrencyByIdNonNull(
                            conversionNode
                                .node("from")
                                .string!!
                        ),
                        to = findCurrencyByIdNonNull(
                            conversionNode
                                .node("to")
                                .string!!
                        ),
                        rate = BigDecimal(
                            conversionNode
                                .node("rate")
                                .double
                        )
                    )
                )
            }
    }

    private fun loadPrimaryLocale() {
        primaryLocale = Locale(
            SettingsCfg
                .rootNode!!
                .node("primary-locale")
                .getString("en_US")
        )
    }

    fun findCurrencyById(id: String): PolyCurrency? {
        return currencies.firstOrNull { it.id.equals(id, true) }
    }

    fun findCurrencyByIdNonNull(id: String): PolyCurrency {
        val currency: PolyCurrency? = findCurrencyById(id)

        if(currency != null)
            return currency

        throw IllegalArgumentException(
            """
Unable to retrieve currency by ID '${id}', as there is no such currency with that ID.
Check for any spelling mistakes in your Settings config.
            """
        )
    }

    @Suppress("unused")
    private fun testDebugPrintout() {
        Log.d(ECONOMY_MANAGER) {
            val out: StringBuilder = StringBuilder(
                """
Primary Currency: '${primaryCurrency!!.id}'.
Primary Locale: displayName: '${primaryLocale!!.displayName}'; toString: '${primaryLocale!!}'.

Currencies:"""
            )

            currencies.forEach { currency ->
                out.append(
                    """
  •-• ID: ${currency.id}
    • Database ID: ${currency.dbId}
    • Enabled: ${currency.enabled}
    • Starting Balance: ${currency.startingBalance}
    • Symbol: ${currency.symbol}
    • Locales:"""
                )

                currency.locales.forEach { locale ->
                    out.append(
                        """
        •-• ID: ${locale.id}
          • Display Name: ${locale.displayName}
          • Word Format Singular: ${locale.wordFormatSingular}
          • Word Format Plural: ${locale.wordFormatPlural}
          • Decimal Char: ${locale.decimalChar}
          • Amount Format: ${locale.amountFormat}
          • Presentation Format: ${locale.presentationFormat}"""
                    )
                }
            }

            out.append("\nConversions:")

            conversions.forEach { conversion ->
                out.append(
                    """
  •-• From: ${conversion.from.id}
    • To: ${conversion.to.id}
    • Rate: ${conversion.rate.toDouble()}x"""
                )
            }

            out.toString() // returned
        }
    }

}