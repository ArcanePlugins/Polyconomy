package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

import io.github.arcaneplugins.polyconomy.api.account.AccountTransaction
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.account.TransactionType
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.cause.Cause
import io.github.arcaneplugins.polyconomy.api.util.cause.CauseType
import io.github.arcaneplugins.polyconomy.api.util.cause.ServerCause
import io.github.arcaneplugins.polyconomy.plugin.core.util.ByteUtil.uuidToBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.Temporal
import java.util.*

class H2PlayerAccount(
    uuid: UUID,
    val handler: H2StorageHandler,
) : PlayerAccount(uuid) {

    override suspend fun getName(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext handler.connection.prepareStatement(H2Statements.getNameOfPlayerAccount)
                .use { statement ->
                    statement.setBytes(1, uuidToBytes(uuid))
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
            handler.connection.prepareStatement(H2Statements.setNameOfPlayerAccount).use { statement ->
                statement.setString(1, newName)
                statement.setBytes(2, uuidToBytes(uuid))
                statement.executeUpdate()
            }
        }
    }

    override suspend fun getBalance(currency: Currency): BigDecimal {
        return withContext(Dispatchers.IO) {
            fun getter(): BigDecimal? {
                return handler.connection.prepareStatement(H2Statements.getBalanceOfPlayerAccount).use { statement ->
                    statement.setBytes(1, uuidToBytes(uuid))
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

    // TODO Move away from using dbId and instead use inner selects to get DB ID when needed for queries
    private fun dbId(): Long {
        return handler.connection.prepareStatement(H2Statements.getPlayerAccountId).use { statement ->
            statement.setBytes(1, uuidToBytes(uuid))
            val rs = statement.executeQuery()
            return@use if (rs.next()) {
                rs.getLong(1)
            } else {
                throw IllegalStateException("Unable to retrieve DB ID")
            }
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
                H2Statements.getTransactionHistoryForPlayerAccount
            ).use { statement ->
                statement.setBytes(1, uuidToBytes(uuid))
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

}