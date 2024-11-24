package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.dao.id.IntIdTable

object Currency : IntIdTable("Currency", "id") {
    val name = varchar("name", 255).uniqueIndex("currency_name_index")
    val enabled = bool("enabled")
    val startingBalance = decimal("starting_balance", 18, 4)
}