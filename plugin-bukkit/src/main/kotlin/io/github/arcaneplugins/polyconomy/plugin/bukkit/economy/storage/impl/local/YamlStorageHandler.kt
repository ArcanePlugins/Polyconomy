package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.impl.local

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.STORAGE_YAML
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.EconomyManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageHandler
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.event.EventBus
import me.lokka30.treasury.api.common.event.FireCompletion
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.account.AccountPermission
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.events.NonPlayerAccountTransactionEvent
import me.lokka30.treasury.api.economy.events.PlayerAccountTransactionEvent
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionImportance
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionInitiator
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

object YamlStorageHandler : StorageHandler("Yaml") {

/*
=======================
FILE STRUCTURE EXAMPLE.
=======================

account:
    player:
      "<player: UUID>":
          name: <String?>
          balance:
            "<currency-dbid: Int->String>": <balance: Double>
          transaction:
            - currency-dbid: <Int>
              initiator:
                type: <PolyTransactionInitiator.Type>
                data: <String>
              timestamp: <Long ; epoch milliseconds>
              type: <PolyTransactionType>
              reason: <String?>
              amount: <Double>
              importance: <PolyTransactionImportance>


    non-player:
        "<id namespace: String>":
          "<id key: String>":
            name: <String?>
            balance: (same as player accounts)
            transaction: (same as player accounts)
            member:
              "player: <UUID>":
                permission:
                  "<PolyAccountPermission>": "value: <TriState>"

currency:
  current-db-id: <last granted currency db id: Int>
  db-id-map:
    <currency-id: String>: <currency-db-id: Int>

metadata:
  file-version: 1
  original:
    file-version: 1
    plugin-version: "${project.version}"
 */

    private val relativePath: Path = Path("data${File.separator}data.yml")

    private lateinit var loader: YamlConfigurationLoader

    private lateinit var rootNode: CommentedConfigurationNode

    private fun read() {
        Log.d(STORAGE_YAML) { "Reading data." }
        Log.d(STORAGE_YAML) { "Absolute path: ${absolutePath()}" }
        createIfNotExists()
        rootNode = loader.load()
        Log.d(STORAGE_YAML) { "Read data." }
    }

    private fun write() {
        Log.d(STORAGE_YAML) { "Writing data." }
        loader.save(rootNode)
        Log.d(STORAGE_YAML) { "Written data." }
    }

    private fun absolutePath(): Path {
        return Path(
            "${Polyconomy.instance.dataFolder.absolutePath}${File.separator}${relativePath}"
        )
    }

    private fun createIfNotExists() {
        val exists: Boolean = absolutePath().exists()
        Log.d(STORAGE_YAML) { "Data file exists: ${if(exists) "Yes" else "No"}" }
        if(exists) return

        Log.d(STORAGE_YAML) { "File doesn't exist; creating." }
        absolutePath().parent.createDirectories()
        absolutePath().createFile()
        Log.d(STORAGE_YAML) { "File created." }
    }

    private fun getAccountNodeSync(
        player: UUID
    ): CommentedConfigurationNode {
        return rootNode.node("account", "player", player.toString())
    }

    private fun getAccountNodeSync(
        account: PlayerAccount
    ): CommentedConfigurationNode {
        return getAccountNodeSync(account.uniqueId)
    }

    private fun getAccountNodeSync(
        id: NamespacedKey
    ): CommentedConfigurationNode {
        return rootNode.node("account", "non-player", id.namespace, id.key)
    }

    private fun getAccountNodeSync(
        account: NonPlayerAccount
    ): CommentedConfigurationNode {
        return getAccountNodeSync(account.identifier)
    }

    @Suppress("BooleanMethodIsAlwaysInverted")
    private fun holdsCurrencySync(
        account: PlayerAccount,
        currency: Currency
    ): Boolean {
        return !getAccountNodeSync(account)
            .node("balance", getOrGrantCurrencyDbIdSync(currency))
            .virtual()
    }

