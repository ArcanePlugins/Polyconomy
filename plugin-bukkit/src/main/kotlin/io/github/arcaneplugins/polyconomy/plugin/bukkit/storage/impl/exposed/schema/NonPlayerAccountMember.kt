package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema

import org.jetbrains.exposed.sql.Table

object NonPlayerAccountMember : Table("NonPlayerAccountMember") {
    val accountId = reference("account_id", Account.id)
    val memberId = binary("member_id", 16)
    val permBalance = bool("perm_balance").nullable()
    val permWithdraw = bool("perm_withdraw").nullable()
    val permDeposit = bool("perm_deposit").nullable()
    val permModifyPerms = bool("perm_modify_perms").nullable()
    val permOwner = bool("perm_owner").nullable()
    val permTransferOwnership = bool("perm_transfer_ownership").nullable()
    val permInviteMember = bool("perm_invite_member").nullable()
    val permRemoveMember = bool("perm_remove_member").nullable()
    val permDelete = bool("perm_delete").nullable()

    override val primaryKey by lazy {
        super.primaryKey ?: PrimaryKey(accountId, memberId)
    }
}