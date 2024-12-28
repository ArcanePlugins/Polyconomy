package io.github.arcaneplugins.polyconomy.api.currency

import java.math.BigDecimal
import java.util.*

abstract class Currency(
    val name: String,
) {

    companion object {
        const val DEFAULT_NAME = "dollar"
        const val DEFAULT_SYMBOL = "$"
        const val DEFAULT_DECIMAL = "."
        const val DEFAULT_DISPLAY_NAME_SINGULAR = "Dollar"
        const val DEFAULT_DISPLAY_NAME_PLURAL = "Dollars"
        const val DEFAULT_STARTING_BALANCE = 50.0
        const val DEFAULT_CONVERSION_RATE = 1.0
        const val DEFAULT_AMOUNT_FORMAT = "#,##0.00"
        const val DEFAULT_PRESENTATION_FORMAT = "%symbol%%amount%"
        // TODO: Configurate storage implementation should reference these constants ^ instead of just declaring them as needed
    }

    abstract suspend fun getSymbol(): String

    abstract suspend fun getDecimal(locale: Locale): String

    abstract suspend fun getLocaleDecimalMap(): Map<Locale, String>

    abstract suspend fun getDisplayName(
        plural: Boolean,
        locale: Locale,
    ): String

    abstract suspend fun isPrimary(): Boolean

    abstract suspend fun getStartingBalance(): BigDecimal

    abstract suspend fun getConversionRate(): BigDecimal

    abstract suspend fun format(
        amount: BigDecimal,
        locale: Locale,
    ): String

}