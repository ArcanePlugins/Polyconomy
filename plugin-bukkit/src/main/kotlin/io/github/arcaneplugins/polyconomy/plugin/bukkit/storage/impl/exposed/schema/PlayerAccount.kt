package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.sql.Table

object PlayerAccount : Table("PlayerAccount") {
    val id = reference("id", Account.id)
    val playerUuid = binary("player_uuid", 16).uniqueIndex("player_uuid_index")

    override val primaryKey by lazy {
        super.primaryKey ?: PrimaryKey(id)
    }
}