    @Suppress("BooleanMethodIsAlwaysInverted")
    private fun holdsCurrencySync(
        account: NonPlayerAccount,
        currency: Currency
    ): Boolean {
        return !getAccountNodeSync(account)
            .node("balance", getOrGrantCurrencyDbIdSync(currency))
            .virtual()
    }

    private fun getOrGrantCurrencyDbIdSync(
        currencyId: String
    ): Int {
        return getCurrencyDbId(currencyId) ?: grantCurrencyDbId(currencyId)
    }

    private fun getOrGrantCurrencyDbIdSync(
        currency: Currency
    ): Int {
        return getOrGrantCurrencyDbIdSync(currency.identifier)
    }

    private fun getCurrencyDbId(
        currencyId: String
    ): Int? {
        val currencyDbIdNode = getCurrencyDbIdNode(currencyId)
        return if(currencyDbIdNode.virtual()) null else currencyDbIdNode.int
    }

    private fun grantCurrencyDbId(
        currencyId: String
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
        currencyId: String
    ): CommentedConfigurationNode {
        return rootNode.node("currency", "db-id-map", currencyId)
    }

    private fun getCurrencyByDbId(
        dbId: Int
    ): Currency {
        return EconomyManager.currencies.first {
            getOrGrantCurrencyDbIdSync(it) == dbId
        }
    }

    override fun connect() {
        Log.d(STORAGE_YAML) { "Connecting." }

        if(connected)
            throw IllegalStateException("Attempted to connect whilst already connected")

        Log.d(STORAGE_YAML) { "Initialising loader." }
        loader = YamlConfigurationLoader.builder()
            .path(absolutePath())
            .build()
        Log.d(STORAGE_YAML) { "Initialised loader." }

        Log.d(STORAGE_YAML) { "Checking if file has not been created yet." }
        createIfNotExists()
        Log.d(STORAGE_YAML) { "File present; continuing." }

        Log.d(STORAGE_YAML) { "Initialising root node: reading data." }
        read()
        Log.d(STORAGE_YAML) { "Initialised root node." }

        connected = true
        Log.d(STORAGE_YAML) { "Connected." }
    }

    override fun disconnect() {
        Log.d(STORAGE_YAML) { "Disconnecting." }

        if(!connected) {
            Log.d(STORAGE_YAML) { "Attempted to disconnect, but is already disconnected." }
            return
        }

        /*
        YAML does not need any disconnect behaviour. The underlying libraries handle the file
        connection being closed with the operating system.
         */
        connected = false

        Log.d(STORAGE_YAML) { "Disconnected." }
    }

    override fun hasPlayerAccountSync(
        player: UUID
    ): Response<TriState> {
        return try {
            Response.success(
                TriState.fromBoolean(
                    !getAccountNodeSync(player)
                        .virtual()
                )
            )
        } catch (ex: Exception) {
            Response.failure { ex.message ?: "?" }
        }
    }

    override fun hasNonPlayerAccountSync(
        id: NamespacedKey
    ): Response<TriState> {
        return try {
            Response.success(
                TriState.fromBoolean(
                    !getAccountNodeSync(id)
                        .virtual()
                )
            )
        } catch (ex: Exception) {
            Response.failure { ex.message ?: "?" }
        }
    }

    override fun retrieveNameSync(
        account: PlayerAccount
    ): Response<Optional<String>> {
        return try {
            Response.success(
                Optional.ofNullable(
                    getAccountNodeSync(account)
                        .node("name")
                        .string
                )
            )
        } catch (ex: Exception) {
            Response.failure { ex.message ?: "?" }
        }
    }

    override fun retrieveNameSync(
        account: NonPlayerAccount
    ): Response<Optional<String>> {
        return try {
            Response.success(
                Optional.ofNullable(
                    getAccountNodeSync(account)
                        .node("name")
                        .string
                )
            )
        } catch (ex: Exception) {
            Response.failure { ex.message ?: "?" }
        }
    }

