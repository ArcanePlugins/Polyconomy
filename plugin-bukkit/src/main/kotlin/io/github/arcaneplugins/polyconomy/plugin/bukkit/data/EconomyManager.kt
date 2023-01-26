package io.github.arcaneplugins.polyconomy.plugin.bukkit.data

import io.github.arcaneplugins.polyconomy.plugin.bukkit.data.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.data.component.currency.PolyCurrencyConversion
import java.util.*

object EconomyManager {

    var primaryCurrency: PolyCurrency? = null
        private set

    var primaryLocale: Locale? = null
        private set

    val currencies: LinkedHashSet<PolyCurrency> = LinkedHashSet()

    val conversions: LinkedHashSet<PolyCurrencyConversion> = LinkedHashSet()

}