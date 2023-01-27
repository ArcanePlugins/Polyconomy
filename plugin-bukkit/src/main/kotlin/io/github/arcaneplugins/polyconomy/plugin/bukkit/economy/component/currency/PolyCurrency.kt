package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

class PolyCurrency(
    val id: String,
    val dbId: Int,
    val enabled: Boolean,
    val startingBalance: BigDecimal,
    val symbol: String,
    val locales: MutableList<CurrencyLocale>
) {
    class CurrencyLocale(
        val id: Locale,
        val displayName: String,
        val wordFormatSingular: String?,
        val wordFormatPlural: String?,
        val decimalChar: String,
        val amountFormat: DecimalFormat,
        val presentationFormat: String
    )
}