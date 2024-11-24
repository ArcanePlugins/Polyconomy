package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.sql.Table

object VaultBankAccountNonPlayerMember : Table("VaultBankAccountNonPlayerMember") {
    val id = reference("id", VaultBankAccount.id)
    val memberIdStr = varchar("member_id_str", 255).nullable()

    override val primaryKey by lazy {
        super.primaryKey ?: PrimaryKey(id, memberIdStr)
    }
}