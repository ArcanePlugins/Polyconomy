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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.Temporal
import java.util.*

class H2NonPlayerAccount(
    namespacedKey: NamespacedKey,
    val handler: H2StorageHandler,
) : NonPlayerAccount(
    namespacedKey
) {

    // TODO Use
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
        TODO("Not yet implemented")
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override suspend fun isLegacyVaultBankOwner(memberId: NamespacedKey): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun isVaultBankOwner(memberId: UUID): Boolean {
        TODO("Not yet implemented")
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override suspend fun isLegacyVaultBankMember(memberId: NamespacedKey): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun setLegacyVaultBankOwner(ownerId: NamespacedKey) {
        TODO("Not yet implemented")
    }

    override suspend fun setVaultBankOwner(ownerId: UUID) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override suspend fun makeTransaction(transaction: AccountTransaction) {
        TODO("Not yet implemented")
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

    override suspend fun addMember(player: UUID) {
        TODO("Not yet implemented")
    }

    override suspend fun removeMember(player: UUID) {
        TODO("Not yet implemented")
    }
}