    override fun setNameSync(
        account: PlayerAccount,
        name: String?
    ): Response<TriState> {
        try {
            val previousNameResponse = retrieveNameSync(account)

            if(!previousNameResponse.isSuccessful) {
                return Response.failure { previousNameResponse.failureReason!!.description }
            }

            val previousName = previousNameResponse.result!!

            if((!previousName.isPresent && name == null) || (previousName.get() == name)) {
                return Response.success(TriState.UNSPECIFIED)
            }

            getAccountNodeSync(account)
                .node("name")
                .set(name)

            write()

            return Response.success(TriState.TRUE)
        } catch (ex: Exception) {
            return Response.failure { ex.message ?: "?" }
        }
    }

    override fun setNameSync(
        account: NonPlayerAccount,
        name: String?
    ): Response<TriState> {
        try {
            val previousNameResponse = retrieveNameSync(account)

            if(!previousNameResponse.isSuccessful) {
                return Response.failure { previousNameResponse.failureReason!!.description }
            }

            val previousName = previousNameResponse.result!!

            if((!previousName.isPresent && name == null) || (previousName.get() == name)) {
                return Response.success(TriState.UNSPECIFIED)
            }

            getAccountNodeSync(account)
                .node("name")
                .set(name)

            write()

            return Response.success(TriState.TRUE)
        } catch (ex: Exception) {
            return Response.failure { ex.message ?: "?" }
        }
    }

    override fun deleteAccountSync(
        account: PlayerAccount
    ): Response<TriState> {
        return try {
            getAccountNodeSync(account)
                .set(null)

            write()

            Response.success(TriState.TRUE)
        } catch (ex: Exception) {
            Response.failure { ex.message ?: "?" }
        }
    }

    override fun deleteAccountSync(
        account: NonPlayerAccount
    ): Response<TriState> {
        return try {
            getAccountNodeSync(account)
                .set(null)

            write()

            Response.success(TriState.TRUE)
        } catch (ex: Exception) {
            Response.failure { ex.message ?: "?" }
        }
    }

    override fun retrieveBalanceSync(
        account: PlayerAccount,
        currency: Currency
    ): Response<BigDecimal> {
        try {
            return Response.success(let {
                val balanceNode = getAccountNodeSync(account).node("balance", getOrGrantCurrencyDbIdSync(currency))
                Log.d(STORAGE_YAML) { "balanceNode @ ${balanceNode.path()}" }

                if(balanceNode.virtual()) {
                    Log.d(STORAGE_YAML) { "balanceNode is virtual; setting starting balance" }

                    val startingBalance = currency.getStartingBalance(account)

                    Log.d(STORAGE_YAML) {
                        "Starting balance = ~${startingBalance.toDouble()}"
                    }

                    doTransactionSync(
                        account,
                        EconomyTransaction(
                            currency.identifier,
                            EconomyTransactionInitiator.SERVER,
                            Instant.now(),
                            EconomyTransactionType.SET,
                            "Starting balance initiated via balance request",
                            startingBalance,
                            EconomyTransactionImportance.HIGH
                        )
                    )

                    Log.d(STORAGE_YAML) {
                        "Transaction complete for starting balance; returning response"
                    }
                }

                Log.d(STORAGE_YAML) { "balanceNode is not virtual" }

                BigDecimal(
                    balanceNode.double
                ) // <-- returned
            })
        } catch (ex: Exception) {
            return Response.failure { ex.message ?: "?" }
        }
    }

