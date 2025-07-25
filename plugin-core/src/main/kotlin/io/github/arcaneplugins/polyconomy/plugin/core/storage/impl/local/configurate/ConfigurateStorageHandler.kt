package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.configurate

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
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageHandler
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.core.util.KeyStore.VU_NAMESPACE_FOR_SHARED_ACCOUNTS
import io.github.arcaneplugins.polyconomy.plugin.core.util.KeyStore.VU_NAMESPACE_FOR_STANDARD_ACCOUNTS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.spongepowered.configurate.ScopedConfigurationNode
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.transformation.ConfigurationTransformation
import org.spongepowered.configurate.transformation.TransformAction
import java.math.BigDecimal
import java.nio.file.Path
import java.text.DecimalFormat
import java.time.Instant
import java.time.temporal.Temporal
import java.util.*
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists

abstract class ConfigurateStorageHandler(
    val absolutePath: Path,
    manager: StorageManager,
    id: String,
) : StorageHandler(id, manager) {

    private lateinit var rootNode: ScopedConfigurationNode<*>

    private val currencyCache = mutableSetOf<Currency>()
    private lateinit var primaryCurrency: Currency

    protected val loader: AbstractConfigurationLoader<out ScopedConfigurationNode<*>> by lazy {
        buildLoader()
    }

    protected abstract fun buildLoader(): AbstractConfigurationLoader<out ScopedConfigurationNode<*>>

    private fun read() {
        createIfNotExists()
        rootNode = loader.load()
    }

    private fun write() {
        loader.save(rootNode)
    }

    private fun createIfNotExists() {
        val exists: Boolean = absolutePath.exists()
        if (exists) return

        absolutePath.createParentDirectories()
        absolutePath.createFile()
    }

    override fun startup() {

        if (connected)
            throw IllegalStateException("Attempted to connect whilst already connected")

        createIfNotExists()

        read()

        if (rootNode.node("currency").virtual()
            || rootNode.node("currency").childrenList().isEmpty()
        ) {
            val currencyNode = rootNode.node("currency", "dollar")

            with(currencyNode) {
                node("enabled").set(true)
                node("starting-balance").set(50.0)
                node("symbol").set("$")
                node("amount-format").set("#,##0.00")
                node("presentation-format").set("%symbol%%amount%")
                node("conversion-rate").set(1)
                node("locale", "en_US", "display-name", "singular").set("Dollar")
                node("locale", "en_US", "display-name", "plural").set("Dollars")
                node("locale", "en_US", "decimal").set(".")
            }

            write()
        }

        currencyCache.clear()
        for ((currencyIdUnfmt, currencyNode) in rootNode.node("currency").childrenMap()) {
            val currencyId = currencyIdUnfmt.toString().lowercase(Locale.ROOT)

            if (!currencyNode.node("enabled").getBoolean(false)) {
                continue
            }

            currencyCache.add(
                CurrencyImpl(currencyId, this)
            )
        }

        if (currencyCache.isEmpty()) {
            throw java.lang.IllegalStateException("You have no currencies configured!")
        }

        val primaryCurrencyId = manager.primaryCurrencyId()
        primaryCurrency = currencyCache.find { it.name == primaryCurrencyId }
            ?: throw IllegalArgumentException("The primary currency ID you have specified (${primaryCurrencyId}) does not match any valid and enabled currency (${currencyCache.size} candidates)")

        connected = true
    }

    override fun shutdown() {
        if (!connected) {
            return
        }

        /*
        Configurate storage handlers don't need any disconnect behaviour. The underlying libraries handle the file
        connection being closed with the operating system.
         */
        connected = false
    }

    override suspend fun playerCacheGetName(uuid: UUID): String? {
        return rootNode.node("player-cache", uuid.toString()).string
    }

    override suspend fun playerCacheSetName(uuid: UUID, name: String) {
        rootNode.node("player-cache", uuid.toString()).set(name)
        write()
    }

    override suspend fun playerCacheIsPlayer(uuid: UUID): Boolean {
        return !rootNode.node("player-cache", uuid.toString()).virtual()
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

    override suspend fun getNonPlayerAccountsPlayerIsMemberOf(uuid: UUID): Collection<NonPlayerAccount> {
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

    override suspend fun hasCurrency(name: String): Boolean {
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
        return !rootNode.node("account", "player", uuid.toString()).virtual()
    }

    override suspend fun hasNonPlayerAccount(nsKey: NamespacedKey): Boolean {
        return !rootNode.node("account", "non-player", nsKey.namespace, nsKey.key).virtual()
    }

    override suspend fun getVaultBankAccountIds(): Collection<NamespacedKey> {
        return getNonPlayerAccountIds()
            .filter { getOrCreateNonPlayerAccount(it, null).isVaultBankAccount() }
    }

    override suspend fun getVaultUnlockedUuidNameMap(): Map<UUID, String> {
        return getNonPlayerAccountIds()
            .filter {
                it.namespace == VU_NAMESPACE_FOR_STANDARD_ACCOUNTS ||
                        it.namespace == VU_NAMESPACE_FOR_SHARED_ACCOUNTS
            }
            .map { getOrCreateNonPlayerAccount(it, null) }
            .associate { UUID.fromString(it.namespacedKey.key) to (it.getName() ?: "") }
            .plus(
                getPlayerAccountIds()
                    .associateWith {
                        getOrCreatePlayerAccount(it, null).getName() ?: playerCacheGetName(it) ?: ""
                    }
            )
    }

    companion object {
        private class PlayerAccountImpl(
            uuid: UUID,
            val storageHandler: ConfigurateStorageHandler,
        ) : PlayerAccount(uuid) {
            private fun accountNode(): ScopedConfigurationNode<*> {
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
                val newBalance: BigDecimal = let {
                    val value = transaction.resultingBalance(oldBalance)
                    val minBal = storageHandler.manager.minimumBalance()

                    return@let if (value < minBal) {
                        throw IllegalArgumentException(
                            "A ${transaction.type} transaction of ${transaction.amount} would evaluate to a balance " +
                                    "of ${value}, which is below the minimum allowed balance of ${minBal}"
                        )
                    } else {
                        value
                    }
                }

                // set new balance
                accountNode().node("balance", transaction.currency.name).set(newBalance.toDouble())

                // set transaction history
                val transactionNextIdNode = accountNode().node("transaction", "next-id")
                if (transactionNextIdNode.virtual()) {
                    transactionNextIdNode.set(0)
                }
                val transactionNextId = accountNode().node("transaction", "next-id").int
                val transactionNode = accountNode().node("transaction", transactionNextId)
                with(transactionNode) {
                    node("amount").set(transaction.amount.toDouble())
                    node("currency").set(transaction.currency.name)
                    node("cause", "type").set(transaction.cause.type.name)
                    node("cause", "data").set(transaction.cause.data.toString())
                    node("reason").set(transaction.reason)
                    node("importance").set(transaction.importance.name)
                    node("type").set(transaction.type.name)
                    node("timestamp").set(transaction.timestamp.epochSecond)
                }
                transactionNextIdNode.set(transactionNextId + 1)

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
                    .childrenMap()
                    .map { storageHandler.getCurrency(it.key.toString()) }
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
                    .childrenMap()
                    .filter {
                        // only get transaction IDs here, as `next-id` key is a str key, skip.
                        it.key != "next-id"
                    }
                    .filter {
                        // skip invalid currency
                        storageHandler.hasCurrency(it.value.node("currency").string!!)
                    }
                    .filter {
                        // make sure it's within the timeframe search
                        it.value.node("timestamp").long in dateFromEpoch..dateToEpoch
                    }
                    .map {
                        val causeData = it.value.node("cause", "data").string!!

                        AccountTransaction(
                            amount = BigDecimal.valueOf(it.value.node("amount").double),
                            currency = storageHandler.getCurrency(it.value.node("currency").string!!),
                            cause = when (CauseType.valueOf(it.value.node("cause", "type").string!!)) {
                                CauseType.PLAYER -> PlayerCause(uuid = UUID.fromString(causeData))
                                CauseType.NON_PLAYER -> NonPlayerCause(NamespacedKey.fromString(causeData))
                                CauseType.PLUGIN -> PluginCause(NamespacedKey.fromString(causeData))
                                CauseType.SERVER -> ServerCause
                            },
                            importance = TransactionImportance.valueOf(it.value.node("importance").string!!),
                            timestamp = Instant.ofEpochSecond(it.value.node("timestamp").long),
                            type = TransactionType.valueOf(it.value.node("type").string!!),
                            reason = it.value.node("reason").string
                        )
                    }
            }

        }

        private class NonPlayerAccountImpl(
            namespacedKey: NamespacedKey,
            val storageHandler: ConfigurateStorageHandler,
        ) : NonPlayerAccount(namespacedKey) {
            private fun accountNode(): ScopedConfigurationNode<*> {
                return storageHandler
                    .rootNode
                    .node("account", "non-player", namespacedKey.namespace, namespacedKey.key)
            }

            override suspend fun isVaultBankAccount(): Boolean {
                return accountNode().hasChild("vault-bank")
            }

            @Suppress("OVERRIDE_DEPRECATION")
            override suspend fun isLegacyVaultBankOwner(memberId: NamespacedKey): Boolean {
                val node = accountNode().node("vault-bank", "owner-string")
                return !node.virtual() && node.string == memberId.toString()
            }

            override suspend fun isVaultBankOwner(memberId: UUID): Boolean {
                val node = accountNode().node("vault-bank", "owner-uuid")
                return !node.virtual() && node.string == memberId.toString()
            }

            @Suppress("OVERRIDE_DEPRECATION")
            override suspend fun isLegacyVaultBankMember(memberId: NamespacedKey): Boolean {
                return accountNode()
                    .node("vault-bank", "legacy-member")
                    .getList(String::class.java)
                    ?.contains(memberId.toString())
                    ?: false
            }

            override suspend fun setLegacyVaultBankOwner(ownerId: NamespacedKey) {
                accountNode().node("vault-bank", "owner-string").set(ownerId.toString())
                storageHandler.write()
            }

            override suspend fun setVaultBankOwner(ownerId: UUID) {
                accountNode().node("vault-bank", "owner-uuid").set(ownerId.toString())
                storageHandler.write()
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
                val transactionNextIdNode = accountNode().node("transaction", "next-id")
                if (transactionNextIdNode.virtual()) {
                    transactionNextIdNode.set(0)
                }
                val transactionNextId = accountNode().node("transaction", "next-id").int
                val transactionNode = accountNode().node("transaction", transactionNextId)
                with(transactionNode) {
                    node("amount").set(transaction.amount.toDouble())
                    node("currency").set(transaction.currency.name)
                    node("cause", "type").set(transaction.cause.type.name)
                    node("cause", "data").set(transaction.cause.data.toString())
                    node("reason").set(transaction.reason)
                    node("importance").set(transaction.importance.name)
                    node("type").set(transaction.type.name)
                    node("timestamp").set(transaction.timestamp.epochSecond)
                }
                transactionNextIdNode.set(transactionNextId + 1)

                // write changes to disk
                storageHandler.write()
            }

            override suspend fun deleteAccount() {
                val accountsNode = storageHandler.rootNode.node("account", "non-player")
                val namespaceNode = storageHandler.rootNode.node("account", "non-player", namespacedKey.namespace)
                namespaceNode.removeChild(namespacedKey.key)
                if (namespaceNode.childrenMap().isEmpty()) {
                    accountsNode.removeChild(namespacedKey.namespace)
                }
                storageHandler.write()
            }

            override suspend fun getHeldCurrencies(): Collection<Currency> {
                return accountNode()
                    .node("balance")
                    .childrenMap()
                    .map { storageHandler.getCurrency(it.key.toString()) }
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
                    .childrenMap()
                    .filter {
                        // only get transaction IDs here, as `next-id` key is a str key, skip.
                        it.key is Int
                    }
                    .filter {
                        // skip invalid currency
                        storageHandler.hasCurrency(it.value.node("currency").string!!)
                    }
                    .filter {
                        // make sure it's within the timeframe search
                        it.value.node("timestamp").long in dateFromEpoch..dateToEpoch
                    }
                    .map {
                        val causeData = it.value.node("cause", "data").string!!

                        AccountTransaction(
                            amount = BigDecimal.valueOf(it.value.node("amount").double),
                            currency = storageHandler.getCurrency(it.value.node("currency").string!!),
                            cause = when (CauseType.valueOf(it.value.node("cause", "type").string!!)) {
                                CauseType.PLAYER -> PlayerCause(uuid = UUID.fromString(causeData))
                                CauseType.NON_PLAYER -> NonPlayerCause(NamespacedKey.fromString(causeData))
                                CauseType.PLUGIN -> PluginCause(NamespacedKey.fromString(causeData))
                                CauseType.SERVER -> ServerCause
                            },
                            importance = TransactionImportance.valueOf(it.value.node("importance").string!!),
                            timestamp = Instant.ofEpochSecond(it.value.node("timestamp").long),
                            type = TransactionType.valueOf(it.value.node("type").string!!),
                            reason = it.value.node("reason").string
                        )
                    }
            }

            override suspend fun getMemberIds(): Collection<UUID> {
                return accountNode()
                    .node("member")
                    .childrenMap()
                    .map { UUID.fromString(it.key as String) }
            }

            override suspend fun isMember(player: UUID): Boolean {
                val memberNode = accountNode().node("member", player.toString())
                return !memberNode.virtual() && memberNode.childrenMap().all { it.value.boolean }
            }

            override suspend fun setPermissions(player: UUID, perms: Map<AccountPermission, Boolean?>) {
                val permsNode = accountNode().node("member", player.toString(), "permission")
                perms.forEach { (perm, state) ->
                    permsNode.node(perm.name).set(state)
                }
                storageHandler.write()
            }

            override suspend fun getPermissions(player: UUID): Map<AccountPermission, Boolean?> {
                if (!isMember(player)) {
                    return AccountPermission.entries.associateWith { false }
                }

                val permsNode = accountNode().node("member", player.toString(), "permission")
                return AccountPermission.entries.associate {
                    val permNode = permsNode.node(it.name)

                    if (permNode.virtual()) {
                        it to null
                    } else {
                        it to permNode.boolean
                    }
                }
            }

            override suspend fun getPermissionsMap(): Map<UUID, Map<AccountPermission, Boolean?>> {
                return getMemberIds().associateWith { getPermissions(it) }
            }

            override suspend fun hasPermissions(player: UUID, permissions: Collection<AccountPermission>): Boolean {
                if (!isMember(player)) {
                    return false
                }

                val permsNode = accountNode().node("member", player.toString(), "permission")

                return permissions.all {
                    val permNode = permsNode.node(it.name)

                    if (permNode.virtual()) {
                        it.defaultValue
                    } else {
                        permNode.boolean
                    }
                }
            }

            override suspend fun addMember(player: UUID) {
                setPermissions(player, AccountPermission.entries.associateWith { null })
                storageHandler.write()
            }

            override suspend fun removeMember(player: UUID) {
                accountNode().node("member").removeChild(player.toString())
                storageHandler.write()
            }

        }

        private class CurrencyImpl(
            name: String,
            val storageHandler: ConfigurateStorageHandler,
        ) : Currency(name) {
            private fun currencyNode(): ScopedConfigurationNode<*> {
                return storageHandler.rootNode.node("currency", name)
            }

            override suspend fun getSymbol(): String {
                return currencyNode().node("symbol").string!!
            }

            override suspend fun getDecimal(locale: Locale): String {
                val preferredNode = currencyNode().node("locale", locale.toLanguageTag(), "decimal")

                if (!preferredNode.virtual()) {
                    return preferredNode.string!!
                }

                return currencyNode()
                    .node("locale")
                    .childrenMap()
                    .values
                    .firstOrNull()
                    ?.node("decimal")
                    ?.getString("")
                    ?: ""
            }

            override suspend fun getLocaleDecimalMap(): Map<Locale, String> {
                return currencyNode()
                    .node("locale")
                    .childrenMap()
                    .mapKeys { Locale.forLanguageTag(it.toString()) }
                    .mapValues { it.value.node("decimal").string!! }
            }

            override suspend fun getDisplayName(plural: Boolean, locale: Locale): String {
                val singularOrPlural = if (plural) {
                    "plural"
                } else {
                    "singular"
                }

                val preferredNode = currencyNode()
                    .node("locale", locale.toLanguageTag(), "display-name", singularOrPlural)

                if (!preferredNode.virtual()) {
                    return preferredNode.getString("")
                }

                return currencyNode()
                    .node("locale")
                    .childrenMap()
                    .values
                    .firstOrNull()
                    ?.node("display-name", singularOrPlural)
                    ?.getString("")
                    ?: ""
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
                    .replace("%symbol%", getSymbol())
                    .replace("%amount%", DecimalFormat(amountFormat).format(amount))
                    .replace("%display-name%", getDisplayName(amount.compareTo(BigDecimal.ONE) == 0, locale))
            }

            override suspend fun setName(new: String) {
                val oldName = name

                if (new == oldName) {
                    throw IllegalStateException("Currency already has the name ${new}; can't set to same value")
                }

                if (isPrimary()) {
                    throw IllegalStateException("Unable to set name of primary currency")
                }

                val transform = ConfigurationTransformation
                    .builder()
                    .addAction(currencyNode().path(), TransformAction.rename(new))
                    .build()
                transform.apply(currencyNode())
                name = new
                storageHandler.write()

                if (currencyNode().virtual()) {
                    throw IllegalStateException("Operation unsuccessful; new currency node is virtual/non-existant: ${currencyNode().path()}")
                }
            }

            override suspend fun setSymbol(new: String) {
                currencyNode()
                    .node("symbol")
                    .set(new)

                storageHandler.write()

                if (getSymbol() != new) {
                    throw IllegalStateException("Operation unsuccessful; values do not match: ${getSymbol()} != ${new}")
                }
            }

            override suspend fun setDisplayName(plural: Boolean, locale: Locale, new: String) {
                val singularOrPlural = if (plural) {
                    "plural"
                } else {
                    "singular"
                }

                currencyNode()
                    .node("locale", locale.toLanguageTag(), "display-name", singularOrPlural)
                    .set(new)

                storageHandler.write()

                if (getDisplayName(plural, locale) != new) {
                    throw IllegalStateException(
                        "Operation unsuccessful; values do not match: ${
                            getDisplayName(
                                plural,
                                locale
                            )
                        } != ${new}"
                    )
                }
            }

            override suspend fun setStartingBalance(new: BigDecimal) {
                currencyNode()
                    .node("starting-balance")
                    .set(new.toDouble())

                storageHandler.write()

                if (getStartingBalance().toDouble() != new.toDouble()) {
                    throw IllegalStateException("Operation unsuccessful; values do not match: ${getStartingBalance()} != ${new}")
                }
            }

            override suspend fun setDecimal(locale: Locale, new: String) {
                currencyNode()
                    .node("locale", locale.toLanguageTag(), "decimal")
                    .set(new)

                storageHandler.write()

                if (getDecimal(locale) != new) {
                    throw IllegalStateException("Operation unsuccessful; values do not match: ${getDecimal(locale)} != ${new}")
                }
            }

            override suspend fun setConversionRate(new: BigDecimal) {
                currencyNode()
                    .node("conversion-rate")
                    .set(new.toDouble())
                storageHandler.write()

                if (getConversionRate().toDouble() != new.toDouble()) {
                    throw IllegalStateException("Operation unsuccessful; values do not match: ${getConversionRate()} != ${new}")
                }
            }

            override suspend fun setAmountFormat(new: String) {
                currencyNode()
                    .node("amount-format")
                    .set(new)
                storageHandler.write()
            }

            override suspend fun setPresentationFormat(new: String) {
                currencyNode()
                    .node("presentation-format")
                    .set(new)

                // write
                storageHandler.write()
            }

            override suspend fun registerLocale(
                locale: Locale,
                dispNameSingular: String,
                dispNamePlural: String,
                decimal: String,
            ) {
                if (hasLocale(locale)) {
                    throw IllegalStateException("Locale $locale is already registered with currency $name")
                }
                setDisplayName(plural = false, locale, dispNameSingular)
                setDisplayName(plural = true, locale, dispNamePlural)
                setDecimal(locale, decimal)
            }

            override suspend fun unregisterLocale(locale: Locale) {
                if (!hasLocale(locale)) {
                    throw IllegalStateException("Locale $locale is not registered with currency $name")
                }
                currencyNode()
                    .node("locale")
                    .removeChild(locale.toLanguageTag())
                storageHandler.write()
            }

            override suspend fun hasLocale(locale: Locale): Boolean {
                return !currencyNode()
                    .node("locale", locale.toLanguageTag())
                    .virtual()
            }

        }
    }

    override suspend fun purgeOldTransactions() {
        val currentTimestamp = Instant.now()
        val minTimestampLow = currentTimestamp.minusSeconds(baseTransactionAgePeriod).epochSecond
        val minTimestampMed = currentTimestamp.minusSeconds(baseTransactionAgePeriod * 2).epochSecond

        /*
        Players
         */
        rootNode
            .node("account", "player")
            .childrenMap()
            .flatMap { (_, playerAccNode) ->
                return@flatMap playerAccNode.node("transaction").childrenMap()
                    .filter { (transactionKey, transactionNode) ->

                        if (transactionKey !is Int) {
                            return@filter false
                        }

                        val importance = TransactionImportance.valueOf(transactionNode.node("importance").string!!)
                        val timestamp = transactionNode.node("timestamp").long

                        return@filter when (importance) {
                            TransactionImportance.HIGH -> false
                            TransactionImportance.MEDIUM -> timestamp < minTimestampMed
                            TransactionImportance.LOW -> timestamp < minTimestampLow
                        }
                    }.values
            }.iterator().forEach { node ->
                node.parent()?.removeChild(node.key())
            }

        /*
        Non-Players
         */
        getNonPlayerAccountIds().forEach { nsKey ->
            rootNode
                .node("account", "non-player", nsKey.namespace, nsKey.key, "transaction")
                .childrenMap().filter { (_, transactionNode) ->

                    if (transactionNode.key() == "next-id") {
                        return@filter false
                    }

                    val importance = TransactionImportance.valueOf(transactionNode.node("importance").string!!)
                    val timestamp = transactionNode.node("timestamp").long

                    return@filter when (importance) {
                        TransactionImportance.HIGH -> false
                        TransactionImportance.MEDIUM -> timestamp < minTimestampMed
                        TransactionImportance.LOW -> timestamp < minTimestampLow
                    }
                }.values.iterator().forEach { node ->
                    node.parent()?.removeChild(node.key())
                }
        }

        write()
    }

    override suspend fun baltop(
        page: Int,
        pageSize: Int,
        currency: Currency,
    ): Map<String, BigDecimal> {
        return withContext(Dispatchers.IO) {
            val playerBalanceMap = sortedMapOf<String, BigDecimal>()

            for (uuid in getPlayerAccountIds()) {
                val playerAcc = getOrCreatePlayerAccount(uuid, name = null)
                val name = playerAcc.getName()
                val balance = playerAcc.getBalance(currency)

                if (name == null) {
                    continue // just skip accounts that have oddities e.g. no name or something
                }

                playerBalanceMap[name] = balance
            }

            return@withContext playerBalanceMap
                .entries
                .drop((page - 1) * pageSize)
                .take(pageSize)
                .associate { it.toPair() }
        }
    }

}