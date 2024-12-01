package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

import io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageHandler
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.core.util.ByteUtil
import io.github.arcaneplugins.polyconomy.plugin.core.util.ByteUtil.uuidToBytes
import java.lang.IllegalArgumentException
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
        val account = PlayerAccount(uuid)

        val existingAccount: PlayerAccount? = connection.prepareStatement(H2Statements.getPlayerAccountName).use { statement ->
            statement.setBytes(1, uuidToBytes(uuid))
            val rs = statement.executeQuery()

            return@use if (rs.next()) {
                account
            } else {
                null
            }
        }

        if (existingAccount != null) {
            return existingAccount
        }

        val accountId: Long = connection.prepareStatement(H2Statements.createAccount).use { statement ->
            statement.setString(1, name)
            val rows = statement.executeUpdate()
            if (rows == 0) {
                throw java.lang.IllegalStateException("Unable to insert account with uuid=${uuid}, name=${name}")
            }

            val rs = statement.generatedKeys
            if (rs.next()) {
                return@use rs.getLong(1)
            } else {
                throw java.lang.IllegalStateException("Unable to get inserted account ID for uuid=${uuid}, name=${name}")
            }
        }

        connection.prepareStatement(H2Statements.createPlayerAccount).use { statement ->
            statement.setLong(1, accountId)
            statement.setBytes(2, uuidToBytes(uuid))
            val rows = statement.executeUpdate()
            if (rows == 0) {
                throw java.lang.IllegalStateException("Unable to insert player account with uuid=${uuid}, name=${name}")
            }
        }

        return account
    }

    override suspend fun getOrCreateNonPlayerAccount(namespacedKey: NamespacedKey, name: String?): NonPlayerAccount {
        val account = NonPlayerAccount(namespacedKey)

        val existingAccount: NonPlayerAccount? = connection.prepareStatement(H2Statements.getNonPlayerAccountName).use { statement ->
            statement.setString(1, namespacedKey.toString())
            val rs = statement.executeQuery()

            return@use if (rs.next()) {
                account
            } else {
                null
            }
        }

        if (existingAccount != null) {
            return existingAccount
        }

        val accountId: Long = connection.prepareStatement(H2Statements.createAccount).use { statement ->
            statement.setString(1, name)
            val rows = statement.executeUpdate()
            if (rows == 0) {
                throw java.lang.IllegalStateException("Unable to insert account with namespacedKey=${namespacedKey}, name=${name}")
            }

            val rs = statement.generatedKeys
            if (rs.next()) {
                return@use rs.getLong(1)
            } else {
                throw java.lang.IllegalStateException("Unable to get inserted account ID for namespacedKey=${namespacedKey}, name=${name}")
            }
        }

        connection.prepareStatement(H2Statements.createNonPlayerAccount).use { statement ->
            statement.setLong(1, accountId)
            statement.setString(2, namespacedKey.toString())
            val rows = statement.executeUpdate()
            if (rows == 0) {
                throw java.lang.IllegalStateException("Unable to insert nonplayeraccount with namespacedKey=${namespacedKey}, name=${name}")
            }
        }

        account.setName(name)

        return account
    }

    override suspend fun getPlayerAccountIds(): Collection<UUID> {
        return connection.prepareStatement(H2Statements.getPlayerAccountIds).use { statement ->
            val rs = statement.executeQuery()
            val uuids = mutableSetOf<UUID>()
            while (rs.next()) {
                uuids.add(ByteUtil.bytesToUuid(rs.getBytes(1)))
            }
            return@use uuids.toSet() // makes it immutable
        }
    }

    override suspend fun getNonPlayerAccountIds(): Collection<NamespacedKey> {
        return connection.prepareStatement(H2Statements.getNonPlayerAccountIds).use { statement ->
            val rs = statement.executeQuery()
            val nsKeys = mutableSetOf<NamespacedKey>()
            while (rs.next()) {
                nsKeys.add(NamespacedKey.fromString(rs.getString(1)))
            }
            return@use nsKeys.toSet() // makes it immutable
        }
    }

    override suspend fun getNonPlayerAccountsPlayerIsMemberOf(uuid: UUID): Collection<NonPlayerAccount> {
        return connection.prepareStatement(H2Statements.getNonPlayerAccountsPlayerIsMemberOf).use { statement ->
            statement.setBytes(1, uuidToBytes(uuid))
            val rs = statement.executeQuery()
            val accounts = mutableSetOf<NonPlayerAccount>()
            while (rs.next()) {
                val nsKey = NamespacedKey.fromString(rs.getString(1))
                accounts.add(NonPlayerAccount(nsKey))
            }
            return@use accounts.toSet() // makes it immutable
        }
    }

    override suspend fun getPrimaryCurrency(): Currency {
        return getCurrency(manager.primaryCurrencyId)
            ?: throw IllegalArgumentException("Unable to get primary currency by ID of '${manager.primaryCurrencyId}': currency does not exist in the database (is there a typo, or was the currency created at all?)")
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