    override fun retrieveBalanceSync(
        account: NonPlayerAccount,
        currency: Currency
    ): Response<BigDecimal> {
        try {
            return Response.success(let {
                val balanceNode = getAccountNodeSync(account).node("balance", getOrGrantCurrencyDbIdSync(currency))
                Log.d(STORAGE_YAML) { "balanceNode @ ${balanceNode.path()}" }

                if(balanceNode.virtual()) {
                    Log.d(STORAGE_YAML) { "balanceNode is virtual; setting starting balance" }

                    val startingBalance = currency.getStartingBalance(account)

                    Log.d(STORAGE_YAML) {
                        "Starting balance = ~${startingBalance.toDouble()}"
                    }

                    doTransactionSync(
                        account,
                        EconomyTransaction(
                            currency.identifier,
                            EconomyTransactionInitiator.SERVER,
                            Instant.now(),
                            EconomyTransactionType.SET,
                            "Starting balance initiated via balance request",
                            startingBalance,
                            EconomyTransactionImportance.HIGH
                        )
                    )

                    Log.d(STORAGE_YAML) {
                        "Transaction complete for starting balance; returning response"
                    }
                }

                Log.d(STORAGE_YAML) { "balanceNode is not virtual" }

                BigDecimal(
                    balanceNode.double
                ) // <-- returned
            })
        } catch (ex: Exception) {
            return Response.failure { ex.message ?: "?" }
        }
    }

    override fun doTransactionSync(
        account: PlayerAccount,
        transaction: EconomyTransaction
    ): Response<BigDecimal> {
        try {
            Log.d(STORAGE_YAML) {
                "Finding currency by ID"
            }

            val currency = EconomyManager.findCurrencyNonNull(transaction.currencyID)

            Log.d(STORAGE_YAML) {
                "Found currency: ${currency.identifier}"
            }

            if(transaction.transactionType != EconomyTransactionType.SET &&
                transaction.transactionAmount.compareTo(BigDecimal.ZERO) == -1
            ) {
                Log.d(STORAGE_YAML) {
                    "error: negative amount cannot be specified when transaction type is not SET."
                }

                return Response.failure { "Negative amount specified for the transaction" }
            }

            val previousBalance: BigDecimal = let {
                if(!holdsCurrencySync(account, currency)) {
                    return@let BigDecimal.ZERO
                }

                Log.d(STORAGE_YAML) { "Sending previous balance response" }
                val previousBalanceResponse = retrieveBalanceSync(
                    account,
                    currency
                )

                Log.d(STORAGE_YAML) { "Received previous balance response" }

                if(!previousBalanceResponse.isSuccessful) {
                    Log.d(STORAGE_YAML) {
                        "previousBalanceResponse was not successful. " +
                                "error: ${previousBalanceResponse.failureReason!!.description}"
                    }

                    return Response.failure { previousBalanceResponse.failureReason!!.description }
                }
                Log.d(STORAGE_YAML) { "Previous balance response was successful" }

                return@let previousBalanceResponse.result!!
            }

            Log.d(STORAGE_YAML) { "Previous balance: ${previousBalance.toDouble()}" }

            val newBalance: BigDecimal = when (transaction.transactionType) {
                EconomyTransactionType.SET ->
                    transaction.transactionAmount

                EconomyTransactionType.DEPOSIT ->
                    previousBalance.add(transaction.transactionAmount)

                EconomyTransactionType.WITHDRAWAL ->
                    previousBalance.subtract(transaction.transactionAmount)
            }

            Log.d(STORAGE_YAML) { "New balance: ${newBalance.toDouble()}" }

            val minBalance = BigDecimal(
                SettingsCfg
                    .rootNode
                    .node("advanced", "minimum-balance")
                    .getDouble(0.0)
            )

            Log.d(STORAGE_YAML) { "Min balance: ${minBalance.toDouble()}" }

            if(newBalance.compareTo(minBalance) == -1) {
                Log.d(STORAGE_YAML) { "error: new balance is below min balance" }
                return Response.failure {
                    "Transaction would result in an overdraft"
                }
            }

            Log.d(STORAGE_YAML) { "Firing transaction event" }
            val event: FireCompletion<PlayerAccountTransactionEvent> = EventBus.INSTANCE.fire(
                PlayerAccountTransactionEvent(
                    transaction,
                    account
                )
            )

            var eventResult: PlayerAccountTransactionEvent? = null
            val eventThrowables: MutableList<Throwable> = mutableListOf()

            event.whenCompleteBlocking { result, throwables ->
                eventResult = result
                eventThrowables.addAll(throwables)
            }

            if(eventThrowables.isNotEmpty()) {
                Log.d(STORAGE_YAML) { "Event exceptions is not empty; throwing" }
                throw eventThrowables.first()
            }

            if(eventResult!!.isCancelled) {
                return Response.failure { "Transaction event was cancelled" }
            }

            Log.d(STORAGE_YAML) { "Attempting to write new balance to data file" }

            Log.d(STORAGE_YAML) { "Fetching balance node" }
            val balanceNode = getAccountNodeSync(account)
                .node("balance", getOrGrantCurrencyDbIdSync(currency))

            Log.d(STORAGE_YAML) { "Is virtual: ${balanceNode.virtual()}" }

            Log.d(STORAGE_YAML) { "Setting node to new balance" }
            balanceNode.set(newBalance.toDouble())

            Log.d(STORAGE_YAML) { "Writing changes to disk" }
            write()

            return Response.success(newBalance)
        } catch(ex: Exception) {
            return Response.failure { ex.message ?: "?" }
        }
    }

