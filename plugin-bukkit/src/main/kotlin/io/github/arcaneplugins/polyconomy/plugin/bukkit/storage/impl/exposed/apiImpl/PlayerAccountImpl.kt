package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.apiImpl

import io.github.arcaneplugins.polyconomy.api.account.AccountPermission
import io.github.arcaneplugins.polyconomy.api.account.AccountTransaction
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
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.ExposedStorageHandler
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.AccountBalanceSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.AccountSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.AccountTransactionSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.CurrencySchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.PlayerAccountSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.ByteUtil.uuidToBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.delete
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.Temporal
import java.util.*

class PlayerAccountImpl(
    val handler: ExposedStorageHandler,
    uuid: UUID,
) : PlayerAccount(uuid) {

    override suspend fun getName(): String? {
        return withContext(Dispatchers.IO) {
            transaction {
                PlayerAccountSchema
                    .join(
                        otherTable = AccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = PlayerAccountSchema.id,
                        otherColumn = AccountSchema.id,
                    )
                    .select(AccountSchema.name)
                    .where { PlayerAccountSchema.playerUuid eq uuidToBytes(uuid) }
                    .map { it[AccountSchema.name] }
                    .firstOrNull()
            }
        }
    }

    override suspend fun setName(newName: String?) {
        return withContext(Dispatchers.IO) {
            transaction {
                PlayerAccountSchema
                    .join(
                        otherTable = AccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = PlayerAccountSchema.id,
                        otherColumn = AccountSchema.id,
                    )
                    .update(
                        where = {
                            PlayerAccountSchema.playerUuid eq uuidToBytes(uuid)
                        }
                    ) {
                        it[AccountSchema.name] = newName
                    }
            }
        }
    }

    override suspend fun getBalance(currency: Currency): BigDecimal {
        return withContext(Dispatchers.IO) {
            transaction {
                PlayerAccountSchema
                    .join(
                        otherTable = AccountBalanceSchema,
                        joinType = JoinType.INNER,
                        onColumn = PlayerAccountSchema.id,
                        otherColumn = AccountBalanceSchema.accountId,
                    )
                    .join(
                        otherTable = CurrencySchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountBalanceSchema.currencyId,
                        otherColumn = CurrencySchema.id
                    )
                    .select(AccountBalanceSchema.balance)
                    .where { (PlayerAccountSchema.playerUuid eq uuidToBytes(uuid) and
                            (CurrencySchema.name eq currency.name)) }
                    .map { it[AccountBalanceSchema.balance] }
                    .firstOrNull() ?: BigDecimal.ZERO
            }
        }
    }

    override suspend fun makeTransaction(transaction: AccountTransaction) {
        withContext(Dispatchers.IO) {
            transaction {
                // calculate current (old) and updated (new) balances
                val oldBalance = runBlocking { getBalance(transaction.currency) }
                val newBalance = let {
                    val value = runBlocking { transaction.resultingBalance(oldBalance) }
                    val minBal = handler.plugin.settings.getMinimumBalance()

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
                AccountBalanceSchema
                    .join(
                        otherTable = PlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountBalanceSchema.accountId,
                        otherColumn = PlayerAccountSchema.id
                    )
                    .join(
                        otherTable = CurrencySchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountBalanceSchema.currencyId,
                        otherColumn = CurrencySchema.id
                    )
                    .update(
                        where = {
                            (PlayerAccountSchema.playerUuid eq uuidToBytes(uuid)) and
                                    (CurrencySchema.name eq transaction.currency.name)
                        }
                    ) {
                        it[AccountBalanceSchema.balance] = newBalance
                    }

                // insert record to transaction history
                val accountId = PlayerAccountSchema
                    .select(PlayerAccountSchema.id)
                    .where { PlayerAccountSchema.playerUuid eq uuidToBytes(uuid) }
                    .map { it[PlayerAccountSchema.id] }
                    .first()

                val currencyId = CurrencySchema
                    .select(CurrencySchema.id)
                    .where { CurrencySchema.name eq transaction.currency.name }
                    .map { it[CurrencySchema.id] }
                    .first()

                AccountTransactionSchema
                    .insert {
                        it[AccountTransactionSchema.accountId] = accountId
                        it[amount] = transaction.amount
                        it[AccountTransactionSchema.currencyId] = currencyId
                        it[causeType] = transaction.cause.type.ordinal.toByte()
                        it[causeData] = transaction.cause.data.toString()
                        it[reason] = transaction.reason
                        it[type] = transaction.type.ordinal.toByte()
                        it[timestamp] = transaction.timestamp.epochSecond
                        it[importance] = transaction.importance.ordinal.toByte()
                    }
            }
        }
    }

    override suspend fun deleteAccount() {
        withContext(Dispatchers.IO) {
            transaction {
                AccountSchema
                    .join(
                        otherTable = PlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = PlayerAccountSchema.id,
                        otherColumn = AccountSchema.id,
                    )
                    .delete(AccountSchema, where = {
                        PlayerAccountSchema.playerUuid eq uuidToBytes(uuid)
                    })
            }
        }
    }

    override suspend fun getHeldCurrencies(): Collection<Currency> {
        return withContext(Dispatchers.IO) {
            transaction {
                PlayerAccountSchema
                    .join(
                        otherTable = AccountBalanceSchema,
                        joinType = JoinType.INNER,
                        onColumn = PlayerAccountSchema.id,
                        otherColumn = AccountBalanceSchema.accountId,
                    )
                    .join(
                        otherTable = CurrencySchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountBalanceSchema.currencyId,
                        otherColumn = CurrencySchema.id
                    )
                    .select(CurrencySchema.name)
                    .where { PlayerAccountSchema.playerUuid eq uuidToBytes(uuid) }
                    .map { CurrencyImpl(handler, it[CurrencySchema.name]) }
            }
        }
    }

    override suspend fun getTransactionHistory(
        maxCount: Int,
        dateFrom: Temporal,
        dateTo: Temporal,
    ): List<AccountTransaction> {
        return withContext(Dispatchers.IO) {
            transaction {
                AccountTransactionSchema
                    .join(
                        otherTable = PlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountTransactionSchema.accountId,
                        otherColumn = PlayerAccountSchema.id,
                    )
                    .join(
                        otherTable = CurrencySchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountTransactionSchema.currencyId,
                        otherColumn = CurrencySchema.id
                    )
                    .selectAll()
                    .where { PlayerAccountSchema.playerUuid eq uuidToBytes(uuid) }
                    .map {
                        AccountTransaction(
                            amount = it[AccountTransactionSchema.amount],
                            cause = when (CauseType.entries[it[AccountTransactionSchema.causeType].toInt()]) {
                                CauseType.PLAYER -> PlayerCause(UUID.fromString(it[AccountTransactionSchema.causeData]))
                                CauseType.NON_PLAYER -> NonPlayerCause(NamespacedKey.fromString(it[AccountTransactionSchema.causeData]))
                                CauseType.PLUGIN -> PluginCause(NamespacedKey.fromString(it[AccountTransactionSchema.causeData]))
                                CauseType.SERVER -> ServerCause
                            },
                            currency = CurrencyImpl(handler, it[CurrencySchema.name]),
                            reason = it[AccountTransactionSchema.reason],
                            timestamp = Instant.ofEpochSecond(it[AccountTransactionSchema.timestamp]),
                            importance = TransactionImportance.entries[it[AccountTransactionSchema.importance].toInt()],
                            type = TransactionType.entries[it[AccountTransactionSchema.type].toInt()]
                        )
                    }
            }
        }
    }

    override suspend fun getMemberIds(): Collection<UUID> {
        return Collections.singletonList(uuid)
    }

    override suspend fun isMember(player: UUID): Boolean {
        return player == uuid
    }

    override suspend fun setPermissions(
        player: UUID,
        perms: Map<io.github.arcaneplugins.polyconomy.api.account.AccountPermission, Boolean?>,
    ) {
        throw IllegalStateException("Unable to set member permissions for a player account")
    }

    override suspend fun getPermissions(player: UUID): Map<io.github.arcaneplugins.polyconomy.api.account.AccountPermission, Boolean?> {
        return if (player == uuid) {
            AccountPermission.entries.associateWith { true }
        } else {
            AccountPermission.entries.associateWith { false }
        }
    }

    override suspend fun getPermissionsMap(): Map<UUID, Map<io.github.arcaneplugins.polyconomy.api.account.AccountPermission, Boolean?>> {
        return mapOf(
            uuid to AccountPermission.entries.associateWith { true }
        )
    }

    override suspend fun hasPermissions(
        player: UUID,
        permissions: Collection<io.github.arcaneplugins.polyconomy.api.account.AccountPermission>,
    ): Boolean {
        return player == uuid
    }

    override suspend fun addMember(player: UUID) {
        throw IllegalStateException("Unable to add members to a player account")
    }

    override suspend fun removeMember(player: UUID) {
        throw IllegalStateException("Unable to remove members from a player account")
    }

}