package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.sql.Table

object AccountBalance : Table() {
    val accountId = reference("account_id", Account.id)
    val currencyId = reference("currency_id", Currency.id)
    val balance = decimal("balance", 18, 4)

    override val primaryKey by lazy {
        super.primaryKey ?: PrimaryKey(accountId, currencyId)
    }
}