package io.github.arcaneplugins.polyconomy.api.currency

import java.math.BigDecimal
import java.util.*

abstract class Currency(
    val name: String,
) {

    abstract suspend fun getSymbol(): String

    abstract suspend fun getDecimal(locale: Locale): String

    abstract suspend fun getLocaleDecimalMap(locale: Locale): Map<Locale, String>

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