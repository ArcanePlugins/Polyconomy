package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.apiImpl

import io.github.arcaneplugins.polyconomy.api.account.AccountTransaction
import io.github.arcaneplugins.polyconomy.api.account.PlayerAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.ExposedStorageHandler
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*

class PlayerAccountImpl(
    val handler: ExposedStorageHandler,
    uuid: UUID,
) : PlayerAccount(uuid) {

    override suspend fun getName(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun setName(newName: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun getBalance(currency: io.github.arcaneplugins.polyconomy.api.currency.Currency): BigDecimal {
        TODO("Not yet implemented")
    }

    override suspend fun makeTransaction(transaction: AccountTransaction) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAccount() {
        TODO("Not yet implemented")
    }

    override suspend fun getHeldCurrencies(): Collection<io.github.arcaneplugins.polyconomy.api.currency.Currency> {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactionHistory(
        maxCount: Int,
        dateFrom: Temporal,
        dateTo: Temporal,
    ): List<AccountTransaction> {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberIds(): Collection<UUID> {
        TODO("Not yet implemented")
    }

    override suspend fun isMember(player: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun setPermissions(
        player: UUID,
        perms: Map<io.github.arcaneplugins.polyconomy.api.account.AccountPermission, Boolean?>,
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getPermissions(player: UUID): Map<io.github.arcaneplugins.polyconomy.api.account.AccountPermission, Boolean?> {
        TODO("Not yet implemented")
    }

    override suspend fun getPermissionsMap(): Map<UUID, Map<io.github.arcaneplugins.polyconomy.api.account.AccountPermission, Boolean?>> {
        TODO("Not yet implemented")
    }

    override suspend fun hasPermissions(
        player: UUID,
        permissions: Collection<io.github.arcaneplugins.polyconomy.api.account.AccountPermission>,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun addMember(player: UUID) {
        TODO("Not yet implemented")
    }

    override suspend fun removeMember(player: UUID) {
        TODO("Not yet implemented")
    }

}