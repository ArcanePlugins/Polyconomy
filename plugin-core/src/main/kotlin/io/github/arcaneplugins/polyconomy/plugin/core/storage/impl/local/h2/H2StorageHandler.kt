package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

import io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageHandler
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.core.util.ByteUtil.uuidToBytes
import java.math.BigDecimal
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.time.Instant
import java.util.*

class H2StorageHandler(
    manager: StorageManager,
    val absolutePath: Path,
) : StorageHandler("h2", manager) {

    companion object {
        const val URI_PREFIX = "jdbc:h2:file:"
        const val USERNAME = "Polyconomy"
        const val PASSWORD = "Polyconomy"
    }

    private lateinit var connection: Connection

    override fun startup() {
        if (connected) {
            throw IllegalStateException("Already connected to the database")
        }

        val uri: String = URI_PREFIX + absolutePath.toString()

        connection = DriverManager.getConnection(uri, USERNAME, PASSWORD)

        connected = true

        createTables()
    }

    override fun shutdown() {
        if (!connected) {
            return
        }

        connection.close()

        connected = false
    }

    private fun createTables() {
        connection.createStatement().use { statement ->
            H2Statements.createTablesStatements.forEach(statement::addBatch)
            statement.executeBatch()
        }

    }

    override suspend fun playerCacheGetName(uuid: UUID): String? {
        return connection.prepareStatement(H2Statements.getUsernameByUuid).use { statement ->
            statement.setBytes(1, uuidToBytes(uuid))
            val rs = statement.executeQuery()
            return@use if (rs.next()) {
                rs.getString("username")
            } else {
                null
            }
        }
    }

    override suspend fun playerCacheSetName(uuid: UUID, name: String) {
        return connection.prepareStatement(H2Statements.setUsernameForUuid).use { statement ->
            statement.setBytes(1, uuidToBytes(uuid))
            statement.setString(2, name)
            statement.setLong(3, Instant.now().epochSecond)
            statement.executeUpdate()
        }
    }

    override suspend fun playerCacheIsPlayer(uuid: UUID): Boolean {
        return connection.prepareStatement(H2Statements.isPlayerCached).use { statement ->
            statement.setBytes(1, uuidToBytes(uuid))
            val rs = statement.executeQuery()
            return@use if (rs.next()) {
                rs.getInt(1) > 0
            } else {
                false
            }
        }
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

    override suspend fun hasCurrency(name: String): Boolean {
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