    override fun doTransactionSync(
        account: NonPlayerAccount,
        transaction: EconomyTransaction
    ): Response<BigDecimal> {
        try {
            Log.d(STORAGE_YAML) {
                "Finding currency by ID"
            }

            val currency = EconomyManager.findCurrencyNonNull(transaction.currencyID)

            Log.d(STORAGE_YAML) {
                "Found currency: ${currency.identifier}"
            }

            if(transaction.transactionType != EconomyTransactionType.SET &&
                transaction.transactionAmount.compareTo(BigDecimal.ZERO) == -1
            ) {
                Log.d(STORAGE_YAML) {
                    "error: negative amount cannot be specified when transaction type is not SET."
                }

                return Response.failure { "Negative amount specified for the transaction" }
            }

            val previousBalance: BigDecimal = let {
                if(!holdsCurrencySync(account, currency)) {
                    return@let BigDecimal.ZERO
                }

                Log.d(STORAGE_YAML) { "Sending previous balance response" }
                val previousBalanceResponse = retrieveBalanceSync(
                    account,
                    currency
                )

                Log.d(STORAGE_YAML) { "Received previous balance response" }

                if(!previousBalanceResponse.isSuccessful) {
                    Log.d(STORAGE_YAML) {
                        "previousBalanceResponse was not successful. " +
                                "error: ${previousBalanceResponse.failureReason!!.description}"
                    }

                    return Response.failure { previousBalanceResponse.failureReason!!.description }
                }
                Log.d(STORAGE_YAML) { "Previous balance response was successful" }

                return@let previousBalanceResponse.result!!
            }

            Log.d(STORAGE_YAML) { "Previous balance: ${previousBalance.toDouble()}" }

            val newBalance: BigDecimal = when (transaction.transactionType) {
                EconomyTransactionType.SET ->
                    transaction.transactionAmount

                EconomyTransactionType.DEPOSIT ->
                    previousBalance.add(transaction.transactionAmount)

                EconomyTransactionType.WITHDRAWAL ->
                    previousBalance.subtract(transaction.transactionAmount)
            }

            Log.d(STORAGE_YAML) { "New balance: ${newBalance.toDouble()}" }

            val minBalance = BigDecimal(
                SettingsCfg
                    .rootNode
                    .node("advanced", "minimum-balance")
                    .getDouble(0.0)
            )

            Log.d(STORAGE_YAML) { "Min balance: ${minBalance.toDouble()}" }

            if(newBalance.compareTo(minBalance) == -1) {
                Log.d(STORAGE_YAML) { "error: new balance is below min balance" }
                return Response.failure {
                    "Transaction would result in an overdraft"
                }
            }

            Log.d(STORAGE_YAML) { "Firing transaction event" }
            val event: FireCompletion<NonPlayerAccountTransactionEvent> = EventBus.INSTANCE.fire(
                NonPlayerAccountTransactionEvent(
                    transaction,
                    account
                )
            )

            var eventResult: NonPlayerAccountTransactionEvent? = null
            val eventThrowables: MutableList<Throwable> = mutableListOf()

            event.whenCompleteBlocking { result, throwables ->
                eventResult = result
                eventThrowables.addAll(throwables)
            }

            if(eventThrowables.isNotEmpty()) {
                Log.d(STORAGE_YAML) { "Event exceptions is not empty; throwing" }
                throw eventThrowables.first()
            }

            if(eventResult!!.isCancelled) {
                return Response.failure { "Transaction event was cancelled" }
            }

            Log.d(STORAGE_YAML) { "Attempting to write new balance to data file" }

            Log.d(STORAGE_YAML) { "Fetching balance node" }
            val balanceNode = getAccountNodeSync(account)
                .node("balance", getOrGrantCurrencyDbIdSync(currency))

            Log.d(STORAGE_YAML) { "Is virtual: ${balanceNode.virtual()}" }

            Log.d(STORAGE_YAML) { "Setting node to new balance" }
            balanceNode.set(newBalance.toDouble())

            Log.d(STORAGE_YAML) { "Writing changes to disk" }
            write()

            return Response.success(newBalance)
        } catch(ex: Exception) {
            return Response.failure { ex.message ?: "?" }
        }
    }

