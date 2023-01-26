package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrencyConversion
import java.util.*

object EconomyManager {

    var primaryCurrency: PolyCurrency? = null
        private set

    var primaryLocale: Locale? = null
        private set

    val currencies: LinkedHashSet<PolyCurrency> = LinkedHashSet()

    val conversions: LinkedHashSet<PolyCurrencyConversion> = LinkedHashSet()

    fun load() {
        loadCurrencies()
        loadPrimaryCurrency()
        loadConversions()
    }

    private fun loadCurrencies() {
        currencies.clear()
        TODO("Not implemented")
    }

    private fun loadPrimaryCurrency() {
        TODO("Not implemented")
    }

    private fun loadConversions() {
        conversions.clear()
        TODO("Not implemented")
    }

}