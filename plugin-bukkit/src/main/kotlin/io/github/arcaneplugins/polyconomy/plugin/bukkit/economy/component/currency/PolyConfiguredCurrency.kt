package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

class PolyConfiguredCurrency(
    id: String,
    symbol: String,
    exchangeRate: BigDecimal,
    displayNameSingular: String,
    displayNamePlural: String,
    decimal: Map<Locale, String>,
    val amountFormat: DecimalFormat,
    val presentationFormat: String,
    val startingBalance: BigDecimal
) : PolyCurrency(
    id,
    symbol,
    exchangeRate,
    displayNameSingular,
    displayNamePlural,
    decimal
) {
    override fun startingBalance(
        player: UUID?
    ): BigDecimal {
        return startingBalance
    }

    override fun format(
        amount: BigDecimal,
        locale: Locale?
    ): String {
        return presentationFormat
            .replace(
                "%amount%",
                amountFormat.format(amount.toDouble())
            )
            .replace(
                ".",
                decimal.getOrDefault(locale, decimal.values.first())
            )
            .replace(
                "%symbol%",
                symbol
            )
            .replace(
                "%display-name%",
                if(amount.compareTo(BigDecimal.ONE) == 1) displayNameSingular else displayNamePlural
            )
    }
}