    override fun retrieveHeldCurrenciesSync(
        account: PlayerAccount
    ): Response<Collection<String>> {
        return try {
            Response.success(
                EconomyManager
                    .registeredCurrencies
                    .stream()
                    .filter { currency -> holdsCurrencySync(account, currency) }
                    .map { currency -> currency.identifier }
                    .collect(Collectors.toList())
            )
        } catch(ex: Exception) {
            Response.failure { ex.message ?: "?" }
        }
    }

    override fun retrieveHeldCurrenciesSync(
        account: NonPlayerAccount
    ): Response<Collection<String>> {
        return try {
            Response.success(
                EconomyManager
                    .registeredCurrencies
                    .stream()
                    .filter { currency -> holdsCurrencySync(account, currency) }
                    .map { currency -> currency.identifier }
                    .collect(Collectors.toList())
            )
        } catch(ex: Exception) {
            Response.failure { ex.message ?: "?" }
        }
    }

    override fun retrieveTransactionHistorySync(
        account: PlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): Response<Collection<EconomyTransaction>> {
        return try {
            Response.success(
                getAccountNodeSync(account)
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

                            // initiator
                            EconomyTransactionInitiator.createInitiator(
                                EconomyTransactionInitiator.Type.valueOf(
                                    tNode
                                        .node("initiator", "type")
                                        .string!!
                                ),

                                tNode
                                    .node("initiator", "data")
                                    .get(Any::class.java)
                            ),

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
            )
        } catch(ex: Exception) {
            Response.failure { ex.message ?: "?" }
        }
    }

    override fun retrieveTransactionHistorySync(
        account: NonPlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): Response<Collection<EconomyTransaction>> {
        return try {
            Response.success(
                getAccountNodeSync(account)
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

                            // initiator
                            EconomyTransactionInitiator.createInitiator(
                                EconomyTransactionInitiator.Type.valueOf(
                                    tNode
                                        .node("initiator", "type")
                                        .string!!
                                ),

                                tNode
                                    .node("initiator", "data")
                                    .get(Any::class.java)
                            ),

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
            )
        } catch(ex: Exception) {
            Response.failure { ex.message ?: "?" }
        }
    }

