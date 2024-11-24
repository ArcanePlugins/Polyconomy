package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.dao.id.IntIdTable

object AccountTransaction : IntIdTable("id") {
    val accountId = reference("account_id", Account.id)
    val amount = decimal("amount", 18, 4)
    val currencyId = reference("currency_id", Currency.id)
    val cause = byte("cause")
    val reason = varchar("reason", 255).nullable()
    val type = byte("type")
    val timestamp = long("timestamp")
}