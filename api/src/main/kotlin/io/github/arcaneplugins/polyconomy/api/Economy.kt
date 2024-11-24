package io.github.arcaneplugins.polyconomy.api

import io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import java.math.BigDecimal
import java.util.*

interface Economy {

    companion object {
        const val PRECISION = 4
    }

    suspend fun getOrCreatePlayerAccount(
        uuid: UUID,
        name: String?,
    ): PlayerAccount

    suspend fun getOrCreateNonPlayerAccount(
        namespacedKey: NamespacedKey,
        name: String?,
    ): NonPlayerAccount

    suspend fun getPlayerAccountIds(): Collection<UUID>

    suspend fun getNonPlayerAccountIds(): Collection<NamespacedKey>

    suspend fun getNonPlayerAccountsPlayerIsMemberof(
        uuid: UUID,
    ): Collection<NonPlayerAccount>

    suspend fun getPrimaryCurrency(): Currency

    suspend fun getCurrency(
        name: String,
    ): Currency?

    suspend fun getCurrencies(): Collection<Currency>

    suspend fun registerCurrency(
        name: String,
        startingBalance: BigDecimal,
        symbol: String,
        amountFormat: String,
        presentationFormat: String,
        conversionRate: BigDecimal,
        displayNameSingularLocaleMap: Map<Locale, String>,
        displayNamePluralLocaleMap: Map<Locale, String>,
        decimalLocaleMap: Map<Locale, String>,
    ): Currency

    suspend fun unregisterCurrency(currency: Currency)

    suspend fun hasPlayerAccount(
        uuid: UUID,
    ): Boolean

    suspend fun hasNonPlayerAccount(
        nsKey: NamespacedKey,
    ): Boolean

    suspend fun getVaultBankAccountIds(): Collection<NamespacedKey>

    suspend fun getVaultUnlockedUuidNameMap(): Map<UUID, String>


}