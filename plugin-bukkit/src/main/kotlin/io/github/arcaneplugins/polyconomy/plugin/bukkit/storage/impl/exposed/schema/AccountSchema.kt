package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.dao.id.IntIdTable

object AccountSchema : IntIdTable("Account") {
    val name = varchar("name", 255).nullable()
}