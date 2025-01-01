package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

import io.github.arcaneplugins.polyconomy.api.account.AccountPermission
import io.github.arcaneplugins.polyconomy.api.account.AccountTransaction
import io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.account.TransactionType
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import io.github.arcaneplugins.polyconomy.api.util.cause.Cause
import io.github.arcaneplugins.polyconomy.api.util.cause.CauseType
import io.github.arcaneplugins.polyconomy.api.util.cause.ServerCause
import io.github.arcaneplugins.polyconomy.plugin.core.util.ByteUtil
import io.github.arcaneplugins.polyconomy.plugin.core.util.ByteUtil.uuidToBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.sql.Types
import java.time.Instant
import java.time.temporal.Temporal
import java.util.*

class H2NonPlayerAccount(
    namespacedKey: NamespacedKey,
    val handler: H2StorageHandler,
) : NonPlayerAccount(
    namespacedKey
) {

    private fun dbId(): Long {
        return handler.connection.prepareStatement(H2Statements.getNonPlayerAccountId).use { statement ->
            statement.setString(1, namespacedKey.toString())
            val rs = statement.executeQuery()
            return@use if (rs.next()) {
                rs.getLong(1)
            } else {
                throw IllegalStateException("Unable to retrieve DB ID")
            }
        }
    }

    override suspend fun isVaultBankAccount(): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext handler.connection.prepareStatement(H2Statements.isVaultBankAccount).use { statement ->
                statement.setString(1, namespacedKey.toString())
                val rs = statement.executeQuery()
                return@use rs.next()
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override suspend fun isLegacyVaultBankOwner(memberId: NamespacedKey): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext handler.connection.prepareStatement(
                H2Statements.isLegacyVaultBankOwner
            ).use { statement ->
                statement.setString(1, namespacedKey.toString())
                statement.setString(2, memberId.toString())
                val rs = statement.executeQuery()
                return@use rs.next()
            }
        }
    }

    override suspend fun isVaultBankOwner(memberId: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext handler.connection.prepareStatement(
                H2Statements.isVaultBankOwner
            ).use { statement ->
                statement.setString(1, namespacedKey.toString())
                statement.setBytes(2, uuidToBytes(memberId))
                val rs = statement.executeQuery()
                return@use rs.next()
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override suspend fun isLegacyVaultBankMember(memberId: NamespacedKey): Boolean {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.isLegacyVaultBankMember).use { statement ->
                statement.setString(1, namespacedKey.toString())
                statement.setString(2, memberId.toString())
                val rs = statement.executeQuery()
                return@use rs.next()
            }
        }
    }

    override suspend fun setLegacyVaultBankOwner(ownerId: NamespacedKey) {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.setLegacyVaultBankOwner).use { statement ->
                statement.setString(1, ownerId.toString())
                statement.setString(2, namespacedKey.toString())
                statement.executeUpdate()
            }
        }
    }

    override suspend fun setVaultBankOwner(ownerId: UUID) {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.setVaultBankOwner).use { statement ->
                statement.setBytes(1, uuidToBytes(ownerId))
                statement.setString(2, namespacedKey.toString())
                statement.executeUpdate()
            }
        }
    }

    override suspend fun getName(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext handler.connection.prepareStatement(H2Statements.getNameOfNonPlayerAccount)
                .use { statement ->
                    statement.setString(1, namespacedKey.toString())
                    val rs = statement.executeQuery()

                    return@use if (rs.next()) {
                        rs.getString(1)
                    } else {
                        null
                    }
                }
        }
    }

    override suspend fun setName(newName: String?) {
        withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.setNameOfNonPlayerAccount).use { statement ->
                statement.setString(1, newName)
                statement.setString(2, namespacedKey.toString())
                statement.executeUpdate()
            }
        }
    }

    override suspend fun getBalance(currency: Currency): BigDecimal {
        return withContext(Dispatchers.IO) {
            fun getter(): BigDecimal? {
                return handler.connection.prepareStatement(H2Statements.getBalanceOfNonPlayerAccount).use { statement ->
                    statement.setString(1, namespacedKey.toString())
                    statement.setString(2, currency.name)
                    val rs = statement.executeQuery()

                    return@use if (rs.next()) {
                        rs.getBigDecimal(1)
                    } else {
                        null
                    }
                }
            }

            val bal = getter()
            if (bal != null) {
                return@withContext bal
            }
            val startingBal = currency.getStartingBalance()
            runBlocking {
                makeBlindTransaction(
                    transaction = AccountTransaction(
                        amount = startingBal,
                        cause = ServerCause,
                        importance = TransactionImportance.HIGH,
                        reason = "Generated as required",
                        currency = currency,
                        timestamp = Instant.now(),
                        type = TransactionType.RESET,
                    ),
                    previousBalance = BigDecimal.ZERO
                )
            }

            startingBal
        }
    }

    override suspend fun makeTransaction(transaction: AccountTransaction) {
        makeBlindTransaction(transaction, getBalance(transaction.currency))
    }

    // makes a transaction without automatically fetching current balance.
    // this is important when initially setting the balance via a transaction as the account's balance won't exist yet!
    private suspend fun makeBlindTransaction(
        transaction: AccountTransaction,
        previousBalance: BigDecimal,
    ) {
        withContext(Dispatchers.IO) {
            val resultingBalance = transaction.resultingBalance(previousBalance)
            val accountDbId = dbId()
            val currencyDbId = handler.getCurrencyDbId(transaction.currency.name)

            handler.connection.prepareStatement(H2Statements.setAccountBalance).use { statement ->
                statement.setLong(1, accountDbId)
                statement.setLong(2, currencyDbId)
                statement.setBigDecimal(3, resultingBalance)
                statement.executeUpdate()
            }

            handler.connection.prepareStatement(H2Statements.insertTransaction).use { statement ->
                statement.setLong(1, accountDbId)
                statement.setBigDecimal(2, resultingBalance)
                statement.setLong(3, currencyDbId)
                statement.setShort(4, transaction.cause.type.ordinal.toShort())
                statement.setString(5, transaction.cause.data.toString().take(255))
                statement.setString(6, transaction.reason)
                statement.setShort(7, transaction.importance.ordinal.toShort())
                statement.setShort(8, transaction.type.ordinal.toShort())
                statement.setLong(9, transaction.timestamp.epochSecond)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun deleteAccount() {
        withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.deleteAccount).use { statement ->
                statement.setLong(1, dbId())
                statement.executeUpdate()
            }
        }
    }

    override suspend fun getHeldCurrencies(): Collection<Currency> {
        return withContext(Dispatchers.IO) {
            return@withContext handler.connection.prepareStatement(H2Statements.getHeldCurrencies).use { statement ->
                statement.setLong(1, dbId())
                val rs = statement.executeQuery()
                val currencies = mutableSetOf<Currency>()

                while (rs.next()) {
                    val name = rs.getString(1)
                    currencies.add(
                        handler.getCurrency(name)
                            ?: throw IllegalStateException("Unable to find currency by name: ${name}")
                    )
                }

                return@use currencies
            }
        }
    }

    override suspend fun getTransactionHistory(
        maxCount: Int,
        dateFrom: Temporal,
        dateTo: Temporal,
    ): List<AccountTransaction> {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(
                H2Statements.getTransactionHistoryForNonPlayerAccount
            ).use { statement ->
                statement.setString(1, namespacedKey.toString())
                statement.setLong(2, Instant.from(dateFrom).epochSecond)
                statement.setLong(3, Instant.from(dateTo).epochSecond)
                val rs = statement.executeQuery()
                val history = mutableListOf<AccountTransaction>()
                while (rs.next()) {
                    val amount = rs.getBigDecimal(1)
                    val currency = H2Currency(rs.getString(2), handler)
                    val cause = Cause.serialize(
                        type = CauseType.entries[rs.getShort(3).toInt()],
                        data = rs.getString(4),
                    )
                    val reason: String? = rs.getString(5)
                    val importance = TransactionImportance.entries[rs.getShort(6).toInt()]
                    val type = TransactionType.entries[rs.getShort(7).toInt()]
                    val timestamp = Instant.ofEpochSecond(rs.getLong(8))

                    history.add(
                        AccountTransaction(
                            amount,
                            currency,
                            cause,
                            reason,
                            importance,
                            type,
                            timestamp,
                        )
                    )
                }
                return@use history
            }
        }
    }

    override suspend fun getMemberIds(): Collection<UUID> {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.getNonPlayerAccountMemberIds).use { statement ->
                statement.setString(1, namespacedKey.toString())
                val rs = statement.executeQuery()
                val ids = mutableSetOf<UUID>()
                while (rs.next()) {
                    ids.add(
                        ByteUtil.bytesToUuid(
                            rs.getBytes(1)
                        )
                    )
                }
                ids
            }
        }
    }

    override suspend fun isMember(player: UUID): Boolean {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(H2Statements.isMemberOfNonPlayerAccount).use { statement ->
                statement.setString(1, namespacedKey.toString())
                statement.setBytes(2, uuidToBytes(player))
                return@use statement.executeQuery().next()
            }
        }
    }

    override suspend fun setPermissions(player: UUID, perms: Map<AccountPermission, Boolean?>) {
        withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(
                H2Statements.setPermissionsOfNonPlayerAccountMember
            ).use { statement ->
                statement.setString(1, namespacedKey.toString())
                statement.setBytes(2, uuidToBytes(player))
                listOf(
                    AccountPermission.BALANCE,
                    AccountPermission.WITHDRAW,
                    AccountPermission.DEPOSIT,
                    AccountPermission.MODIFY_PERMISSIONS,
                    AccountPermission.OWNER,
                    AccountPermission.TRANSFER_OWNERSHIP,
                    AccountPermission.INVITE_MEMBER,
                    AccountPermission.REMOVE_MEMBER,
                    AccountPermission.DELETE,
                ).forEachIndexed { index, perm ->
                    val state: Boolean? = perms[perm]
                    if (state != null) {
                        statement.setBoolean(index + 3, state)
                    } else {
                        statement.setNull(index + 3, Types.BOOLEAN)
                    }
                }

                statement.executeUpdate()
            }
        }
    }

    override suspend fun getPermissions(player: UUID): Map<AccountPermission, Boolean?> {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(
                H2Statements.getPermissionsOfNonPlayerAccountMember
            ).use { statement ->
                statement.setString(1, namespacedKey.toString())
                statement.setBytes(2, uuidToBytes(player))
                val rs = statement.executeQuery()
                if (!rs.next()) {
                    throw IllegalStateException("${player} is not a member of ${namespacedKey}")
                }
                return@use mapOf(
                    AccountPermission.BALANCE to rs.getBoolean(3),
                    AccountPermission.WITHDRAW to rs.getBoolean(4),
                    AccountPermission.DEPOSIT to rs.getBoolean(5),
                    AccountPermission.MODIFY_PERMISSIONS to rs.getBoolean(6),
                    AccountPermission.OWNER to rs.getBoolean(7),
                    AccountPermission.TRANSFER_OWNERSHIP to rs.getBoolean(8),
                    AccountPermission.INVITE_MEMBER to rs.getBoolean(9),
                    AccountPermission.REMOVE_MEMBER to rs.getBoolean(10),
                    AccountPermission.DELETE to rs.getBoolean(11),
                )
            }
        }
    }

    override suspend fun getPermissionsMap(): Map<UUID, Map<AccountPermission, Boolean?>> {
        return withContext(Dispatchers.IO) {
            getMemberIds().associateWith { getPermissions(it) }
        }
    }

    override suspend fun hasPermissions(player: UUID, permissions: Collection<AccountPermission>): Boolean {
        return withContext(Dispatchers.IO) {
            val perms = getPermissions(player)
            permissions.all { perms[it] ?: it.defaultValue }
        }
    }

    override suspend fun addMember(player: UUID) {
        return withContext(Dispatchers.IO) {
            setPermissions(player, Collections.emptyMap())
        }
    }

    override suspend fun removeMember(player: UUID) {
        return withContext(Dispatchers.IO) {
            handler.connection.prepareStatement(
                H2Statements.deleteMemberOfNonPlayerAccount
            ).use { statement ->
                statement.setString(1, namespacedKey.toString())
                statement.setBytes(2, uuidToBytes(player))
                statement.executeUpdate()
            }
        }
    }
}