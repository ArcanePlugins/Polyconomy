package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.sql.Table

object VaultBankAccountSchema : Table("VaultBankAccount") {
    val id = reference("id", NonPlayerAccountSchema.id)
    val ownerString = varchar("owner_string", 255).nullable()
    val ownerUuid = binary("owner_uuid", 16).nullable()

    override val primaryKey by lazy {
        super.primaryKey ?: PrimaryKey(id)
    }
}