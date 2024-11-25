package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object AccountTransactionSchema : IntIdTable("AccountTransaction") {
    val accountId = reference("account_id", AccountSchema.id, onDelete = ReferenceOption.CASCADE)
    val amount = decimal("amount", 18, 4)
    val currencyId = reference("currency_id", CurrencySchema.id, onDelete = ReferenceOption.CASCADE)
    val causeType = byte("cause_type")
    val causeData = varchar("cause_data", 1023)
    val reason = varchar("reason", 255).nullable()
    val type = byte("type")
    val timestamp = long("timestamp")
    val importance = byte("importance")
}