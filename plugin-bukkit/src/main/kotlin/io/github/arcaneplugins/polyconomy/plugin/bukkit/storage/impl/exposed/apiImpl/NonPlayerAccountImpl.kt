package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.apiImpl

import io.github.arcaneplugins.polyconomy.api.account.AccountPermission
import io.github.arcaneplugins.polyconomy.api.account.AccountTransaction
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
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.AccountTransactionSchema.importance
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.CurrencySchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.NonPlayerAccountMemberSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.NonPlayerAccountSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.VaultBankAccountNonPlayerMemberSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.VaultBankAccountSchema
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.ByteUtil
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

class NonPlayerAccountImpl(
    val handler: ExposedStorageHandler,
    nsKey: NamespacedKey,
) : io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount(nsKey) {

    override suspend fun isVaultBankAccount(): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                VaultBankAccountSchema
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = VaultBankAccountSchema.id,
                        otherColumn = NonPlayerAccountSchema.id
                    )
                    .select(NonPlayerAccountSchema.namespacedKey)
                    .where { NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString() }
                    .any()
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override suspend fun isLegacyVaultBankOwner(memberId: NamespacedKey): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                VaultBankAccountSchema
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = VaultBankAccountSchema.id,
                        otherColumn = NonPlayerAccountSchema.id
                    )
                    .select(NonPlayerAccountSchema.namespacedKey, VaultBankAccountSchema.ownerString)
                    .where {
                        (NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString()) and
                                (VaultBankAccountSchema.ownerString eq memberId.toString())
                    }
                    .any()
            }
        }
    }

    override suspend fun isVaultBankOwner(memberId: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                VaultBankAccountSchema
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = VaultBankAccountSchema.id,
                        otherColumn = NonPlayerAccountSchema.id
                    )
                    .select(NonPlayerAccountSchema.namespacedKey, VaultBankAccountSchema.ownerUuid)
                    .where {
                        (NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString()) and
                                (VaultBankAccountSchema.ownerUuid eq uuidToBytes(memberId))
                    }
                    .any()
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override suspend fun isLegacyVaultBankMember(memberId: NamespacedKey): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                VaultBankAccountSchema
                    .join(
                        otherTable = VaultBankAccountNonPlayerMemberSchema,
                        joinType = JoinType.INNER,
                        onColumn = VaultBankAccountSchema.id,
                        otherColumn = VaultBankAccountNonPlayerMemberSchema.id
                    )
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = VaultBankAccountSchema.id,
                        otherColumn = NonPlayerAccountSchema.id
                    )
                    .select(NonPlayerAccountSchema.namespacedKey)
                    .where {
                        (NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString()) and
                                (VaultBankAccountNonPlayerMemberSchema.memberIdStr eq memberId.toString())
                    }
                    .any()
            }
        }
    }

    override suspend fun setLegacyVaultBankOwner(ownerId: NamespacedKey) {
        return withContext(Dispatchers.IO) {
            transaction {
                VaultBankAccountSchema
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = VaultBankAccountSchema.id,
                        otherColumn = NonPlayerAccountSchema.id
                    )
                    .update(
                        where = {
                            NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString()
                        }
                    ) {
                        it[VaultBankAccountSchema.ownerString] = ownerId.toString()
                    }
            }
        }
    }

    override suspend fun setVaultBankOwner(ownerId: UUID) {
        return withContext(Dispatchers.IO) {
            transaction {
                VaultBankAccountSchema
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = VaultBankAccountSchema.id,
                        otherColumn = NonPlayerAccountSchema.id
                    )
                    .update(
                        where = {
                            NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString()
                        }
                    ) {
                        it[VaultBankAccountSchema.ownerUuid] = uuidToBytes(ownerId)
                    }
            }
        }
    }

    override suspend fun getName(): String? {
        return withContext(Dispatchers.IO) {
            transaction {
                NonPlayerAccountSchema
                    .join(
                        otherTable = AccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = NonPlayerAccountSchema.id,
                        otherColumn = AccountSchema.id,
                    )
                    .select(AccountSchema.name)
                    .where { NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString() }
                    .map { it[AccountSchema.name] }
                    .firstOrNull()
            }
        }
    }

    override suspend fun setName(newName: String?) {
        return withContext(Dispatchers.IO) {
            transaction {
                NonPlayerAccountSchema
                    .join(
                        otherTable = AccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = NonPlayerAccountSchema.id,
                        otherColumn = AccountSchema.id,
                    )
                    .update(
                        where = {
                            NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString()
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
                NonPlayerAccountSchema
                    .join(
                        otherTable = AccountBalanceSchema,
                        joinType = JoinType.INNER,
                        onColumn = NonPlayerAccountSchema.id,
                        otherColumn = AccountBalanceSchema.accountId,
                    )
                    .join(
                        otherTable = CurrencySchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountBalanceSchema.currencyId,
                        otherColumn = CurrencySchema.id
                    )
                    .select(AccountBalanceSchema.balance)
                    .where { CurrencySchema.name eq currency.name }
                    .map { it[AccountBalanceSchema.balance] }
                    .first()
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
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountBalanceSchema.accountId,
                        otherColumn = NonPlayerAccountSchema.id
                    )
                    .join(
                        otherTable = CurrencySchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountBalanceSchema.currencyId,
                        otherColumn = CurrencySchema.id
                    )
                    .update(
                        where = {
                            (NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString()) and
                                    (CurrencySchema.name eq transaction.currency.name)
                        }
                    ) {
                        it[AccountBalanceSchema.balance] = newBalance
                    }

                // insert record to transaction history
                val accountId = NonPlayerAccountSchema
                    .select(NonPlayerAccountSchema.id)
                    .where { NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString() }
                    .map { it[NonPlayerAccountSchema.id] }
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
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = NonPlayerAccountSchema.id,
                        otherColumn = AccountSchema.id,
                    )
                    .delete(AccountSchema, where = {
                        NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString()
                    })
            }
        }
    }

    override suspend fun getHeldCurrencies(): Collection<Currency> {
        return withContext(Dispatchers.IO) {
            transaction {
                NonPlayerAccountSchema
                    .join(
                        otherTable = AccountBalanceSchema,
                        joinType = JoinType.INNER,
                        onColumn = NonPlayerAccountSchema.id,
                        otherColumn = AccountBalanceSchema.accountId,
                    )
                    .join(
                        otherTable = CurrencySchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountBalanceSchema.currencyId,
                        otherColumn = CurrencySchema.id
                    )
                    .select(CurrencySchema.name)
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
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountTransactionSchema.accountId,
                        otherColumn = NonPlayerAccountSchema.id,
                    )
                    .join(
                        otherTable = CurrencySchema,
                        joinType = JoinType.INNER,
                        onColumn = AccountTransactionSchema.currencyId,
                        otherColumn = CurrencySchema.id
                    )
                    .selectAll()
                    .where { NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString() }
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
                            importance = TransactionImportance.entries[it[importance].toInt()],
                            type = TransactionType.entries[it[AccountTransactionSchema.type].toInt()]
                        )
                    }
            }
        }
    }

    override suspend fun getMemberIds(): Collection<UUID> {
        return withContext(Dispatchers.IO) {
            transaction {
                NonPlayerAccountMemberSchema
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = NonPlayerAccountMemberSchema.accountId,
                        otherColumn = NonPlayerAccountSchema.id
                    )
                    .select(NonPlayerAccountMemberSchema.memberId)
                    .where { NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString() }
                    .map { ByteUtil.bytesToUuid(it[NonPlayerAccountMemberSchema.memberId]) }
            }
        }
    }

    override suspend fun isMember(player: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                NonPlayerAccountMemberSchema
                    .join(
                        otherTable = NonPlayerAccountSchema,
                        joinType = JoinType.INNER,
                        onColumn = NonPlayerAccountMemberSchema.accountId,
                        otherColumn = NonPlayerAccountSchema.id
                    )
                    .select(NonPlayerAccountMemberSchema.memberId)
                    .where {
                        (NonPlayerAccountSchema.namespacedKey eq namespacedKey.toString()) and
                                (NonPlayerAccountMemberSchema.memberId eq uuidToBytes(player))
                    }
                    .any()
            }
        }
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

    override suspend fun addMember(player: UUID) {
        TODO("Not yet implemented")
    }

    override suspend fun removeMember(player: UUID) {
        TODO("Not yet implemented")
    }

}