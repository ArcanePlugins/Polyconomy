package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.plugin.core.debug.DebugCategory.STORAGE_H2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import java.util.function.Supplier

class H2Currency(
    name: String,
    val handler: H2StorageHandler,
) : Currency(name) {

    override suspend fun getSymbol(): String {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.getSymbolForCurrency).use { statement ->
                statement.setString(1, name)
                val rs = statement.executeQuery()
                return@use if (rs.next()) {
                    rs.getString(1)
                } else {
                    throw IllegalStateException("Unable to get symbol for currency ${name} in database: no results")
                }
            }
        }
    }

    override suspend fun getDecimal(locale: Locale): String {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.getDecimalForCurrencyWithLocale).use { statement ->
                statement.setString(1, name)
                statement.setString(2, locale.toLanguageTag())
                val rs = statement.executeQuery()
                return@use if (rs.next()) {
                    rs.getString(1)
                } else {
                    getLocaleDecimalMap().entries.first().value
                }
            }
        }
    }

    override suspend fun getLocaleDecimalMap(): Map<Locale, String> {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.getLocaleDecimalPairsForCurrency).use { statement ->
                statement.setString(1, name)
                val rs = statement.executeQuery()
                val map = mutableMapOf<Locale, String>()

                while (rs.next()) {
                    val locale = Locale.forLanguageTag(rs.getString(1))
                    val decimal = rs.getString(2)

                    map[locale] = decimal
                }

                return@use map.toMap()
            }
        }
    }

    override suspend fun getDisplayName(plural: Boolean, locale: Locale): String {
        val plugin = handler.manager.plugin

        return withContext(Dispatchers.IO) {
            plugin.debugLog(STORAGE_H2) { "START getDisplayName: currency=${name}, plural=${plural}, locale=${locale}" }
            val dn: String? = handler.connection.prepareStatement(
                H2Statements.getDisplayNamesForCurrencyWithLocale
            ).use { statement ->
                statement.setString(1, name)
                statement.setString(2, locale.toLanguageTag())
                plugin.debugLog(STORAGE_H2) { "Initial statement: ${statement}" }
                val rs = statement.executeQuery()
                return@use if (rs.next()) {
                    rs.getString(
                        if (plural) {
                            2
                        } else {
                            1
                        }
                    )
                } else {
                    null
                }
            }

            // if result found, let's use that
            if (dn != null) {
                plugin.debugLog(STORAGE_H2) { "Display name: ${dn}" }
                return@withContext dn
            }

            plugin.debugLog(STORAGE_H2) { "Did not find display name with locale ${locale}." }

            // fallback to system default locale
            if (locale != handler.manager.plugin.settingsCfg.defaultLocale()) {
                plugin.debugLog(STORAGE_H2) { "Running recursive getDisplayName for default locale: ${locale}" }
                return@withContext getDisplayName(plural, handler.manager.plugin.settingsCfg.defaultLocale())
            }

            plugin.debugLog(STORAGE_H2) { "Already tried default locale, falling back to whatever first pops up on the DB." }

            // finally, fallback on whatever's the first entry that pops up from DB
            return@withContext handler.connection.prepareStatement(
                H2Statements.getDisplayNamesForCurrency
            ).use { statement ->
                statement.setString(1, name)
                plugin.debugLog(STORAGE_H2) { "SQL: ${statement}" }
                val rs = statement.executeQuery()
                return@use if (rs.next()) {
                    rs.getString(
                        if (plural) {
                            2
                        } else {
                            1
                        }
                    )
                } else {
                    throw IllegalStateException("Unable to find any display name locale records for ${name}")
                }
            }
        }
    }

    override suspend fun isPrimary(): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext handler.getPrimaryCurrency().name == name
        }
    }

    override suspend fun getStartingBalance(): BigDecimal {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.getStartingBalanceForCurrency).use { statement ->
                statement.setString(1, name)
                val rs = statement.executeQuery()
                return@use if (rs.next()) {
                    rs.getBigDecimal(1)
                } else {
                    throw IllegalStateException("Unable to get starting balance for currency ${name} in database: no results")
                }
            }
        }
    }

    override suspend fun getConversionRate(): BigDecimal {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.getConversionRateForCurrency).use { statement ->
                statement.setString(1, name)
                val rs = statement.executeQuery()
                return@use if (rs.next()) {
                    rs.getBigDecimal(1)
                } else {
                    throw IllegalStateException("Unable to get conversion rate for currency ${name} in database: no results")
                }
            }
        }
    }

    override suspend fun format(amount: BigDecimal, locale: Locale): String {
        return withContext(Dispatchers.IO) {
            val amountFormat: String // init later
            val presentationFormat: String // init later

            handler.connection.prepareStatement(H2Statements.getStringFormatsForCurrency).use { statement ->
                statement.setString(1, name)
                val rs = statement.executeQuery()
                if (!rs.next()) {
                    throw IllegalStateException("Unable to get string formats for currency ${name}: no results")
                }
                amountFormat = rs.getString(1)
                presentationFormat = rs.getString(2)
            }

            var intermediaryFormat: String = presentationFormat

            fun replaceSuppliedIfContains(target: String, replacement: Supplier<Any>) {
                if (!intermediaryFormat.contains(target)) {
                    return
                }

                intermediaryFormat = intermediaryFormat.replace(target, replacement.get().toString())
            }

            replaceSuppliedIfContains("%amount%") { DecimalFormat(amountFormat).format(amount) }
            replaceSuppliedIfContains(".") { runBlocking { getDecimal(locale) } }
            replaceSuppliedIfContains("%symbol%") { runBlocking { getSymbol() } }
            replaceSuppliedIfContains("%display-name%") {
                runBlocking {
                    getDisplayName(
                        plural = amount != BigDecimal.ZERO,
                        locale = locale,
                    )
                }
            }

            return@withContext intermediaryFormat
        }
    }

    override suspend fun setName(new: String) {
        if (name == new) {
            throw IllegalStateException("Currency name is already ${new}; can't rename to the same name")
        }

        if (isPrimary()) {
            throw IllegalStateException("Unable to rename a primary currency. Try make another currency the primary one temporarily, then change this currency's name, then set back to primary currency. This is a safety measure.")
        }

        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.currencySetName).use { statement ->
                statement.setString(1, new)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun setSymbol(new: String) {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.currencySetSymbol).use { statement ->
                statement.setString(1, new)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun setDisplayName(plural: Boolean, locale: Locale, new: String) {
        return withContext(Dispatchers.IO) {
            val statementStr: String = if (plural) {
                H2Statements.currencySetDisplayNamePlural
            } else {
                H2Statements.currencySetDisplayNameSingular
            }

            handler.connection.prepareStatement(statementStr).use { statement ->
                statement.setString(1, new)
                statement.setString(2, name)
                statement.setString(3, locale.toLanguageTag())
                statement.executeUpdate()
            }
        }
    }

    override suspend fun setStartingBalance(new: BigDecimal) {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.currencySetStartingBalance).use { statement ->
                statement.setBigDecimal(1, new)
                statement.setString(2, name)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun setDecimal(locale: Locale, new: String) {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.currencySetDecimal).use { statement ->
                statement.setString(1, new)
                statement.setString(2, name)
                statement.setString(3, locale.toLanguageTag())
                statement.executeUpdate()
            }
        }
    }

    override suspend fun setConversionRate(new: BigDecimal) {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.currencySetConversionRate).use { statement ->
                statement.setBigDecimal(1, new)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun setAmountFormat(new: String) {
        TODO("Not yet implemented")
    }

    override suspend fun setPresentationFormat(new: String) {
        TODO("Not yet implemented")
    }

    override suspend fun registerLocale(
        locale: Locale,
        dispNameSingular: String,
        dispNamePlural: String,
        decimal: String,
    ) {
        withContext(Dispatchers.IO) {
            if (hasLocale(locale)) {
                throw IllegalStateException("Can't register locale $locale: Locale is already registered.")
            }

            handler.connection.prepareStatement(H2Statements.currencyRegisterLocale).use { statement ->
                statement.setString(1, name)
                statement.setString(2, locale.toLanguageTag())
                statement.setString(3, dispNameSingular)
                statement.setString(4, dispNamePlural)
                statement.setString(5, decimal)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun unregisterLocale(locale: Locale) {
        if (!hasLocale(locale)) {
            throw IllegalStateException("Can't unregister locale $locale: Locale is not registered.")
        }

        withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.currencyUnregisterLocale).use { statement ->
                statement.setString(1, name)
                statement.setString(2, locale.toLanguageTag())
                statement.executeUpdate()
            }
        }
    }

    override suspend fun hasLocale(locale: Locale): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext handler.connection.prepareStatement(H2Statements.currencyHasLocale).use { statement ->
                statement.setString(1, name)
                statement.setString(2, locale.toLanguageTag())
                val rs = statement.executeQuery()

                rs.next()

                val count = rs.getInt(1)

                return@use when (count) {
                    0 -> false
                    1 -> true
                    else -> throw IllegalStateException("Currency ${name} has more than 1 database records for locale $locale when there should be one or none")
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is H2Currency) {
            other.name == name
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }

}