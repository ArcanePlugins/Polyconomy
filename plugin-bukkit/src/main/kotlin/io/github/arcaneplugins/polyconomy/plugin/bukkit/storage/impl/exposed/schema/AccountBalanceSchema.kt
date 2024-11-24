package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object AccountBalanceSchema : Table() {
    val accountId = reference("account_id", AccountSchema.id, onDelete = ReferenceOption.CASCADE)
    val currencyId = reference("currency_id", CurrencySchema.id, onDelete = ReferenceOption.CASCADE)
    val balance = decimal("balance", 18, 4)

    override val primaryKey by lazy {
        super.primaryKey ?: PrimaryKey(accountId, currencyId)
    }
}