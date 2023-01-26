package io.github.arcaneplugins.polyconomy.plugin.bukkit.data.component.currency

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

class PolyCurrency(
    id: String,
    dbId: Int,
    enabled: Boolean,
    startingBalance: BigDecimal,
    symbol: String,
    locales: LinkedHashSet<CurrencyLocale>
) {
    class CurrencyLocale(
        id: Locale,
        displayName: String,
        wordFormatSingular: String?,
        wordFormatPlural: String?,
        decimalChar: String,
        amountFormat: DecimalFormat,
        presentationFormat: String
    )
}