package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object NonPlayerAccountSchema : Table("NonPlayerAccount") {
    val id = reference("id", AccountSchema.id, onDelete = ReferenceOption.CASCADE)
    val namespacedKey = varchar("namespaced_key", 255).uniqueIndex("namespaced_key_index")

    override val primaryKey by lazy {
        super.primaryKey ?: PrimaryKey(id)
    }
}