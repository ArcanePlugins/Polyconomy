package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.dao.id.IntIdTable

object AccountTransactionSchema : IntIdTable("id") {
    val accountId = reference("account_id", AccountSchema.id)
    val amount = decimal("amount", 18, 4)
    val currencyId = reference("currency_id", CurrencySchema.id)
    val cause = byte("cause")
    val reason = varchar("reason", 255).nullable()
    val type = byte("type")
    val timestamp = long("timestamp")
}