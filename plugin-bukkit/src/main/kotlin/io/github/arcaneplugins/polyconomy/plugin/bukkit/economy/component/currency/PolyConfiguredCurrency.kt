package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.PolyAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponseError
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

class PolyConfiguredCurrency(
    id: String,
    symbol: String,
    exchangeRate: BigDecimal,
    displayNameSingular: String,
    displayNamePlural: String,
    decimal: Map<Locale, Char>,
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
        account: PolyAccount?
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
                "${decimalMap.getOrDefault(locale, decimalMap.values.first())}"
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

    override fun parseSync(
        formatted: String,
        locale: Locale?
    ): PolyResponse<BigDecimal> {
        val responseName = "parseSync; formatted=${formatted}; id=${id}"

        return try {
            PolyResponse(
                name = responseName,
                result = BigDecimal(
                    formatted.replace(
                        regex = Regex("[^\\d${decimal(locale)}]+"),
                        replacement = ""
                    )
                ),
                error = null
            )
        } catch (ex: Exception) {
            PolyResponse(
                name = responseName,
                result = null,
                error = PolyResponseError.fromException(ex)
            )
        }
    }
}