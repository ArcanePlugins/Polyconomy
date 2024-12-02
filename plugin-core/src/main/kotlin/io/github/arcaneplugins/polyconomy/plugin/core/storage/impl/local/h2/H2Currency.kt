package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

import io.github.arcaneplugins.polyconomy.api.currency.Currency
import java.math.BigDecimal
import java.util.*

class H2Currency(
    name: String
) : Currency(name) {

    override suspend fun getSymbol(): String {
        TODO("Not yet implemented")
    }

    override suspend fun getDecimal(locale: Locale): String {
        TODO("Not yet implemented")
    }

    override suspend fun getLocaleDecimalMap(): Map<Locale, String> {
        TODO("Not yet implemented")
    }

    override suspend fun getDisplayName(plural: Boolean, locale: Locale): String {
        TODO("Not yet implemented")
    }

    override suspend fun isPrimary(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getStartingBalance(): BigDecimal {
        TODO("Not yet implemented")
    }

    override suspend fun getConversionRate(): BigDecimal {
        TODO("Not yet implemented")
    }

    override suspend fun format(amount: BigDecimal, locale: Locale): String {
        TODO("Not yet implemented")
    }

}