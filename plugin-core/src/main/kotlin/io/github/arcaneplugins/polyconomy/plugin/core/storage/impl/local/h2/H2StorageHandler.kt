package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

import io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import io.github.arcaneplugins.polyconomy.plugin.core.debug.DebugCategory
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageHandler
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.core.util.ByteUtil.bytesToUuid
import io.github.arcaneplugins.polyconomy.plugin.core.util.ByteUtil.uuidToBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.h2.tools.Server
import java.math.BigDecimal
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.sql.Types
import java.time.Instant
import java.util.*

class H2StorageHandler(
    manager: StorageManager,
    val absolutePath: Path,
) : StorageHandler("h2", manager) {

    companion object {
        const val URI_PREFIX = "jdbc:h2:file:"
        const val USERNAME = ""
        const val PASSWORD = ""
    }

    lateinit var connection: Connection
        private set

    fun startDebugServer() {
        Server.startWebServer(connection)
    }

    override fun startup() {
        if (connected) {
            throw IllegalStateException("Already connected to the database")
        }

        // Load driver into classpath
        Class.forName("org.h2.Driver")

        val uri: String = URI_PREFIX + absolutePath.toString()

        connection = DriverManager.getConnection(uri, USERNAME, PASSWORD)

        connected = true

        createTables()
        insertDefaults()
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
            H2Statements.createTablesStatements.forEach(statement::executeUpdate)
        }
    }

    private fun insertDefaults() {
        insertDefaultCurrencyIfNoCurrencies()
    }

    private fun insertDefaultCurrencyIfNoCurrencies() {
        val anyCurrencyExists: Boolean = connection.prepareStatement(H2Statements.getCurrencyNames).use { statement ->
            val rs = statement.executeQuery()
            return@use rs.next()
        }

        if (anyCurrencyExists) {
            return
        }

        return runBlocking {
            registerCurrency(
                name = Currency.DEFAULT_NAME,
                startingBalance = Currency.DEFAULT_STARTING_BALANCE.toBigDecimal(),
                symbol = Currency.DEFAULT_SYMBOL,
                amountFormat = Currency.DEFAULT_AMOUNT_FORMAT,
                presentationFormat = Currency.DEFAULT_PRESENTATION_FORMAT,
                conversionRate = Currency.DEFAULT_CONVERSION_RATE.toBigDecimal(),
                displayNameSingularLocaleMap = mapOf(Locale.ENGLISH to Currency.DEFAULT_DISPLAY_NAME_SINGULAR),
                displayNamePluralLocaleMap = mapOf(Locale.ENGLISH to Currency.DEFAULT_DISPLAY_NAME_PLURAL),
                decimalLocaleMap = mapOf(Locale.ENGLISH to Currency.DEFAULT_DECIMAL),
            )
        }
    }

    suspend fun getCurrencyDbId(name: String): Long {
        return withContext(Dispatchers.IO) {
            return@withContext connection.prepareStatement(H2Statements.getCurrencyDbId).use { statement ->
                statement.setString(1, name)
                val rs = statement.executeQuery()
                return@use if (rs.next()) {
                    rs.getLong(1)
                } else {
                    throw IllegalStateException("Unable to retrieve currency DB ID for CurrencyName = ${name}")
                }
            }
        }
    }

    override suspend fun playerCacheGetName(uuid: UUID): String? {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getUsernameByUuid).use { statement ->
                statement.setBytes(1, uuidToBytes(uuid))
                val rs = statement.executeQuery()
                return@use if (rs.next()) {
                    rs.getString("username")
                } else {
                    null
                }
            }
        }
    }

    override suspend fun playerCacheSetName(uuid: UUID, name: String) {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.setUsernameForUuid).use { statement ->
                statement.setBytes(1, uuidToBytes(uuid))
                statement.setString(2, name)
                statement.setLong(3, Instant.now().epochSecond)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun playerCacheIsPlayer(uuid: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.isPlayerCached).use { statement ->
                statement.setBytes(1, uuidToBytes(uuid))
                val rs = statement.executeQuery()
                return@use if (rs.next()) {
                    rs.getInt(1) > 0
                } else {
                    false
                }
            }
        }
    }

    override suspend fun purgeOldTransactions() {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.purgeOldTransactionsStatement()).use { statement ->
                statement.executeUpdate()
            }
        }
    }

    override suspend fun baltop(page: Int, pageSize: Int, currency: Currency): Map<String, BigDecimal> {
        return withContext(Dispatchers.IO) {
            val offset = (page - 1) * pageSize
            val results = mutableMapOf<String, BigDecimal>()

            connection.prepareStatement(H2Statements.getBaltop).use { statement ->
                statement.setString(1, currency.name)
                statement.setInt(2, pageSize)
                statement.setInt(3, offset)
                val rs = statement.executeQuery()
                while (rs.next()) {
                    val username = rs.getString(1)
                    val balance = rs.getBigDecimal(2)
                    results[username] = balance
                }
            }

            return@withContext results
        }
    }

    override suspend fun getOrCreatePlayerAccount(uuid: UUID, name: String?): PlayerAccount {
        return withContext(Dispatchers.IO) {
            val account = H2PlayerAccount(uuid, this@H2StorageHandler)

            val existingAccount: PlayerAccount? =
                connection.prepareStatement(H2Statements.getPlayerAccountName).use { statement ->
                    statement.setBytes(1, uuidToBytes(uuid))
                    val rs = statement.executeQuery()

                    return@use if (rs.next()) {
                        account
                    } else {
                        null
                    }
                }

            if (existingAccount != null) {
                return@withContext existingAccount
            }

            val accountId: Long = connection.prepareStatement(
                H2Statements.createAccount,
                Statement.RETURN_GENERATED_KEYS,
            ).use { statement ->
                if (name == null) {
                    statement.setNull(1, Types.VARCHAR)
                } else {
                    statement.setString(1, name)
                }
                val rows = statement.executeUpdate()
                if (rows == 0) {
                    throw IllegalStateException("Unable to insert account with uuid=${uuid}, name=${name}")
                }

                val rs = statement.generatedKeys
                if (rs.next()) {
                    return@use rs.getLong(1)
                } else {
                    throw IllegalStateException("Unable to get inserted account ID for uuid=${uuid}, name=${name}")
                }
            }

            connection.prepareStatement(H2Statements.createPlayerAccount).use { statement ->
                statement.setLong(1, accountId)
                statement.setBytes(2, uuidToBytes(uuid))
                val rows = statement.executeUpdate()
                if (rows == 0) {
                    throw IllegalStateException("Unable to insert player account with uuid=${uuid}, name=${name}")
                }
            }

            return@withContext account
        }
    }

    override suspend fun getOrCreateNonPlayerAccount(namespacedKey: NamespacedKey, name: String?): NonPlayerAccount {
        return withContext(Dispatchers.IO) {
            val account = H2NonPlayerAccount(namespacedKey, this@H2StorageHandler)

            val existingAccount: NonPlayerAccount? =
                connection.prepareStatement(H2Statements.getNonPlayerAccountName).use { statement ->
                    statement.setString(1, namespacedKey.toString())
                    val rs = statement.executeQuery()

                    return@use if (rs.next()) {
                        account
                    } else {
                        null
                    }
                }

            if (existingAccount != null) {
                return@withContext existingAccount
            }

            val accountId: Long = connection.prepareStatement(
                H2Statements.createAccount,
                Statement.RETURN_GENERATED_KEYS,
            ).use { statement ->
                statement.setString(1, name)
                val rows = statement.executeUpdate()
                if (rows == 0) {
                    throw IllegalStateException("Unable to insert account with namespacedKey=${namespacedKey}, name=${name}")
                }

                val rs = statement.generatedKeys
                if (rs.next()) {
                    return@use rs.getLong(1)
                } else {
                    throw IllegalStateException("Unable to get inserted account ID for namespacedKey=${namespacedKey}, name=${name}")
                }
            }

            connection.prepareStatement(H2Statements.createNonPlayerAccount).use { statement ->
                statement.setLong(1, accountId)
                statement.setString(2, namespacedKey.toString())
                val rows = statement.executeUpdate()
                if (rows == 0) {
                    throw IllegalStateException("Unable to insert nonplayeraccount with namespacedKey=${namespacedKey}, name=${name}")
                }
            }

            account.setName(name)

            return@withContext account
        }
    }

    override suspend fun getPlayerAccountIds(): Collection<UUID> {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getPlayerAccountIds).use { statement ->
                val rs = statement.executeQuery()
                val uuids = mutableSetOf<UUID>()
                while (rs.next()) {
                    uuids.add(bytesToUuid(rs.getBytes(1)))
                }
                return@use uuids.toSet() // makes it immutable
            }
        }
    }

    override suspend fun getNonPlayerAccountIds(): Collection<NamespacedKey> {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getNonPlayerAccountIds).use { statement ->
                val rs = statement.executeQuery()
                val nsKeys = mutableSetOf<NamespacedKey>()
                while (rs.next()) {
                    nsKeys.add(NamespacedKey.fromString(rs.getString(1)))
                }
                return@use nsKeys.toSet() // makes it immutable
            }
        }
    }

    override suspend fun getNonPlayerAccountsPlayerIsMemberOf(uuid: UUID): Collection<NonPlayerAccount> {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getNonPlayerAccountsPlayerIsMemberOf).use { statement ->
                statement.setBytes(1, uuidToBytes(uuid))
                val rs = statement.executeQuery()
                val accounts = mutableSetOf<NonPlayerAccount>()
                while (rs.next()) {
                    val nsKey = NamespacedKey.fromString(rs.getString(1))
                    accounts.add(getOrCreateNonPlayerAccount(nsKey, name = null))
                }
                return@use accounts.toSet() // makes it immutable
            }
        }
    }

    override suspend fun getPrimaryCurrency(): Currency {
        return getCurrency(manager.primaryCurrencyId)
            ?: throw IllegalArgumentException("Unable to get primary currency by ID of '${manager.primaryCurrencyId}': currency does not exist in the database (is there a typo, or was the currency created at all?)")
    }

    override suspend fun getCurrency(name: String): Currency? {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getCurrencyByName).use { statement ->
                statement.setString(1, name)
                return@use if (statement.executeQuery().next()) {
                    H2Currency(name, this@H2StorageHandler)
                } else {
                    null
                }
            }
        }
    }

    override suspend fun getCurrencies(): Collection<Currency> {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getCurrencyNames).use { statement ->
                val rs = statement.executeQuery()
                val currencies = mutableSetOf<Currency>()
                while (rs.next()) {
                    currencies.add(H2Currency(name = rs.getString(1), this@H2StorageHandler))
                }
                return@use currencies.toSet()
            }
        }
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
        return withContext(Dispatchers.IO) {
            if (hasCurrency(name)) {
                return@withContext H2Currency(name, this@H2StorageHandler)
            }

            val id: Long = connection.prepareStatement(
                H2Statements.insertCurrency,
                Statement.RETURN_GENERATED_KEYS,
            ).use { statement ->
                statement.setString(1, name)
                statement.setBigDecimal(2, startingBalance)
                statement.setString(3, symbol)
                statement.setString(4, amountFormat)
                statement.setString(5, presentationFormat)
                statement.setBigDecimal(6, conversionRate)
                statement.executeUpdate()

                val rs = statement.generatedKeys

                if (rs.next()) {
                    return@use rs.getLong(1)
                } else {
                    throw IllegalStateException("Expected non-empty generated keys result set whilst inserting currency ${name} into the database")
                }
            }

            val locales: Set<Locale> = displayNameSingularLocaleMap.keys
                .plus(displayNamePluralLocaleMap.keys)
                .plus(decimalLocaleMap.keys)

            fun getStrValueInLocaleMap(
                locale: Locale,
                map: Map<Locale, String>,
            ): String {
                return map.getOrDefault(
                    locale,
                    map.getOrDefault(
                        manager.plugin.settingsCfg.defaultLocale(),
                        map.entries.first().value
                    )
                )
            }

            locales.forEach { locale ->
                connection.prepareStatement(H2Statements.insertCurrencyLocale).use { statement ->
                    statement.setLong(1, id)
                    statement.setString(2, locale.toLanguageTag())
                    statement.setString(3, getStrValueInLocaleMap(locale, displayNameSingularLocaleMap))
                    statement.setString(4, getStrValueInLocaleMap(locale, displayNamePluralLocaleMap))
                    statement.setString(5, getStrValueInLocaleMap(locale, decimalLocaleMap))
                    statement.executeUpdate()
                    manager.plugin.debugLog(DebugCategory.STORAGE_H2) { "Inserted locale ${locale.toLanguageTag()} into DB for currency ${name} - statement#toString: $statement" }
                }
            }

            return@withContext H2Currency(name, this@H2StorageHandler)
        }
    }

    override suspend fun unregisterCurrency(currency: Currency) {
        withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.deleteCurrency).use { statement ->
                statement.setString(1, currency.name)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun hasCurrency(name: String): Boolean {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getCurrencyByName).use { statement ->
                statement.setString(1, name)
                return@use statement.executeQuery().next()
            }
        }
    }

    override suspend fun hasPlayerAccount(uuid: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getPlayerAccountName).use { statement ->
                statement.setBytes(1, uuidToBytes(uuid))
                return@use statement.executeQuery().next()
            }
        }
    }

    override suspend fun hasNonPlayerAccount(nsKey: NamespacedKey): Boolean {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getNonPlayerAccountName).use { statement ->
                statement.setString(1, nsKey.toString())
                return@use statement.executeQuery().next()
            }
        }
    }

    override suspend fun getVaultBankAccountIds(): Collection<NamespacedKey> {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getVaultBankAccountIds).use { statement ->
                val rs = statement.executeQuery()
                val nsKeys = mutableSetOf<NamespacedKey>()
                while (rs.next()) {
                    nsKeys.add(NamespacedKey.fromString(rs.getString(1)))
                }
                return@use nsKeys.toSet()
            }
        }
    }

    override suspend fun getVaultUnlockedUuidNameMap(): Map<UUID, String> {
        return withContext(Dispatchers.IO) {
            connection.prepareStatement(H2Statements.getPlayerAccountUuidAndNames).use { statement ->
                val rs = statement.executeQuery()
                val map = mutableMapOf<UUID, String>()
                while (rs.next()) {
                    val uuid = bytesToUuid(rs.getBytes(1))
                    val name = rs.getString(2)
                    map[uuid] = name
                }
                return@use map
            }.plus(connection.prepareStatement(H2Statements.getVaultUnlockedNonPlayerAccounts).use { statement ->
                val rs = statement.executeQuery()
                val map = mutableMapOf<UUID, String>()
                while (rs.next()) {
                    val uuid = UUID.fromString(rs.getString(1).split(":")[1])
                    val name = rs.getString(2)
                    map[uuid] = name
                }
                return@use map
            })
        }
    }
}