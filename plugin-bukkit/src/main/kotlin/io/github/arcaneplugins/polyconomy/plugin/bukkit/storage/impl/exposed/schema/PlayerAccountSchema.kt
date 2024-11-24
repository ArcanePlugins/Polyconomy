package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object PlayerAccountSchema : Table("PlayerAccount") {
    val id = reference("id", AccountSchema.id, onDelete = ReferenceOption.CASCADE)
    val playerUuid = binary("player_uuid", 16).uniqueIndex("player_uuid_index")

    override val primaryKey by lazy {
        super.primaryKey ?: PrimaryKey(id)
    }
}