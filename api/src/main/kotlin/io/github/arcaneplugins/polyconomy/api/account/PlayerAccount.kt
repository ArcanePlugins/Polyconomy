package io.github.arcaneplugins.polyconomy.api.account

import java.util.*

abstract class PlayerAccount(
    val uuid: UUID,
) : Account {

    override suspend fun getMemberIds(): Collection<UUID> {
        return Collections.singletonList(uuid)
    }

    override suspend fun isMember(player: UUID): Boolean {
        return player == uuid
    }

    override suspend fun setPermissions(player: UUID, perms: Map<AccountPermission, Boolean?>) {
        throw IllegalStateException("Cannot set permissions for a player account")
    }

    override suspend fun getPermissions(player: UUID): Map<AccountPermission, Boolean?> {
        return AccountPermission.entries.associateWith { player == uuid }
    }

    override suspend fun getPermissionsMap(): Map<UUID, Map<AccountPermission, Boolean?>> {
        return mapOf(uuid to getPermissions(uuid))
    }

    override suspend fun hasPermissions(player: UUID, permissions: Collection<AccountPermission>): Boolean {
        return player == uuid
    }

    override suspend fun addMember(player: UUID) {
        if (player == uuid) {
            return
        }

        throw IllegalStateException("Unable to add members for a player account")
    }

    override suspend fun removeMember(player: UUID) {
        throw IllegalStateException("Unable to remove members from a player account")
    }

}
