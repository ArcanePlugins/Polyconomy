package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

import io.github.arcaneplugins.polyconomy.api.account.AccountTransaction
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.cause.ServerCause
import io.github.arcaneplugins.polyconomy.plugin.core.util.ByteUtil.uuidToBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*

class H2PlayerAccount(
    uuid: UUID,
    val handler: H2StorageHandler,
) : PlayerAccount(uuid) {

    override suspend fun getName(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext handler.connection.prepareStatement(H2Statements.getNameOfPlayerAccount).use { statement ->
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

            var bal = getter()
            if (bal != null) {
                return@withContext bal
            }
            resetBalance(currency, ServerCause, TransactionImportance.HIGH, "Generated as required")
            bal = getter()

            return@withContext bal
                ?: throw IllegalStateException("Unable to get balance record even after resetting it")
        }
    }

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
        withContext(Dispatchers.IO) {
            val previousBalance = getBalance(transaction.currency)
            val resultingBalance = transaction.resultingBalance(previousBalance)
            val accountDbId = dbId()
            val currencyDbId = handler.getCurrencyDbId(transaction.currency.name)

            handler.connection.prepareStatement(H2Statements.setAccountBalance).use { statement ->
                statement.setLong(1, accountDbId)
                statement.setBigDecimal(2, transaction.amount)
                statement.setLong(3, currencyDbId)
                statement.executeUpdate()
            }

            handler.connection.prepareStatement(H2Statements.insertTransaction).use { statement ->
                statement.setLong(1, accountDbId)
                statement.setLong(2, currencyDbId)
                statement.setBigDecimal(3, resultingBalance)
                statement.setShort(4, transaction.cause.type.ordinal.toShort())
                statement.setString(5, transaction.cause.data.toString())
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
        TODO("Not yet implemented")
    }

}