package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed

import io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import io.github.arcaneplugins.polyconomy.api.util.cause.ServerCause
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.StorageHandler
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.apiImpl.CurrencyImpl
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.apiImpl.NonPlayerAccountImpl
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.apiImpl.PlayerAccountImpl
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.AccountBalanceSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.AccountTransactionSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.NonPlayerAccountMemberSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.PlayerCacheSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.VaultBankAccountNonPlayerMemberSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.ByteUtil.bytesToUuid
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.ByteUtil.uuidToBytes
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.ThrowableUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.util.*
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.AccountSchema as AccountSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.CurrencySchema as CurrencySchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.CurrencyLocaleSchema as CurrencyLocaleSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.NonPlayerAccountSchema as NonPlayerAccountSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.PlayerAccountSchema as PlayerAccountSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.VaultBankAccountSchema as VaultBankAccountSchema

abstract class ExposedStorageHandler(
    val plugin: Polyconomy,
    id: String,
) : StorageHandler(
    id = id
) {

    lateinit var db: Database
        private set

    protected abstract fun initializeDb(): Database

    override fun connect() {
        db = initializeDb()
        runBlocking { createTables() }
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
        TransactionManager.closeAndUnregister(database = db)
        connected = false
    }

    private suspend fun createTables() {
        withContext(Dispatchers.IO) {
            transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    AccountSchema,
                    AccountBalanceSchema,
                    AccountTransactionSchema,
                    CurrencySchema,
                    CurrencyLocaleSchema,
                    io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.NonPlayerAccountSchema,
                    NonPlayerAccountMemberSchema,
                    io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.PlayerAccountSchema,
                    PlayerCacheSchema,
                    VaultBankAccountSchema,
                    VaultBankAccountNonPlayerMemberSchema,
                )

                val noCurrenciesRecorded = CurrencySchema
                    .selectAll()
                    .none()

                if (noCurrenciesRecorded) {
                    runBlocking {
                        val id = CurrencySchema
                            .insertAndGetId {
                                it[name] = "dollar"
                                it[startingBalance] = BigDecimal.valueOf(50)
                                it[symbol] = "$"
                                it[amountFormat] = "#,##0.00"
                                it[presentationFormat] = "%symbol%%amount%"
                                it[conversionRate] = BigDecimal.ONE
                            }

                        CurrencyLocaleSchema
                            .insert {
                                it[currencyId] = id
                                it[locale] = Locale.ENGLISH.toLanguageTag()
                                it[displayNameSingular] = "Dollar"
                                it[displayNamePlural] = "Dollars"
                                it[decimal] = "."
                            }
                    }
                }

                TODO("Insert default currency and currencylocale values if needed")
            }
        }
    }

    override suspend fun playerCacheGetName(uuid: UUID): String? {
        return withContext(Dispatchers.IO) {
            transaction {
                PlayerCacheSchema
                    .select(PlayerCacheSchema.playerUuid)
                    .where { PlayerCacheSchema.playerUuid eq uuidToBytes(uuid) }
                    .map { it[PlayerCacheSchema.username] }
                    .singleOrNull()
            }
        }
    }

    override suspend fun playerCacheSetName(uuid: UUID, name: String) {
        return withContext(Dispatchers.IO) {
            transaction {
                val uuidAsBytes = uuidToBytes(uuid)

                val exists = runBlocking {
                    playerCacheIsPlayer(uuid)
                }

                if (exists) {
                    PlayerCacheSchema
                        .update({ PlayerCacheSchema.playerUuid eq uuidAsBytes })
                        {
                            it[username] = name
                        }
                } else {
                    PlayerCacheSchema
                        .insert {
                            it[playerUuid] = uuidAsBytes
                            it[username] = name
                        }
                }
            }
        }
    }

    override suspend fun playerCacheIsPlayer(uuid: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                PlayerCacheSchema
                    .select(PlayerCacheSchema.playerUuid)
                    .where { PlayerCacheSchema.playerUuid eq uuidToBytes(uuid) }
                    .any()
            }
        }
    }

    override suspend fun getOrCreatePlayerAccount(uuid: UUID, name: String?): PlayerAccount {
        return withContext(Dispatchers.IO) {
            transaction {
                if (runBlocking { hasPlayerAccount(uuid) }) {
                    PlayerAccountImpl(this@ExposedStorageHandler, uuid)
                }

                val id = AccountSchema
                    .insertAndGetId {
                        it[AccountSchema.name] = name
                    }

                PlayerAccountSchema
                    .insert {
                        it[PlayerAccountSchema.id] = id
                        it[playerUuid] = uuidToBytes(uuid)
                    }

                val acc = PlayerAccountImpl(this@ExposedStorageHandler, uuid)

                runBlocking {
                    acc.resetBalance(
                        currency = getPrimaryCurrency(),
                        cause = ServerCause,
                        reason = "Account creation",
                        importance = TransactionImportance.HIGH
                    )
                }

                acc
            }
        }
    }

    override suspend fun getOrCreateNonPlayerAccount(namespacedKey: NamespacedKey, name: String?): NonPlayerAccount {
        return withContext(Dispatchers.IO) {
            transaction {
                if (runBlocking { hasNonPlayerAccount(namespacedKey) }) {
                    NonPlayerAccountImpl(this@ExposedStorageHandler, namespacedKey)
                }

                val id = AccountSchema
                    .insertAndGetId {
                        it[AccountSchema.name] = name
                    }

                NonPlayerAccountSchema
                    .insert {
                        it[PlayerAccountSchema.id] = id
                        it[NonPlayerAccountSchema.namespacedKey] = namespacedKey.toString()
                    }

                val acc = NonPlayerAccountImpl(this@ExposedStorageHandler, namespacedKey)

                runBlocking {
                    acc.resetBalance(
                        currency = getPrimaryCurrency(),
                        cause = ServerCause,
                        reason = "Account creation",
                        importance = TransactionImportance.HIGH
                    )
                }

                acc
            }
        }
    }

    override suspend fun getPlayerAccountIds(): Collection<UUID> {
        return withContext(Dispatchers.IO) {
            transaction {
                PlayerAccountSchema
                    .selectAll()
                    .map { bytesToUuid(it[PlayerAccountSchema.playerUuid]) }
            }
        }
    }

    override suspend fun getNonPlayerAccountIds(): Collection<NamespacedKey> {
        return withContext(Dispatchers.IO) {
            transaction {
                NonPlayerAccountSchema
                    .selectAll()
                    .map { NamespacedKey.fromString(it[NonPlayerAccountSchema.namespacedKey]) }
            }
        }
    }

    override suspend fun getNonPlayerAccountsPlayerIsMemberof(uuid: UUID): Collection<NonPlayerAccount> {
        return withContext(Dispatchers.IO) {
            transaction {
                val uuidAsBytes = uuidToBytes(uuid)

                NonPlayerAccountMemberSchema
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = NonPlayerAccountSchema.id,
                        otherColumn = NonPlayerAccountMemberSchema.accountId
                    )
                    .select(NonPlayerAccountSchema.namespacedKey)
                    .where { NonPlayerAccountMemberSchema.memberId eq uuidAsBytes }
                    .map {
                        NonPlayerAccountImpl(
                            handler = this@ExposedStorageHandler,
                            nsKey = NamespacedKey.fromString(it[NonPlayerAccountSchema.namespacedKey])
                        )
                    }
            }
        }
    }

    override suspend fun getPrimaryCurrency(): Currency {
        return CurrencyImpl(
            handler = this@ExposedStorageHandler,
            name = plugin.settings.getPrimaryCurrencyId()
        )
    }

    override suspend fun hasCurrency(name: String): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                CurrencySchema
                    .select(CurrencySchema.name)
                    .where {
                        (CurrencySchema.name eq name) and
                                (CurrencySchema.enabled eq true)
                    }
                    .any()
            }
        }
    }

    override suspend fun getCurrency(name: String): Currency? {
        return withContext(Dispatchers.IO) {
            transaction {
                val hasCurrency = runBlocking { hasCurrency(name) }

                return@transaction if (hasCurrency) {
                    CurrencyImpl(handler = this@ExposedStorageHandler, name = name)
                } else {
                    null
                }
            }
        }
    }

    override suspend fun getCurrencies(): Collection<Currency> {
        return withContext(Dispatchers.IO) {
            transaction {
                CurrencySchema
                    .selectAll()
                    .where { CurrencySchema.enabled eq true }
                    .map { CurrencyImpl(this@ExposedStorageHandler, it[CurrencySchema.name]) }
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
            transaction {
                if (runBlocking { hasCurrency(name) }) {
                    throw java.lang.IllegalArgumentException("Currency already exists")
                }

                val id = CurrencySchema
                    .insertAndGetId {
                        it[CurrencySchema.name] = name
                        it[CurrencySchema.startingBalance] = startingBalance
                        it[CurrencySchema.symbol] = symbol
                        it[CurrencySchema.amountFormat] = amountFormat
                        it[CurrencySchema.presentationFormat] = presentationFormat
                        it[CurrencySchema.conversionRate] = conversionRate
                    }

                val locales = displayNameSingularLocaleMap.keys
                    .plus(displayNamePluralLocaleMap.keys)
                    .plus(decimalLocaleMap.keys)

                locales.forEach { locale ->
                    CurrencyLocaleSchema.insert {
                        it[currencyId] = id
                        it[CurrencyLocaleSchema.locale] = locale.toLanguageTag()
                        it[displayNameSingular] = displayNameSingularLocaleMap[locale]!!
                        it[displayNamePlural] = displayNamePluralLocaleMap[locale]!!
                        it[decimal] = decimalLocaleMap[locale]!!
                    }
                }

                CurrencyImpl(this@ExposedStorageHandler, name)
            }
        }
    }

    override suspend fun unregisterCurrency(currency: Currency) {
        return withContext(Dispatchers.IO) {
            transaction {
                CurrencySchema
                    .deleteWhere { name eq currency.name }
            }
        }
    }

    override suspend fun hasPlayerAccount(uuid: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                val uuidAsBytes = uuidToBytes(uuid)

                PlayerAccountSchema
                    .selectAll()
                    .where { PlayerAccountSchema.playerUuid eq uuidAsBytes }
                    .any()
            }
        }
    }

    override suspend fun hasNonPlayerAccount(nsKey: NamespacedKey): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                val nsKeyAsStr = nsKey.toString()

                NonPlayerAccountSchema
                    .selectAll()
                    .where { NonPlayerAccountSchema.namespacedKey eq nsKeyAsStr }
                    .any()
            }
        }
    }

    override suspend fun getVaultBankAccountIds(): Collection<NamespacedKey> {
        return withContext(Dispatchers.IO) {
            transaction {
                VaultBankAccountSchema
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        otherColumn = NonPlayerAccountSchema.id,
                        joinType = JoinType.INNER,
                        onColumn = VaultBankAccountSchema.id
                    )
                    .select(NonPlayerAccountSchema.namespacedKey)
                    .map { NamespacedKey.fromString(it[NonPlayerAccountSchema.namespacedKey]) }
            }
        }
    }

    override suspend fun getVaultUnlockedUuidNameMap(): Map<UUID, String> {
        return withContext(Dispatchers.IO) {
            transaction {
                VaultBankAccountSchema
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        otherColumn = NonPlayerAccountSchema.id,
                        joinType = JoinType.INNER,
                        onColumn = VaultBankAccountSchema.id
                    )
                    .join(
                        otherTable = AccountSchema,
                        otherColumn = AccountSchema.id,
                        joinType = JoinType.INNER,
                        onColumn = VaultBankAccountSchema.id
                    )
                    .select(NonPlayerAccountSchema.namespacedKey, AccountSchema.name)
                    .associate {
                        val nsk = NamespacedKey.fromString(it[NonPlayerAccountSchema.namespacedKey])
                        val uuid = UUID.fromString(nsk.key)
                        val name = (it[AccountSchema.name] ?: "")

                        return@associate uuid to name
                    }
                    .plus(
                        PlayerAccountSchema
                            .join(
                                otherTable = AccountSchema,
                                otherColumn = AccountSchema.id,
                                joinType = JoinType.INNER,
                                onColumn = PlayerAccountSchema.id
                            )
                            .select(PlayerAccountSchema.playerUuid, AccountSchema.name)
                            .associate {
                                val uuid = bytesToUuid(it[PlayerAccountSchema.playerUuid])
                                val name = it[AccountSchema.name] ?: ""
                                return@associate uuid to name
                            }
                    )
            }
        }
    }

}