package io.github.arcaneplugins.polyconomy.api

import io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import java.util.*

interface Economy {

    suspend fun getOrCreatePlayerAccount(
        uuid: UUID
    ): PlayerAccount

    suspend fun getPlayerAccountIds(): Set<UUID>

    suspend fun getNonPlayerAccountIds(): Set<NamespacedKey>

    suspend fun getNonPlayerAccountsPlayerIsMemberof(
        uuid: UUID
    ): Set<NonPlayerAccount>

    suspend fun getPrimaryCurrency(): Currency

    suspend fun getCurrency(
        name: String
    ): Currency?

    suspend fun getCurrencies(): Set<Currency>

    suspend fun registerCurrency(currency: Currency)

    suspend fun unregisterCurrency(currency: Currency)


}