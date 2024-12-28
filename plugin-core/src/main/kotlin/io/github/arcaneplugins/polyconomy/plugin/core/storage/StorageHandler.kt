package io.github.arcaneplugins.polyconomy.plugin.core.storage

import io.github.arcaneplugins.polyconomy.api.Economy
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

abstract class StorageHandler(
    val id: String,
    val manager: StorageManager,
) : Economy {

    companion object {
        val baseTransactionAgePeriod = TimeUnit.SECONDS.convert(
            91,
            TimeUnit.DAYS
        ) // 3 months
    }

    var connected = false
        protected set

    abstract fun startup()

    abstract fun shutdown()

    abstract suspend fun playerCacheGetName(uuid: UUID): String?

    abstract suspend fun playerCacheSetName(uuid: UUID, name: String)

    abstract suspend fun playerCacheIsPlayer(uuid: UUID): Boolean

    suspend fun cleanup() {
        purgeOldTransactions()
    }

    abstract suspend fun purgeOldTransactions()

    abstract suspend fun baltop(
        page: Int,
        pageSize: Int,
        currency: Currency,
    ): Map<String, BigDecimal>

}