package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.local

import io.github.arcaneplugins.polyconomy.api.account.AccountPermission
import io.github.arcaneplugins.polyconomy.api.account.AccountTransaction
import io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.account.TransactionType
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import io.github.arcaneplugins.polyconomy.api.util.cause.CauseType
import io.github.arcaneplugins.polyconomy.api.util.cause.NonPlayerCause
import io.github.arcaneplugins.polyconomy.api.util.cause.PlayerCause
import io.github.arcaneplugins.polyconomy.api.util.cause.PluginCause
import io.github.arcaneplugins.polyconomy.api.util.cause.ServerCause
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.STORAGE_YAML
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.StorageHandler
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.math.BigDecimal
import java.nio.file.Path
import java.text.DecimalFormat
import java.time.Instant
import java.time.temporal.Temporal
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists

class YamlStorageHandler(
    val plugin: Polyconomy,
) : StorageHandler("yaml") {

    private val relativePath: Path = Path("data${File.separator}data.yml")

    private val loader: YamlConfigurationLoader = YamlConfigurationLoader.builder()
        .path(absolutePath())
        .build()

    private lateinit var rootNode: CommentedConfigurationNode

    private val currencyCache = mutableSetOf<Currency>()
    private lateinit var primaryCurrency: Currency

    private fun read() {
        plugin.debugLog(STORAGE_YAML) { "Reading data." }
        plugin.debugLog(STORAGE_YAML) { "Absolute path: ${absolutePath()}" }
        createIfNotExists()
        rootNode = loader.load()
        plugin.debugLog(STORAGE_YAML) { "Read data." }
    }

    private fun write() {
        plugin.debugLog(STORAGE_YAML) { "Writing data." }
        loader.save(rootNode)
        plugin.debugLog(STORAGE_YAML) { "Written data." }
    }

    private fun absolutePath(): Path {
        return Path(
            "${plugin.dataFolder.absolutePath}${File.separator}${relativePath}"
        )
    }

    private fun createIfNotExists() {
        val exists: Boolean = absolutePath().exists()
        plugin.debugLog(STORAGE_YAML) { "Data file exists: ${if (exists) "Yes" else "No"}" }
        if (exists) return

        plugin.debugLog(STORAGE_YAML) { "File doesn't exist; creating." }
        absolutePath().parent.createDirectories()
        absolutePath().createFile()
        plugin.debugLog(STORAGE_YAML) { "File created." }
    }

    override fun connect() {
        plugin.debugLog(STORAGE_YAML) { "Connecting." }

        if (connected)
            throw IllegalStateException("Attempted to connect whilst already connected")

        plugin.debugLog(STORAGE_YAML) { "Checking if file has not been created yet." }
        createIfNotExists()
        plugin.debugLog(STORAGE_YAML) { "File present; continuing." }

        plugin.debugLog(STORAGE_YAML) { "Initialising root node: reading data." }
        read()
        plugin.debugLog(STORAGE_YAML) { "Initialised root node." }

        plugin.debugLog(STORAGE_YAML) { "Caching currencies." }
        currencyCache.clear()
        for (currencyNode in rootNode.node("currency").childrenList()) {
            val currencyId = currencyNode.key().toString().lowercase(Locale.ROOT)

            plugin.debugLog(STORAGE_YAML) { "Found currency ${currencyId}." }

            if (!currencyNode.node("enabled").boolean) {
                plugin.debugLog(STORAGE_YAML) { "Currency ${currencyId} is not enabled, skipping." }
                continue
            }

            currencyCache.add(
                CurrencyImpl(currencyId, this)
            )
        }
        val primaryCurrencyId = rootNode.node("primary-currency").string!!
        primaryCurrency = currencyCache.first { it.name == primaryCurrencyId }
        plugin.debugLog(STORAGE_YAML) { "Cached currencies." }

        connected = true
        plugin.debugLog(STORAGE_YAML) { "Connected." }
    }

    override fun disconnect() {
        plugin.debugLog(STORAGE_YAML) { "Disconnecting." }

        if (!connected) {
            plugin.debugLog(STORAGE_YAML) { "Attempted to disconnect, but is already disconnected." }
            return
        }

        /*
        YAML does not need any disconnect behaviour. The underlying libraries handle the file
        connection being closed with the operating system.
         */
        connected = false

        plugin.debugLog(STORAGE_YAML) { "Disconnected." }
    }

    override suspend fun getOrCreatePlayerAccount(uuid: UUID, name: String?): PlayerAccount {
        val account = PlayerAccountImpl(uuid, this)
        val accountNode = rootNode.node("account", "player", uuid.toString())
        if (!accountNode.virtual()) {
            return account
        }

        account.setName(name)
        account.resetBalance(getPrimaryCurrency(), ServerCause, TransactionImportance.HIGH, "Account creation")

        return account
    }

    override suspend fun getOrCreateNonPlayerAccount(namespacedKey: NamespacedKey, name: String?): NonPlayerAccount {
        val account = NonPlayerAccountImpl(namespacedKey, this)
        val accountNode = rootNode.node("account", "non-player", namespacedKey.namespace, namespacedKey.key)
        if (!accountNode.virtual()) {
            return account
        }

        account.setName(name)
        account.resetBalance(getPrimaryCurrency(), ServerCause, TransactionImportance.HIGH, "Account creation")

        return account
    }

    override suspend fun getPlayerAccountIds(): Collection<UUID> {
        return rootNode.node("account", "player")
            .childrenMap()
            .keys
            .map { UUID.fromString(it.toString()) }
            .toSet()
    }

    override suspend fun getNonPlayerAccountIds(): Collection<NamespacedKey> {
        return rootNode.node("account", "non-player")
            .childrenMap()
            .flatMap { (namespace, namespaceNode) ->
                namespaceNode.childrenMap().keys.map { key ->
                    NamespacedKey(namespace.toString(), key.toString())
                }
            }
    }

    override suspend fun getNonPlayerAccountsPlayerIsMemberof(uuid: UUID): Collection<NonPlayerAccount> {
        val list = mutableListOf<NonPlayerAccount>()

        for (namespaceNode in rootNode.node("account", "non-player").childrenList()) {
            for (keyNode in namespaceNode.childrenList()) {
                if (keyNode.node("member").hasChild(uuid.toString())) {
                    list.add(
                        NonPlayerAccountImpl(
                            NamespacedKey(
                                namespaceNode.key().toString(),
                                keyNode.key().toString()
                            ),
                            this
                        )
                    )
                }
            }
        }

        return list
    }

    override suspend fun getPrimaryCurrency(): Currency {
        return primaryCurrency
    }

    override suspend fun getCurrency(name: String): Currency {
        return currencyCache.first { it.name == name }
    }

    fun hasCurrency(name: String): Boolean {
        return currencyCache.firstOrNull { it.name == name } != null
    }

    override suspend fun getCurrencies(): Collection<Currency> {
        return currencyCache
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
        val currenciesNode = rootNode.node("currency")

        if (currenciesNode.hasChild(name)) {
            throw IllegalArgumentException("Currency by the name of '${name}' already exists")
        }

        val currencyNode = currenciesNode.node(name)

        currencyNode.node("name").set(name)
        currenciesNode.node("starting-balance").set(startingBalance.toDouble())
        currenciesNode.node("amount-format").set(amountFormat)
        currenciesNode.node("presentation-format").set(presentationFormat)
        currenciesNode.node("conversion-rate").set(conversionRate.toDouble())
        displayNameSingularLocaleMap.forEach { (loc, str) ->
            currenciesNode.node("locale", loc.toLanguageTag(), "display-name", "singular").set(str)
        }
        displayNamePluralLocaleMap.forEach { (loc, str) ->
            currenciesNode.node("locale", loc.toLanguageTag(), "display-name", "plural").set(str)
        }
        decimalLocaleMap.forEach { (loc, str) ->
            currenciesNode.node("locale", loc.toLanguageTag(), "decimal").set(str)
        }
        write()

        val currency = CurrencyImpl(name, this)

        currencyCache.add(currency)

        return currency
    }

    override suspend fun unregisterCurrency(currency: Currency) {
        if (!currencyCache.contains(currency)) {
            throw IllegalArgumentException("Currency ${currency.name} is not registered")
        }
        // note: this storage handler doesn't cascade currency deletions onto transactions,
        // so there will be transactions that reference an invalid currency. however, when loading currencies,
        // the storage handler ignores entries with invalid currencies.
        currencyCache.remove(currency)
        rootNode.node("currency").removeChild(currency.name.lowercase(Locale.ROOT))
        write()
    }

    override suspend fun hasPlayerAccount(uuid: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun hasNonPlayerAccount(nsKey: NamespacedKey): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun isVaultBankOwner(bankId: NamespacedKey, memberId: NamespacedKey): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun isVaultBankOwner(bankId: NamespacedKey, memberId: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun isVaultBankMember(bankId: NamespacedKey, memberId: NamespacedKey): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun isVaultBankMember(bankId: NamespacedKey, memberId: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getVaultBankAccountIds(): Collection<NamespacedKey> {
        TODO("Not yet implemented")
    }

    override suspend fun setVaultBankOwner(bankId: NamespacedKey, ownerId: NamespacedKey) {
        TODO("Not yet implemented")
    }

    override suspend fun setVaultBankOwner(bankId: NamespacedKey, ownerId: UUID) {
        TODO("Not yet implemented")
    }

    companion object {
        private class PlayerAccountImpl(
            uuid: UUID,
            val storageHandler: YamlStorageHandler,
        ) : PlayerAccount(uuid) {
            private fun accountNode(): CommentedConfigurationNode {
                return storageHandler.rootNode.node("account", "player", uuid.toString())
            }

            override suspend fun getName(): String? {
                return accountNode().node("name").string
            }

            override suspend fun setName(newName: String?) {
                accountNode().node("name").set(newName)
                storageHandler.write()
            }

            override suspend fun getBalance(currency: Currency): BigDecimal {
                return BigDecimal.valueOf(
                    accountNode().node("balance", currency.name).double
                )
            }

            override suspend fun makeTransaction(transaction: AccountTransaction) {
                val oldBalance: BigDecimal = getBalance(transaction.currency)
                val newBalance: BigDecimal = when (transaction.type) {
                    TransactionType.SET -> transaction.amount
                    TransactionType.RESET -> transaction.currency.getStartingBalance()
                    TransactionType.WITHDRAW -> oldBalance - transaction.amount
                    TransactionType.DEPOSIT -> oldBalance + transaction.amount
                }

                // set new balance
                accountNode().node("balance", transaction.currency.name).set(newBalance.toDouble())

                // set transaction history
                val transactionNextId: Int = accountNode().node("transaction", "next-id").getInt(0)
                val transactionNode = accountNode().node("transaction", transactionNextId)
                with (transactionNode) {
                    node("amount").set(transaction.amount.toDouble())
                    node("currency").set(transaction.currency.name)
                    node("cause", "type").set(transaction.cause.type.name)
                    node("cause", "data").set(transaction.cause.data.toString())
                    node("reason").set(transaction.reason)
                    node("importance").set(transaction.importance.name)
                    node("type").set(transaction.type.name)
                    node("timestamp").set(transaction.timestamp.epochSecond)
                }

                // write changes to disk
                storageHandler.write()
            }

            override suspend fun deleteAccount() {
                storageHandler.rootNode.node("account", "player").removeChild(uuid.toString())
                storageHandler.write()
            }

            override suspend fun getHeldCurrencies(): Collection<Currency> {
                return accountNode()
                    .node("balance")
                    .childrenList()
                    .map { storageHandler.getCurrency(it.key().toString()) }
            }

            override suspend fun getTransactionHistory(
                maxCount: Int,
                dateFrom: Temporal,
                dateTo: Temporal,
            ): List<AccountTransaction> {
                val dateFromEpoch = Instant.from(dateFrom).epochSecond
                val dateToEpoch = Instant.from(dateTo).epochSecond

                return accountNode()
                    .node("transaction")
                    .childrenList()
                    .filter {
                        // only get transaction IDs here, as `next-id` key is a str key, skip.
                        it.key() is Int
                    }
                    .filter {
                        // skip invalid currency
                        storageHandler.hasCurrency(it.node("currency").string!!)
                    }
                    .filter {
                        // make sure it's within the timeframe search
                        it.node("timestamp").long in dateFromEpoch..dateToEpoch
                    }
                    .map {
                        val causeData = it.node("cause", "data").string!!

                        AccountTransaction(
                            amount = BigDecimal.valueOf(it.node("amount").double),
                            currency = storageHandler.getCurrency(it.node("currency").string!!),
                            cause = when (CauseType.valueOf(it.node("cause", "type").string!!)) {
                                CauseType.PLAYER -> PlayerCause(uuid = UUID.fromString(causeData))
                                CauseType.NON_PLAYER -> NonPlayerCause(namespacedKey = NamespacedKey(causeData))
                                CauseType.PLUGIN -> PluginCause(namespacedKey = NamespacedKey(causeData))
                                CauseType.SERVER -> ServerCause
                            },
                            importance = TransactionImportance.valueOf(it.node("importance").string!!),
                            timestamp = Instant.ofEpochSecond(it.node("timestamp").long),
                            type = TransactionType.valueOf(it.node("type").string!!),
                            reason = it.node("reason").string
                        )
                    }
            }

            override suspend fun getMemberIds(): Collection<UUID> {
                return Collections.singletonList(uuid)
            }

            override suspend fun isMember(player: UUID): Boolean {
                return player == uuid
            }

            override suspend fun setPermissions(player: UUID, perms: Map<AccountPermission, Boolean?>) {
                throw IllegalStateException("Unable to set permissions on a player account")
            }

            override suspend fun getPermissions(player: UUID): Map<AccountPermission, Boolean?> {
                return if (player == uuid) {
                    AccountPermission.entries.associateWith { true }
                } else {
                    emptyMap()
                }
            }

            override suspend fun getPermissionsMap(): Map<UUID, Map<AccountPermission, Boolean?>> {
                return mapOf(uuid to getPermissions(uuid))
            }

            override suspend fun hasPermissions(player: UUID, permissions: Collection<AccountPermission>): Boolean {
                return player == uuid
            }

        }

        private class NonPlayerAccountImpl(
            namespacedKey: NamespacedKey,
            val storageHandler: YamlStorageHandler,
        ) : NonPlayerAccount(namespacedKey) {
            private fun accountNode(): CommentedConfigurationNode {
                return storageHandler
                    .rootNode
                    .node("account", "non-player", namespacedKey.namespace, namespacedKey.key)
            }

            override suspend fun getName(): String? {
                return accountNode().node("name").string
            }

            override suspend fun setName(newName: String?) {
                accountNode().node("name").set(newName)
                storageHandler.write()
            }

            override suspend fun getBalance(currency: Currency): BigDecimal {
                return BigDecimal.valueOf(
                    accountNode().node("balance", currency.name).double
                )
            }

            override suspend fun makeTransaction(transaction: AccountTransaction) {
                val oldBalance: BigDecimal = getBalance(transaction.currency)
                val newBalance: BigDecimal = when (transaction.type) {
                    TransactionType.SET -> transaction.amount
                    TransactionType.RESET -> transaction.currency.getStartingBalance()
                    TransactionType.WITHDRAW -> oldBalance - transaction.amount
                    TransactionType.DEPOSIT -> oldBalance + transaction.amount
                }

                // set new balance
                accountNode().node("balance", transaction.currency.name).set(newBalance.toDouble())

                // set transaction history
                val transactionNextId: Int = accountNode().node("transaction", "next-id").getInt(0)
                val transactionNode = accountNode().node("transaction", transactionNextId)
                with (transactionNode) {
                    node("amount").set(transaction.amount.toDouble())
                    node("currency").set(transaction.currency.name)
                    node("cause", "type").set(transaction.cause.type.name)
                    node("cause", "data").set(transaction.cause.data.toString())
                    node("reason").set(transaction.reason)
                    node("importance").set(transaction.importance.name)
                    node("type").set(transaction.type.name)
                    node("timestamp").set(transaction.timestamp.epochSecond)
                }

                // write changes to disk
                storageHandler.write()
            }

            override suspend fun deleteAccount() {
                storageHandler.rootNode.node("account", "player").removeChild(namespacedKey.toString())
                storageHandler.write()
            }

            override suspend fun getHeldCurrencies(): Collection<Currency> {
                return accountNode()
                    .node("balance")
                    .childrenList()
                    .map { storageHandler.getCurrency(it.key().toString()) }
            }

            override suspend fun getTransactionHistory(
                maxCount: Int,
                dateFrom: Temporal,
                dateTo: Temporal,
            ): List<AccountTransaction> {
                val dateFromEpoch = Instant.from(dateFrom).epochSecond
                val dateToEpoch = Instant.from(dateTo).epochSecond

                return accountNode()
                    .node("transaction")
                    .childrenList()
                    .filter {
                        // only get transaction IDs here, as `next-id` key is a str key, skip.
                        it.key() is Int
                    }
                    .filter {
                        // skip invalid currency
                        storageHandler.hasCurrency(it.node("currency").string!!)
                    }
                    .filter {
                        // make sure it's within the timeframe search
                        it.node("timestamp").long in dateFromEpoch..dateToEpoch
                    }
                    .map {
                        val causeData = it.node("cause", "data").string!!

                        AccountTransaction(
                            amount = BigDecimal.valueOf(it.node("amount").double),
                            currency = storageHandler.getCurrency(it.node("currency").string!!),
                            cause = when (CauseType.valueOf(it.node("cause", "type").string!!)) {
                                CauseType.PLAYER -> PlayerCause(uuid = UUID.fromString(causeData))
                                CauseType.NON_PLAYER -> NonPlayerCause(namespacedKey = NamespacedKey(causeData))
                                CauseType.PLUGIN -> PluginCause(namespacedKey = NamespacedKey(causeData))
                                CauseType.SERVER -> ServerCause
                            },
                            importance = TransactionImportance.valueOf(it.node("importance").string!!),
                            timestamp = Instant.ofEpochSecond(it.node("timestamp").long),
                            type = TransactionType.valueOf(it.node("type").string!!),
                            reason = it.node("reason").string
                        )
                    }
            }

            override suspend fun getMemberIds(): Set<UUID> {
                TODO("Not yet implemented")
            }

            override suspend fun isMember(player: UUID): Boolean {
                TODO("Not yet implemented")
            }

            override suspend fun setPermissions(player: UUID, perms: Map<AccountPermission, Boolean?>) {
                TODO("Not yet implemented")
            }

            override suspend fun getPermissions(player: UUID): Map<AccountPermission, Boolean?> {
                TODO("Not yet implemented")
            }

            override suspend fun getPermissionsMap(): Map<UUID, Map<AccountPermission, Boolean?>> {
                TODO("Not yet implemented")
            }

            override suspend fun hasPermissions(player: UUID, permissions: Collection<AccountPermission>): Boolean {
                TODO("Not yet implemented")
            }

        }

        private class CurrencyImpl(
            name: String,
            val storageHandler: YamlStorageHandler,
        ) : Currency(name) {
            private fun currencyNode(): CommentedConfigurationNode {
                return storageHandler.rootNode.node("currency", name)
            }

            override suspend fun getSymbol(): String {
                return currencyNode().node("symbol").string!!
            }

            override suspend fun getDecimal(locale: Locale): String {
                return currencyNode().node("locale", locale.toLanguageTag(), "decimal").string!!
            }

            override suspend fun getLocaleDecimalMap(): Map<Locale, String> {
                return currencyNode()
                    .node("locale")
                    .childrenMap()
                    .mapKeys { Locale.forLanguageTag(it.toString()) }
                    .mapValues { it.value.node("decimal").string!! }
            }

            override suspend fun getDisplayName(plural: Boolean, locale: Locale): String {
                return currencyNode()
                    .node("locale", locale.toLanguageTag(), "display-name")
                    .node(
                        if (plural) {
                            "plural"
                        } else {
                            "singular"
                        }
                    )
                    .string!!
            }

            override suspend fun isPrimary(): Boolean {
                return storageHandler.primaryCurrency.name == name
            }

            override suspend fun getStartingBalance(): BigDecimal {
                return BigDecimal.valueOf(
                    currencyNode()
                        .node("starting-balance")
                        .double
                )
            }

            override suspend fun getConversionRate(): BigDecimal {
                return BigDecimal.valueOf(
                    currencyNode()
                        .node("conversion-rate")
                        .double
                )
            }

            override suspend fun format(amount: BigDecimal, locale: Locale): String {
                val amountFormat = currencyNode().node("amount-format").string!!
                val presentationFormat = currencyNode().node("presentation-format").string!!

                return presentationFormat
                    .replace("symbol", getSymbol())
                    .replace("amount", DecimalFormat(amountFormat).format(amount))
                    .replace("%display-name%", getDisplayName(amount.compareTo(BigDecimal.ONE) == 0, locale))
            }

        }
    }

}