package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.dao.id.IntIdTable

object Account : IntIdTable("Account", "id") {
    val name = varchar("name", 255)
}