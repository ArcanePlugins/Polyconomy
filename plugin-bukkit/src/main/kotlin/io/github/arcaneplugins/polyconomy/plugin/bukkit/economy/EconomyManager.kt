package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.ECONOMY_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyConfiguredCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

object EconomyManager {

    lateinit var primaryCurrency: PolyCurrency
        private set

    lateinit var primaryLocaleId: Locale
        private set

    val currencies: MutableList<PolyCurrency> = mutableListOf()

    fun load() {
        loadPrimaryLocale()
        loadCurrencies()
        loadPrimaryCurrency()
    }

    private fun loadCurrencies() {
        currencies.clear()
        SettingsCfg
            .rootNode
            .node("currencies")
            .childrenList()
            .filter { currencyNode -> currencyNode.node("enabled").getBoolean(true) }
            .forEach { currencyNode ->
                Log.d(ECONOMY_MANAGER) { "Parsing currency node @ ${currencyNode.path()}" }

                currencies.add(
                    PolyConfiguredCurrency(
                        id = currencyNode
                            .node("currency")
                            .string!!,

                        startingBalance = BigDecimal(
                            currencyNode
                                .node("starting-balance")
                                .double
                        ),

                        symbol = currencyNode.node("symbol").string!!,

                        exchangeRate = BigDecimal(
                            currencyNode.node("exchange-rate").getDouble(0.0)
                        ),

                        amountFormat = DecimalFormat(
                            currencyNode
                                .node("amount-format")
                                .getString("#,##0.00")
                        ),

                        decimal = let {
                            val map: MutableMap<Locale, String> = mutableMapOf()

                            currencyNode
                                .node("decimal")
                                .childrenList()
                                .forEach { localeDecimalNode ->
                                    val locale = Locale(
                                        localeDecimalNode
                                            .node("locale")
                                            .string!!
                                    )

                                    val character: String = localeDecimalNode
                                        .node("character")
                                        .string!!

                                    Log.d(ECONOMY_MANAGER) {
                                        """
                                        localeDecimalNode parsed:   { Locale: '${locale}'; Character: '${character}' }
                                        """.trimIndent()
                                    }

                                    map[locale] = character
                                }

                            if(!map.containsKey(primaryLocaleId))
                                map[primaryLocaleId] = "."

                            return@let map
                        },

                        displayNameSingular = currencyNode
                            .node("display-name-singular")
                            .string!!,

                        displayNamePlural = currencyNode
                            .node("display-name-plural")
                            .string!!,

                        presentationFormat = currencyNode
                            .node("presentation-format")
                            .getString("%amount% %display-name%")
                    )
                )
            }
    }

    private fun loadPrimaryCurrency() {
        primaryCurrency = findCurrencyByIdNonNull(
            SettingsCfg
                .rootNode
                .node("primary-currency")
                .string!!
        )
    }

    private fun loadPrimaryLocale() {
        primaryLocaleId = Locale(
            SettingsCfg
                .rootNode
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

}