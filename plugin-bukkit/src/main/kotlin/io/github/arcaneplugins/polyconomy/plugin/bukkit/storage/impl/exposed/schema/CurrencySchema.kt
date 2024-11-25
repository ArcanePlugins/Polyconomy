package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.dao.id.IntIdTable

object CurrencySchema : IntIdTable("Currency") {
    val name = varchar("name", 255).uniqueIndex("currency_name_index")
    val startingBalance = decimal("starting_balance", 18, 4)
    val symbol = varchar("symbol", 32)
    val amountFormat = varchar("amount_format", 255)
    val presentationFormat = varchar("presentation_format", 1023)
    val conversionRate = decimal("conversion_rate", 18, 4)
}