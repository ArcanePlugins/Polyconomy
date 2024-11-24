package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.dao.id.IntIdTable

object CurrencyLocale : IntIdTable("id") {
    val currencyId = reference("currency_id", Currency.id)
    val locale = varchar("locale", 255)
    val displayNameSingular = varchar("display_name_singular", 255)
    val displayNamePlural = varchar("display_name_plural", 255)
    val decimal = varchar("decimal", 32)
}