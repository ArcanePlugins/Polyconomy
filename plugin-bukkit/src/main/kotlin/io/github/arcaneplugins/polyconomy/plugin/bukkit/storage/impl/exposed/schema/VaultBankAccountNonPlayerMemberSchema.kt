package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.sql.Table

object VaultBankAccountNonPlayerMemberSchema : Table("VaultBankAccountNonPlayerMember") {
    val id = reference("id", VaultBankAccountSchema.id)
    val memberIdStr = varchar("member_id_str", 255).nullable()

    override val primaryKey by lazy {
        super.primaryKey ?: PrimaryKey(id, memberIdStr)
    }
}