    override fun retrieveMemberIdsSync(
        account: NonPlayerAccount
    ): Response<Collection<UUID>> {
        return Response.success(
            getAccountNodeSync(account)
                .node("member")
                .childrenMap()
                .keys
                .map { UUID.fromString((it as String)) }
        )
    }

    override fun isMemberSync(
        account: NonPlayerAccount,
        memberPlayer: UUID
    ): Response<TriState> {
        val memNode = getAccountNodeSync(account)
            .node("member", memberPlayer.toString())

        if(memNode.virtual()) {
            return Response.success(TriState.FALSE)
        }

        return Response.success(
            TriState.fromBoolean(
                memNode.node("permission")
                    .childrenMap()
                    .any { (_, node) -> node.string!! == TriState.TRUE.name }
            )
        )
    }

    override fun setPermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
        permissionValue: TriState,
        vararg permissions: AccountPermission
    ): Response<TriState> {
        TODO("Not yet implemented")
    }

/* TODO
------ non-player account node -----
member:
  "player: <UUID>":
    permission:
      "<PolyAccountPermission>": "value: <TriState>"
*/

    fun test(): String {
        return "Hey"
    }

    override fun retrievePermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID
    ): Response<Map<AccountPermission, TriState>> {
        val isMemberResponse = isMemberSync(account, memberPlayer)

        if(isMemberResponse.isSuccessful) {
            if(isMemberResponse.result!! != TriState.TRUE) {
                return Response.failure { "Player is not a member of the account" }
            }
        } else {
            return Response.failure { isMemberResponse.failureReason!!.description }
        }

        return Response.success(
            getAccountNodeSync(account)
                .node(
                    "member",
                    memberPlayer.toString(),
                    "permission"
                )
                .childrenMap()
                .mapKeys { AccountPermission.valueOf(it.key as String) }
                .mapValues { TriState.valueOf(it.value.string!!) }
                .withDefault { TriState.UNSPECIFIED }
        )
    }

    override fun retrievePermissionsMapSync(
        account: NonPlayerAccount
    ): Response<Map<UUID, Map<AccountPermission, TriState>>> {
        val memberIdsResponse = retrieveMemberIdsSync(account)

        if(!memberIdsResponse.isSuccessful) {
            return Response.failure { memberIdsResponse.failureReason!!.description }
        }

        val map = mutableMapOf<UUID, Map<AccountPermission, TriState>>()

        val members: Collection<UUID> = memberIdsResponse.result!!

        for(member in members) {
            val permissionsResponse = retrievePermissionsSync(account, member)

            if(!permissionsResponse.isSuccessful) {
                return Response.failure { permissionsResponse.failureReason!!.description }
            }

            map[member] = permissionsResponse.result!!
        }

        return Response.success(map)
    }

    override fun hasPermissionsSync(
        account: NonPlayerAccount,
        memberPlayer: UUID,
        vararg permissions: AccountPermission
    ): Response<TriState> {
        if(permissions.isEmpty()) {
            return Response.failure {
                "At least one item must be specified in the permissions array."
            }
        }

        return Response.success(
            TriState.fromBoolean(
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
        )
    }


    override fun retrievePlayerAccountIdsSync(): Response<Collection<UUID>> {
        return Response.success(
            rootNode
                .node("account", "player")
                .childrenMap()
                .keys
                .map { key -> UUID.fromString(key as String) }
        )
    }

    override fun retrieveNonPlayerAccountIdsSync(): Response<Collection<NamespacedKey>> {
        val ids = mutableListOf<NamespacedKey>()

        rootNode
            .node("account", "non-player")
            .childrenMap()
            .forEach { (namespace, namespaceNode) ->
                namespaceNode.childrenMap().forEach { (key, _) ->
                    ids.add(NamespacedKey.of(namespace as String, key as String))
                }
            }

        return Response.success(ids)
    }

}