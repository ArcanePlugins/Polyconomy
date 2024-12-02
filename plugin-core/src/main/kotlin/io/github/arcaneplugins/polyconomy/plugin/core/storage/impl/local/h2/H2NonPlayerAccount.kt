package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

import io.github.arcaneplugins.polyconomy.api.account.AccountPermission
import io.github.arcaneplugins.polyconomy.api.account.AccountTransaction
import io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*

class H2NonPlayerAccount(
    namespacedKey: NamespacedKey
) : NonPlayerAccount(
    namespacedKey
) {
    override suspend fun isVaultBankAccount(): Boolean {
        TODO("Not yet implemented")
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override suspend fun isLegacyVaultBankOwner(memberId: NamespacedKey): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun isVaultBankOwner(memberId: UUID): Boolean {
        TODO("Not yet implemented")
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override suspend fun isLegacyVaultBankMember(memberId: NamespacedKey): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun setLegacyVaultBankOwner(ownerId: NamespacedKey) {
        TODO("Not yet implemented")
    }

    override suspend fun setVaultBankOwner(ownerId: UUID) {
        TODO("Not yet implemented")
    }

    override suspend fun getName(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun setName(newName: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun getBalance(currency: Currency): BigDecimal {
        TODO("Not yet implemented")
    }

    override suspend fun makeTransaction(transaction: AccountTransaction) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAccount() {
        TODO("Not yet implemented")
    }

    override suspend fun getHeldCurrencies(): Collection<Currency> {
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

    override suspend fun setPermissions(player: UUID, perms: Map<AccountPermission, Boolean?>) {
        TODO("Not yet implemented")
    }

    override suspend fun getPermissions(player: UUID): Map<AccountPermission, Boolean?> {
        TODO("Not yet implemented")
    }

    override suspend fun getPermissionsMap(): Map<UUID, Map<AccountPermission, Boolean?>> {
        TODO("Not yet implemented")
    }

    override suspend fun hasPermissions(player: UUID, permissions: Collection<AccountPermission>): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun addMember(player: UUID) {
        TODO("Not yet implemented")
    }

    override suspend fun removeMember(player: UUID) {
        TODO("Not yet implemented")
    }
}