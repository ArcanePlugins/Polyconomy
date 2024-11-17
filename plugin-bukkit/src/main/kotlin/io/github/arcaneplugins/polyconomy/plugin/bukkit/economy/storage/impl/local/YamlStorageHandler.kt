package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.impl.local

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.STORAGE_YAML
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.account.PolyCause
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageHandler
import me.lokka30.treasury.api.common.Cause
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.event.EventBus
import me.lokka30.treasury.api.common.event.FireCompletion
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.common.response.TreasuryException
import me.lokka30.treasury.api.economy.account.AccountPermission
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.events.NonPlayerAccountTransactionEvent
import me.lokka30.treasury.api.economy.events.PlayerAccountTransactionEvent
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionImportance
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionType
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.math.BigDecimal
import java.nio.file.Path
import java.time.Instant
import java.util.*
import java.util.stream.Collectors
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists

class YamlStorageHandler(
    val plugin: Polyconomy,
) : StorageHandler("Yaml") {

    private val relativePath: Path = Path("data${File.separator}data.yml")

    private lateinit var loader: YamlConfigurationLoader

    private lateinit var rootNode: CommentedConfigurationNode

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

    private fun getAccountNodeSync(
        player: UUID,
    ): CommentedConfigurationNode {
        return rootNode.node("account", "player", player.toString())
    }

    private fun getAccountNodeSync(
        account: PlayerAccount,
    ): CommentedConfigurationNode {
        return getAccountNodeSync(account.identifier())
    }

    private fun getAccountNodeSync(
        id: NamespacedKey,
    ): CommentedConfigurationNode {
        return rootNode.node("account", "non-player", id.namespace, id.key)
    }

    private fun getAccountNodeSync(
        account: NonPlayerAccount,
    ): CommentedConfigurationNode {
        return getAccountNodeSync(account.identifier())
    }

    @Suppress("BooleanMethodIsAlwaysInverted")
    private fun holdsCurrencySync(
        account: PlayerAccount,
        currency: Currency,
    ): Boolean {
        return !getAccountNodeSync(account)
            .node("balance", getOrGrantCurrencyDbIdSync(currency))
            .virtual()
    }

    @Suppress("BooleanMethodIsAlwaysInverted")
    private fun holdsCurrencySync(
        account: NonPlayerAccount,
        currency: Currency,
    ): Boolean {
        return !getAccountNodeSync(account)
            .node("balance", getOrGrantCurrencyDbIdSync(currency))
            .virtual()
    }

    private fun getOrGrantCurrencyDbIdSync(
        currencyId: String,
    ): Int {
        return getCurrencyDbId(currencyId) ?: grantCurrencyDbId(currencyId)
    }

    private fun getOrGrantCurrencyDbIdSync(
        currency: Currency,
    ): Int {
        return getOrGrantCurrencyDbIdSync(currency.identifier)
    }

    private fun getCurrencyDbId(
        currencyId: String,
    ): Int? {
        val currencyDbIdNode = getCurrencyDbIdNode(currencyId)
        return if (currencyDbIdNode.virtual()) null else currencyDbIdNode.int
    }

    private fun grantCurrencyDbId(
        currencyId: String,
    ): Int {
        val currencyDbIdNode = getCurrencyDbIdNode(currencyId)
        val latestDbIdGrantedNode = rootNode.node("currency", "current-db-id")

        val latestDbIdGranted: Int = latestDbIdGrantedNode.getInt(0)
        val dbIdToGrant: Int = latestDbIdGranted + 1

        latestDbIdGrantedNode.set(dbIdToGrant)
        currencyDbIdNode.set(dbIdToGrant)
        write()

        return dbIdToGrant
    }

    private fun getCurrencyDbIdNode(
        currencyId: String,
    ): CommentedConfigurationNode {
        return rootNode.node("currency", "db-id-map", currencyId)
    }

    private fun getCurrencyByDbId(
        dbId: Int,
    ): Currency {
        return plugin.economyManager.currencies.first {
            getOrGrantCurrencyDbIdSync(it) == dbId
        }
    }

    override fun connect() {
        plugin.debugLog(STORAGE_YAML) { "Connecting." }

        if (connected)
            throw IllegalStateException("Attempted to connect whilst already connected")

        plugin.debugLog(STORAGE_YAML) { "Initialising loader." }
        loader = YamlConfigurationLoader.builder()
            .path(absolutePath())
            .build()
        plugin.debugLog(STORAGE_YAML) { "Initialised loader." }

        plugin.debugLog(STORAGE_YAML) { "Checking if file has not been created yet." }
        createIfNotExists()
        plugin.debugLog(STORAGE_YAML) { "File present; continuing." }

        plugin.debugLog(STORAGE_YAML) { "Initialising root node: reading data." }
        read()
        plugin.debugLog(STORAGE_YAML) { "Initialised root node." }

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

    override fun hasPlayerAccountSync(
        player: UUID,
    ): Boolean {
        return !getAccountNodeSync(player).virtual()
    }

    override fun hasNonPlayerAccountSync(
        id: NamespacedKey,
    ): Boolean {
        return !getAccountNodeSync(id).virtual()
    }

    override fun retrieveNameSync(
        account: PlayerAccount,
    ): Optional<String> {
        return Optional.ofNullable(
            getAccountNodeSync(account)
                .node("name")
                .string
        )
    }

    override fun retrieveNameSync(
        account: NonPlayerAccount,
    ): Optional<String> {
        return Optional.ofNullable(
            getAccountNodeSync(account)
                .node("name")
                .string
        )
    }

    override fun setNameSync(
        account: PlayerAccount,
        name: String?,
    ): Boolean {
        val previousName: Optional<String> = retrieveNameSync(account)

        if ((!previousName.isPresent && name == null) || (previousName.get() == name)) {
            return false
        }

        getAccountNodeSync(account)
            .node("name")
            .set(name)

        write()

        return true
    }

    override fun setNameSync(
        account: NonPlayerAccount,
        name: String?,
    ): Boolean {
        val previousName: Optional<String> = retrieveNameSync(account)

        if ((!previousName.isPresent && name == null) || (previousName.get() == name)) {
            return false
        }

        getAccountNodeSync(account)
            .node("name")
            .set(name)

        write()

        return true
    }

    override fun deleteAccountSync(
        account: PlayerAccount,
    ): Boolean {
        getAccountNodeSync(account)
            .set(null)

        write()

        return true
    }

    override fun deleteAccountSync(
        account: NonPlayerAccount,
    ): Boolean {
        getAccountNodeSync(account)
            .set(null)

        write()

        return true
    }

    override fun retrieveBalanceSync(
        account: PlayerAccount,
        currency: Currency,
    ): BigDecimal {
        val balanceNode = getAccountNodeSync(account).node("balance", getOrGrantCurrencyDbIdSync(currency))
        plugin.debugLog(STORAGE_YAML) { "balanceNode @ ${balanceNode.path()}" }

        if (balanceNode.virtual()) {
            plugin.debugLog(STORAGE_YAML) { "balanceNode is virtual; setting starting balance" }

            val startingBalance = currency.getStartingBalance(account)

            plugin.debugLog(STORAGE_YAML) {
                "Starting balance = ~${startingBalance.toDouble()}"
            }

            doTransactionSync(
                account,
                EconomyTransaction(
                    currency.identifier,
                    Cause.SERVER,
                    Instant.now(),
                    EconomyTransactionType.SET,
                    "Starting balance initiated via balance request",
                    startingBalance,
                    EconomyTransactionImportance.HIGH
                )
            )

            plugin.debugLog(STORAGE_YAML) {
                "Transaction complete for starting balance; returning response"
            }
        }

        plugin.debugLog(STORAGE_YAML) { "balanceNode is not virtual" }

        return BigDecimal(balanceNode.double)
    }

    override fun retrieveBalanceSync(
        account: NonPlayerAccount,
        currency: Currency,
    ): BigDecimal {
        val balanceNode = getAccountNodeSync(account).node("balance", getOrGrantCurrencyDbIdSync(currency))
        plugin.debugLog(STORAGE_YAML) { "balanceNode @ ${balanceNode.path()}" }

        if (balanceNode.virtual()) {
            plugin.debugLog(STORAGE_YAML) { "balanceNode is virtual; setting starting balance" }

            val startingBalance = currency.getStartingBalance(account)

            plugin.debugLog(STORAGE_YAML) {
                "Starting balance = ~${startingBalance.toDouble()}"
            }

            doTransactionSync(
                account,
                EconomyTransaction(
                    currency.identifier,
                    Cause.SERVER,
                    Instant.now(),
                    EconomyTransactionType.SET,
                    "Starting balance initiated via balance request",
                    startingBalance,
                    EconomyTransactionImportance.HIGH
                )
            )

            plugin.debugLog(STORAGE_YAML) {
                "Transaction complete for starting balance; returning response"
            }
        }

        plugin.debugLog(STORAGE_YAML) { "balanceNode is not virtual" }

        return BigDecimal(balanceNode.double)
    }

    override fun doTransactionSync(
        account: PlayerAccount,
        transaction: EconomyTransaction,
    ): BigDecimal {
        plugin.debugLog(STORAGE_YAML) {
            "Finding currency by ID"
        }

        val currency = plugin.economyManager.findCurrencyNonNull(transaction.currencyId)

        plugin.debugLog(STORAGE_YAML) {
            "Found currency: ${currency.identifier}"
        }

        if (transaction.type != EconomyTransactionType.SET &&
            transaction.amount.compareTo(BigDecimal.ZERO) == -1
        ) {
            plugin.debugLog(STORAGE_YAML) {
                "Error: Negative amount cannot be specified when transaction type is not SET."
            }

            throw TreasuryException {
                "Transactions must not be actioned with a negative amount unless the transaction type is 'SET'"
            }
        }

        val previousBalance: BigDecimal = let {
            if (!holdsCurrencySync(account, currency)) {
                return@let BigDecimal.ZERO
            }

            return@let retrieveBalanceSync(
                account,
                currency
            )
        }

        plugin.debugLog(STORAGE_YAML) { "Previous balance: ${previousBalance.toDouble()}" }

        val newBalance: BigDecimal = when (transaction.type) {
            EconomyTransactionType.SET ->
                transaction.amount

            EconomyTransactionType.DEPOSIT ->
                previousBalance.add(transaction.amount)

            EconomyTransactionType.WITHDRAWAL ->
                previousBalance.subtract(transaction.amount)
        }

        plugin.debugLog(STORAGE_YAML) { "New balance: ${newBalance.toDouble()}" }

        val minBalance = BigDecimal(
            plugin.settings
                .rootNode
                .node("advanced", "minimum-balance")
                .getDouble(0.0)
        )

        plugin.debugLog(STORAGE_YAML) { "Min balance: ${minBalance.toDouble()}" }

        if (newBalance.compareTo(minBalance) == -1) {
            plugin.debugLog(STORAGE_YAML) { "error: new balance is below min balance" }
            throw TreasuryException { "Transaction would result in an overdraft" }
        }

        plugin.debugLog(STORAGE_YAML) { "Firing transaction event" }
        val event: FireCompletion<PlayerAccountTransactionEvent> = EventBus.INSTANCE.fire(
            PlayerAccountTransactionEvent(
                transaction,
                account
            )
        )

        val eventThrowables: MutableList<Throwable> = mutableListOf()

        event.whenCompleteBlocking { _, throwables ->
            eventThrowables.addAll(throwables)
        }

        if (eventThrowables.isNotEmpty()) {
            plugin.debugLog(STORAGE_YAML) { "Event exceptions is not empty; throwing" }
            throw eventThrowables.first()
        }

        plugin.debugLog(STORAGE_YAML) { "Attempting to write new balance to data file" }

        plugin.debugLog(STORAGE_YAML) { "Fetching balance node" }
        val balanceNode = getAccountNodeSync(account)
            .node("balance", getOrGrantCurrencyDbIdSync(currency))

        plugin.debugLog(STORAGE_YAML) { "Is virtual: ${balanceNode.virtual()}" }

        plugin.debugLog(STORAGE_YAML) { "Setting node to new balance" }
        balanceNode.set(newBalance.toDouble())

        plugin.debugLog(STORAGE_YAML) { "Adding transaction to history" }

        val historyNodes = getAccountNodeSync(account).node("transaction")
        val index = historyNodes.childrenList().size
        val historyNode = historyNodes.node(index)

        historyNode
            .node("currency-dbid")
            .set(getOrGrantCurrencyDbIdSync(currency))

        historyNode
            .node("cause", "type")
            .set(let {
                if (transaction.cause is Cause.Player) {
                    return@let "Player"
                } else if (transaction.cause is Cause.NonPlayer) {
                    return@let "NonPlayer"
                } else if (transaction.cause is Cause.Plugin) {
                    return@let "Plugin"
                } else if (transaction.cause.equals(Cause.SERVER)) {
                    return@let "Server"
                } else {
                    return@let "Custom"
                }
            })

        historyNode
            .node("cause", "data")
            .set(transaction.cause.identifier().toString())

        historyNode
            .node("timestamp")
            .set(transaction.timestamp.toEpochMilli())

        historyNode
            .node("type")
            .set(transaction.type.name)

        if (transaction.reason.isPresent) {
            historyNode
                .node("reason")
                .set(transaction.reason.get())
        }

        historyNode
            .node("amount")
            .set(transaction.amount.toDouble())

        historyNode
            .node("importance")
            .set(transaction.importance.name)


        plugin.debugLog(STORAGE_YAML) { "Writing changes to disk" }
        write()

        return newBalance
    }

    override fun doTransactionSync(
        account: NonPlayerAccount,
        transaction: EconomyTransaction,
    ): BigDecimal {
        plugin.debugLog(STORAGE_YAML) {
            "Finding currency by ID"
        }

        val currency = plugin.economyManager.findCurrencyNonNull(transaction.currencyId)

        plugin.debugLog(STORAGE_YAML) {
            "Found currency: ${currency.identifier}"
        }

        if (transaction.type != EconomyTransactionType.SET &&
            transaction.amount.compareTo(BigDecimal.ZERO) == -1
        ) {
            plugin.debugLog(STORAGE_YAML) {
                "Error: Negative amount cannot be specified when transaction type is not SET."
            }

            throw TreasuryException {
                "Transactions must not be actioned with a negative amount unless the transaction type is 'SET'"
            }
        }

        val previousBalance: BigDecimal = let {
            if (!holdsCurrencySync(account, currency)) {
                return@let BigDecimal.ZERO
            }

            return@let retrieveBalanceSync(
                account,
                currency
            )
        }

        plugin.debugLog(STORAGE_YAML) { "Previous balance: ${previousBalance.toDouble()}" }

        val newBalance: BigDecimal = when (transaction.type) {
            EconomyTransactionType.SET ->
                transaction.amount

            EconomyTransactionType.DEPOSIT ->
                previousBalance.add(transaction.amount)

            EconomyTransactionType.WITHDRAWAL ->
                previousBalance.subtract(transaction.amount)
        }

        plugin.debugLog(STORAGE_YAML) { "New balance: ${newBalance.toDouble()}" }

        val minBalance = BigDecimal(
            plugin.settings
                .rootNode
                .node("advanced", "minimum-balance")
                .getDouble(0.0)
        )

        plugin.debugLog(STORAGE_YAML) { "Min balance: ${minBalance.toDouble()}" }

        if (newBalance.compareTo(minBalance) == -1) {
            plugin.debugLog(STORAGE_YAML) { "error: new balance is below min balance" }
            throw TreasuryException { "Transaction would result in an overdraft" }
        }

        plugin.debugLog(STORAGE_YAML) { "Firing transaction event" }
        val event: FireCompletion<NonPlayerAccountTransactionEvent> = EventBus.INSTANCE.fire(
            NonPlayerAccountTransactionEvent(
                transaction,
                account
            )
        )

        val eventThrowables: MutableList<Throwable> = mutableListOf()

        event.whenCompleteBlocking { _, throwables ->
            eventThrowables.addAll(throwables)
        }

        if (eventThrowables.isNotEmpty()) {
            plugin.debugLog(STORAGE_YAML) { "Event exceptions is not empty; throwing" }
            throw eventThrowables.first()
        }

        plugin.debugLog(STORAGE_YAML) { "Attempting to write new balance to data file" }

        plugin.debugLog(STORAGE_YAML) { "Fetching balance node" }
        val balanceNode = getAccountNodeSync(account)
            .node("balance", getOrGrantCurrencyDbIdSync(currency))

        plugin.debugLog(STORAGE_YAML) { "Is virtual: ${balanceNode.virtual()}" }

        plugin.debugLog(STORAGE_YAML) { "Setting node to new balance" }
        balanceNode.set(newBalance.toDouble())

        plugin.debugLog(STORAGE_YAML) { "Adding transaction to history" }

        val historyNodes = getAccountNodeSync(account).node("transaction")
        val index = historyNodes.childrenList().size
        val historyNode = historyNodes.node(index)

        historyNode
            .node("currency-dbid")
            .set(getOrGrantCurrencyDbIdSync(currency))

        historyNode
            .node("cause", "type")
            .set(let {
                if (transaction.cause is Cause.Player) {
                    return@let "Player"
                } else if (transaction.cause is Cause.NonPlayer) {
                    return@let "NonPlayer"
                } else if (transaction.cause is Cause.Plugin) {
                    return@let "Plugin"
                } else if (transaction.cause.identifier() == "Server") {
                    return@let "Server"
                } else {
                    return@let "Custom"
                }
            })

        historyNode
            .node("cause", "data")
            .set(transaction.cause.identifier().toString())

        historyNode
            .node("timestamp")
            .set(transaction.timestamp.toEpochMilli())

        historyNode
            .node("type")
            .set(transaction.type.name)

        if (transaction.reason.isPresent) {
            historyNode
                .node("reason")
                .set(transaction.reason.get())
        }

        historyNode
            .node("amount")
            .set(transaction.amount.toDouble())

        historyNode
            .node("importance")
            .set(transaction.importance.name)


        plugin.debugLog(STORAGE_YAML) { "Writing changes to disk" }
        write()

        return newBalance
    }

    override fun retrieveHeldCurrenciesSync(
        account: PlayerAccount,
    ): Collection<String> {
        return plugin.economyManager
            .registeredCurrencies
            .stream()
            .filter { currency -> holdsCurrencySync(account, currency) }
            .map { currency -> currency.identifier }
            .collect(Collectors.toList())
    }

    override fun retrieveHeldCurrenciesSync(
        account: NonPlayerAccount,
    ): Collection<String> {
        return plugin.economyManager
            .registeredCurrencies
            .stream()
            .filter { currency -> holdsCurrencySync(account, currency) }
            .map { currency -> currency.identifier }
            .collect(Collectors.toList())
    }

    override fun retrieveTransactionHistorySync(
        account: PlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant,
    ): Collection<EconomyTransaction> {
        return getAccountNodeSync(account)
            .node("transaction")
            .childrenList()
            .map { tNode ->
                EconomyTransaction(
                    // currency id
                    getCurrencyByDbId(
                        tNode
                            .node("currency-dbid")
                            .int
                    ).identifier,

                    // cause
                    when (PolyCause.valueOf(
                        tNode.node("cause", "type").string!!
                            .uppercase(Locale.ROOT)
                    )) {
                        PolyCause.PLAYER -> Cause.player(
                            UUID.fromString(tNode.node("cause", "data").string!!)
                        )

                        PolyCause.NON_PLAYER -> Cause.nonPlayer(
                            NamespacedKey.fromString(tNode.node("cause", "data").string!!)
                        )

                        PolyCause.PLUGIN -> Cause.plugin(
                            NamespacedKey.fromString(tNode.node("cause", "data").string!!)
                        )

                        PolyCause.SERVER -> Cause.SERVER
                    },

                    // timestamp
                    Instant.ofEpochMilli(
                        tNode
                            .node("timestamp")
                            .long
                    ),

                    // type
                    EconomyTransactionType.valueOf(
                        tNode
                            .node("type")
                            .string!!
                    ),

                    // reason
                    tNode
                        .node("reason")
                        .string,

                    // amount
                    BigDecimal(
                        tNode
                            .node("amount")
                            .double
                    ),

                    // importance
                    EconomyTransactionImportance.valueOf(
                        tNode
                            .node("importance")
                            .string!!
                    )
                )
            }
            .filterIndexed { index, transaction ->
                index < transactionCount &&
                        transaction.timestamp >= from &&
                        transaction.timestamp <= to
            }
    }

    override fun retrieveTransactionHistorySync(
        account: NonPlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant,
    ): Collection<EconomyTransaction> {
        return getAccountNodeSync(account)
            .node("transaction")
            .childrenList()
            .map { tNode ->
                EconomyTransaction(
                    // currency id
                    getCurrencyByDbId(
                        tNode
                            .node("currency-dbid")
                            .int
                    ).identifier,

                    // cause
                    when (PolyCause.valueOf(
                        tNode.node("cause", "type").string!!
                            .uppercase(Locale.ROOT)
                    )) {
                        PolyCause.PLAYER -> Cause.player(
                            UUID.fromString(tNode.node("cause", "data").string!!)
                        )

                        PolyCause.NON_PLAYER -> Cause.nonPlayer(
                            NamespacedKey.fromString(tNode.node("cause", "data").string!!)
                        )

                        PolyCause.PLUGIN -> Cause.plugin(
                            NamespacedKey.fromString(tNode.node("cause", "data").string!!)
                        )

                        PolyCause.SERVER -> Cause.SERVER
                    },

                    // timestamp
                    Instant.ofEpochMilli(
                        tNode
                            .node("timestamp")
                            .long
                    ),

                    // type
                    EconomyTransactionType.valueOf(
                        tNode
                            .node("type")
                            .string!!
                    ),

                    // reason
                    tNode
                        .node("reason")
                        .string,

                    // amount
                    BigDecimal(
                        tNode
                            .node("amount")
                            .double
                    ),

                    // importance
                    EconomyTransactionImportance.valueOf(
                        tNode
                            .node("importance")
                            .string!!
                    )
                )
            }
            .filterIndexed { index, transaction ->
                index < transactionCount &&
                        transaction.timestamp >= from &&
                        transaction.timestamp <= to
            }
    }

    override fun retrieveMemberIdsSync(
        account: NonPlayerAccount,
    ): Collection<UUID> {
        return getAccountNodeSync(account)
            .node("member")
            .childrenMap()
            .keys
            .map { UUID.fromString((it as String)) }
    }

    override fun isMemberSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
    ): Boolean {
        val memNode = getAccountNodeSync(account)
            .node("member", memberPlayer.toString())

        if (memNode.virtual()) {
            return false
        }

        return memNode.node("permission")
            .childrenMap()
            .any { (_, node) -> node.string!! == TriState.TRUE.name }
    }

    override fun setPermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
        permissionValue: TriState,
        vararg permissions: AccountPermission,
    ): Boolean {
        val permsNode = getAccountNodeSync(account)
            .node("member", memberPlayer.toString(), "permission")

        var adjustmentMade = false

        for (permission in permissions) {
            val node = permsNode.node(permission.name)

            if (!node.getString("").equals(permissionValue.name, ignoreCase = true)) {
                adjustmentMade = true
            }

            node.set(permissionValue.name)
        }

        write()

        return adjustmentMade
    }

    override fun setPermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
        permissionsMap: Map<AccountPermission, TriState>,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun retrievePermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
    ): Map<AccountPermission, TriState> {
        if (!isMemberSync(account, memberPlayer)) {
            throw TreasuryException {
                "Player is not a member of the account"
            }
        }

        return getAccountNodeSync(account)
            .node(
                "member",
                memberPlayer.toString(),
                "permission"
            )
            .childrenMap()
            .mapKeys { AccountPermission.valueOf(it.key as String) }
            .mapValues { TriState.valueOf(it.value.string!!) }
            .withDefault { TriState.UNSPECIFIED }
    }

    override fun retrievePermissionsMapSync(
        account: NonPlayerAccount,
    ): Map<UUID, Map<AccountPermission, TriState>> {
        val members = retrieveMemberIdsSync(account)

        val map = mutableMapOf<UUID, Map<AccountPermission, TriState>>()

        for (member in members) {
            map[member] = retrievePermissionsSync(account, member)
        }

        return map
    }

    override fun hasPermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
        vararg permissions: AccountPermission,
    ): TriState {
        if (permissions.isEmpty()) {
            throw TreasuryException {
                "At least one item must be specified in the permissions array."
            }
        }

        // todo: if any are false, return false. if any are unspecified, return that. otherwise return true

        return TriState.fromBoolean(
            permissions.all { permission ->
                getAccountNodeSync(account)
                    .node(
                        "member",
                        memberPlayer.toString(),
                        "permission",
                        permission.name
                    )
                    .string!! == TriState.TRUE.name
            }
        )
    }


    override fun retrievePlayerAccountIdsSync(): Collection<UUID> {
        return rootNode
            .node("account", "player")
            .childrenMap()
            .keys
            .map { key -> UUID.fromString(key as String) }
    }

    override fun retrieveNonPlayerAccountIdsSync(): Collection<NamespacedKey> {
        val ids = mutableListOf<NamespacedKey>()

        rootNode
            .node("account", "non-player")
            .childrenMap()
            .forEach { (namespace, namespaceNode) ->
                namespaceNode.childrenMap().forEach { (key, _) ->
                    ids.add(NamespacedKey.of(namespace as String, key as String))
                }
            }

        return ids
    }

}