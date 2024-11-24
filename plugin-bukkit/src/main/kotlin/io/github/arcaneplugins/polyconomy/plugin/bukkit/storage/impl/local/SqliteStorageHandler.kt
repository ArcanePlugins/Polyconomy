package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.local

import io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.StorageHandler
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.ThrowableUtil
import java.io.File
import java.math.BigDecimal
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class SqliteStorageHandler(
    val plugin: Polyconomy,
) : StorageHandler(
    id = "sqlite"
) {

    val dbFile = File(plugin.dataFolder, "data${File.separator}sqlite.db")
    lateinit var connection: Connection

    override fun connect() {
        var requiresInitialization = false

        if (!dbFile.exists()) {
            dbFile.mkdirs()
            dbFile.createNewFile()
            requiresInitialization = true
        }

        connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")

        if (requiresInitialization)

        connected = true
    }

    override fun disconnect() {
        if (!connected) {
            throw ThrowableUtil.explainHelpfully(
                plugin = plugin,
                throwable = IllegalArgumentException("SQLite already disconnected"),
                otherInfo = "Attempted to disconnect the SQLite storage handler, but it has already been disconnected",
                otherContext = "SqliteStorageHandler class",
                printTrace = false
            )
        }
        if (::connection.isInitialized && !connection.isClosed) {
            connection.close()
        }
        connected = false
    }

    override fun playerCacheGetName(uuid: UUID): String? {
        TODO("Not yet implemented")
    }

    override fun playerCacheSetName(uuid: UUID, name: String) {
        TODO("Not yet implemented")
    }

    override fun playerCacheIsPlayer(uuid: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getOrCreatePlayerAccount(uuid: UUID, name: String?): PlayerAccount {
        TODO("Not yet implemented")
    }

    override suspend fun getOrCreateNonPlayerAccount(namespacedKey: NamespacedKey, name: String?): NonPlayerAccount {
        TODO("Not yet implemented")
    }

    override suspend fun getPlayerAccountIds(): Collection<UUID> {
        TODO("Not yet implemented")
    }

    override suspend fun getNonPlayerAccountIds(): Collection<NamespacedKey> {
        TODO("Not yet implemented")
    }

    override suspend fun getNonPlayerAccountsPlayerIsMemberof(uuid: UUID): Collection<NonPlayerAccount> {
        TODO("Not yet implemented")
    }

    override suspend fun getPrimaryCurrency(): Currency {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrency(name: String): Currency? {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrencies(): Collection<Currency> {
        TODO("Not yet implemented")
    }

    override suspend fun registerCurrency(
        name: String,
        startingBalance: BigDecimal,
        symbol: String,
        amountFormat: String,
        presentationFormat: String,
        conversionRate: BigDecimal,
        displayNameSingularLocaleMap: Map<Locale, String>,
        displayNamePluralLocaleMap: Map<Locale, String>,
        decimalLocaleMap: Map<Locale, String>,
    ): Currency {
        TODO("Not yet implemented")
    }

    override suspend fun unregisterCurrency(currency: Currency) {
        TODO("Not yet implemented")
    }

    override suspend fun hasPlayerAccount(uuid: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun hasNonPlayerAccount(nsKey: NamespacedKey): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getVaultBankAccountIds(): Collection<NamespacedKey> {
        TODO("Not yet implemented")
    }

    override suspend fun getVaultUnlockedUuidNameMap(): Map<UUID, String> {
        TODO("Not yet implemented")
    }

}