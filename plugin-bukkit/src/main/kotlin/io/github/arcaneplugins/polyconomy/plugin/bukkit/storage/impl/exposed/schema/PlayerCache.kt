package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.sql.Table

object PlayerCache : Table() {
    val playerUuid = binary("player_uuid", 16)
    val username = varchar("username", 255)

    override val primaryKey by lazy {
        super.primaryKey ?: PrimaryKey(playerUuid)
    }
}