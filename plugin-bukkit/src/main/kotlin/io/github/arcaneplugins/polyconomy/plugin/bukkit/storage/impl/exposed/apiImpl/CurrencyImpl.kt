package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.apiImpl

import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.ExposedStorageHandler
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.CurrencyLocaleSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.CurrencySchema
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

class CurrencyImpl(
    val handler: ExposedStorageHandler,
    name: String,
) : Currency(
    name = name
) {

    var populated = false
    lateinit var symbol: String
    lateinit var decimalLocaleMap: Map<Locale, String>
    lateinit var displayNameSingularLocaleMap: Map<Locale, String>
    lateinit var displayNamePluralLocaleMap: Map<Locale, String>
    lateinit var startingBalance: BigDecimal
    lateinit var amountFormat: String
    lateinit var presentationFormat: String
    lateinit var conversionRate: BigDecimal

    // WARNING: BLOCKING FUNCTION - NO COROUTINE
    private fun populate() {
        if (populated) {
            return
        }

        transaction {
            val mutableDecimalLocaleMap: MutableMap<Locale, String> = mutableMapOf()
            val mutableDisplayNameSingularLocaleMap: MutableMap<Locale, String> = mutableMapOf()
            val mutableDisplayNamePluralLocaleMap: MutableMap<Locale, String> = mutableMapOf()

            val currRow = CurrencySchema
                .selectAll()
                .where { (CurrencySchema.enabled eq true) and (CurrencySchema.name eq name) }
                .firstOrNull() ?: throw IllegalArgumentException("No enabled currency in database named ${name}")

            symbol = currRow[CurrencySchema.symbol]
            startingBalance = currRow[CurrencySchema.startingBalance]
            amountFormat = currRow[CurrencySchema.amountFormat]
            presentationFormat = currRow[CurrencySchema.presentationFormat]
            conversionRate = currRow[CurrencySchema.conversionRate]

            val id = currRow[CurrencySchema.id]
            CurrencyLocaleSchema
                .selectAll()
                .where { CurrencyLocaleSchema.currencyId eq id }
                .forEach {
                    val locale = Locale.forLanguageTag(it[CurrencyLocaleSchema.locale])

                    mutableDecimalLocaleMap[locale] = it[CurrencyLocaleSchema.decimal]
                    mutableDisplayNameSingularLocaleMap[locale] = it[CurrencyLocaleSchema.displayNameSingular]
                    mutableDisplayNamePluralLocaleMap[locale] = it[CurrencyLocaleSchema.displayNamePlural]
                }

            decimalLocaleMap = mutableDecimalLocaleMap
            displayNameSingularLocaleMap = mutableDisplayNameSingularLocaleMap
            displayNamePluralLocaleMap = mutableDisplayNamePluralLocaleMap
        }

        populated = true
    }

    override suspend fun getSymbol(): String {
        populate()
        return symbol
    }

    override suspend fun getDecimal(locale: Locale): String {
        populate()
        return decimalLocaleMap[locale]!!
    }

    override suspend fun getLocaleDecimalMap(): Map<Locale, String> {
        populate()
        return decimalLocaleMap
    }

    override suspend fun getDisplayName(plural: Boolean, locale: Locale): String {
        populate()
        return if(plural) {
            displayNameSingularLocaleMap[locale]!!
        } else {
            displayNamePluralLocaleMap[locale]!!
        }
    }

    override suspend fun isPrimary(): Boolean {
        return name == handler.plugin.settings.getPrimaryCurrencyId()
    }

    override suspend fun getStartingBalance(): BigDecimal {
        populate()
        return startingBalance
    }

    override suspend fun getConversionRate(): BigDecimal {
        populate()
        return conversionRate
    }

    override suspend fun format(amount: BigDecimal, locale: Locale): String {
        populate()

        return presentationFormat
            .replace("%symbol%", getSymbol())
            .replace("%amount%", DecimalFormat(amountFormat).format(amount))
            .replace("%display-name%", getDisplayName(amount.compareTo(BigDecimal.ONE) == 0, locale))
    }

}