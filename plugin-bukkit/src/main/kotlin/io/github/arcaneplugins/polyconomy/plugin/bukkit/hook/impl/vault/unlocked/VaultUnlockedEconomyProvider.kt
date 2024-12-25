package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault.unlocked

import io.github.arcaneplugins.polyconomy.api.Economy.Companion.PRECISION
import io.github.arcaneplugins.polyconomy.api.account.Account
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import io.github.arcaneplugins.polyconomy.api.util.cause.PluginCause
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault.legacy.VaultLegacyEconomyProvider
import io.github.arcaneplugins.polyconomy.plugin.core.util.StdKey.VU_NAMESPACE_FOR_SHARED_ACCOUNTS
import io.github.arcaneplugins.polyconomy.plugin.core.util.StdKey.VU_NAMESPACE_FOR_STANDARD_ACCOUNTS
import kotlinx.coroutines.runBlocking
import net.milkbowl.vault2.economy.Economy
import net.milkbowl.vault2.economy.EconomyResponse
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*
import io.github.arcaneplugins.polyconomy.api.account.AccountPermission as PolyAccountPermission
import net.milkbowl.vault2.economy.AccountPermission as VuAccountPermission

class VaultUnlockedEconomyProvider(
    plugin: Polyconomy,
) : VaultLegacyEconomyProvider(plugin), Economy {

    companion object {
        private val vaultUnlockedCause = PluginCause(NamespacedKey("vault-unlocked", "cause"))

        enum class VuAccountType {
            PLAYER,
            NON_PLAYER_STANDARD,
            NON_PLAYER_SHARED,
        }
    }

    private fun vuNskForStdAccount(uuid: UUID): NamespacedKey {
        return NamespacedKey(VU_NAMESPACE_FOR_STANDARD_ACCOUNTS, uuid.toString())
    }

    private fun vuNskForSharedAccount(uuid: UUID): NamespacedKey {
        return NamespacedKey(VU_NAMESPACE_FOR_SHARED_ACCOUNTS, uuid.toString())
    }

    private suspend fun getAccountTypeByUuid(
        isPlayer: Boolean?,
        accountId: UUID,
    ): VuAccountType {
        if (isPlayer == true) {
            return VuAccountType.PLAYER
        }

        if (Bukkit.getOnlinePlayers().any { it.uniqueId == accountId } ||
            storageHandler().hasPlayerAccount(accountId) ||
            storageHandler().playerCacheIsPlayer(accountId)
        ) {
            return VuAccountType.PLAYER
        }

        if (storageHandler().hasNonPlayerAccount(vuNskForStdAccount(accountId))) {
            return VuAccountType.NON_PLAYER_STANDARD
        }

        if (storageHandler().hasNonPlayerAccount(vuNskForSharedAccount(accountId))) {
            return VuAccountType.NON_PLAYER_SHARED
        }

        return if (try {
                Bukkit.getOfflinePlayer(accountId).hasPlayedBefore()
            } catch (ignored: Exception) {
                false
            }
        ) {
            VuAccountType.PLAYER
        } else {
            VuAccountType.NON_PLAYER_STANDARD
        }
    }

    private suspend fun getAccountByUuid(
        isPlayer: Boolean?,
        accountId: UUID,
        name: String? = accountId.toString(),
    ): Account {
        return when (getAccountTypeByUuid(isPlayer, accountId)) {
            VuAccountType.PLAYER -> storageHandler().getOrCreatePlayerAccount(accountId, name)
            VuAccountType.NON_PLAYER_STANDARD -> storageHandler().getOrCreateNonPlayerAccount(
                vuNskForStdAccount(
                    accountId
                ), name
            )

            VuAccountType.NON_PLAYER_SHARED -> storageHandler().getOrCreateNonPlayerAccount(
                vuNskForSharedAccount(
                    accountId
                ), name
            )
        }
    }

    @Suppress("unused")
    private fun polyAccountPermToVu(perm: PolyAccountPermission): VuAccountPermission {
        return when (perm) {
            PolyAccountPermission.MODIFY_PERMISSIONS -> VuAccountPermission.CHANGE_MEMBER_PERMISSION
            PolyAccountPermission.WITHDRAW -> VuAccountPermission.WITHDRAW
            PolyAccountPermission.DEPOSIT -> VuAccountPermission.DEPOSIT
            PolyAccountPermission.BALANCE -> VuAccountPermission.BALANCE
            PolyAccountPermission.DELETE -> VuAccountPermission.DELETE
            PolyAccountPermission.INVITE_MEMBER -> VuAccountPermission.INVITE_MEMBER
            PolyAccountPermission.REMOVE_MEMBER -> VuAccountPermission.REMOVE_MEMBER
            PolyAccountPermission.OWNER -> VuAccountPermission.OWNER
            PolyAccountPermission.TRANSFER_OWNERSHIP -> VuAccountPermission.TRANSFER_OWNERSHIP
        }
    }

    private fun polyAccountPermFromVu(perm: VuAccountPermission): PolyAccountPermission {
        return when (perm) {
            VuAccountPermission.CHANGE_MEMBER_PERMISSION -> PolyAccountPermission.MODIFY_PERMISSIONS
            VuAccountPermission.WITHDRAW -> PolyAccountPermission.WITHDRAW
            VuAccountPermission.DEPOSIT -> PolyAccountPermission.DEPOSIT
            VuAccountPermission.BALANCE -> PolyAccountPermission.BALANCE
            VuAccountPermission.DELETE -> PolyAccountPermission.DELETE
            VuAccountPermission.INVITE_MEMBER -> PolyAccountPermission.INVITE_MEMBER
            VuAccountPermission.REMOVE_MEMBER -> PolyAccountPermission.REMOVE_MEMBER
            VuAccountPermission.OWNER -> PolyAccountPermission.OWNER
            VuAccountPermission.TRANSFER_OWNERSHIP -> PolyAccountPermission.TRANSFER_OWNERSHIP
        }
    }

    override fun hasSharedAccountSupport(): Boolean {
        return true
    }

    override fun hasMultiCurrencySupport(): Boolean {
        return true
    }

    override fun fractionalDigits(pluginName: String): Int {
        return PRECISION
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun format(amount: BigDecimal): String {
        return runBlocking {
            primaryCurrency().format(amount, Locale.getDefault())
        }
    }

    override fun format(
        pluginName: String,
        amount: BigDecimal,
    ): String {
        return format(amount)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun format(
        amount: BigDecimal,
        currency: String,
    ): String {
        return runBlocking {
            storageHandler()
                .getCurrency(currency)
                ?.format(amount, Locale.getDefault())
                ?: throw IllegalArgumentException("No currency enabled and active named ${currency}")
        }
    }

    override fun format(
        pluginName: String,
        amount: BigDecimal,
        currency: String,
    ): String {
        return format(amount, currency)
    }

    override fun hasCurrency(currency: String): Boolean {
        return runBlocking {
            storageHandler().getCurrencies().any { it.name == currency }
        }
    }

    override fun getDefaultCurrency(pluginName: String?): String {
        return runBlocking {
            primaryCurrency().name
        }
    }

    override fun defaultCurrencyNamePlural(
        pluginName: String?,
    ): String {
        return runBlocking {
            primaryCurrency().getDisplayName(true, Locale.getDefault())
        }
    }

    override fun defaultCurrencyNameSingular(
        pluginName: String?,
    ): String {
        return runBlocking {
            primaryCurrency().getDisplayName(false, Locale.getDefault())
        }
    }

    override fun currencies(): Collection<String> {
        return runBlocking {
            storageHandler().getCurrencies().map { it.name }
        }
    }

    @Deprecated(message = "Does not identify player vs non-player accounts")
    override fun createAccount(
        accountID: UUID,
        name: String,
    ): Boolean {
        return runBlocking {
            getAccountByUuid(null, accountID, name)
            true
        }
    }

    override fun createAccount(accountID: UUID, name: String, player: Boolean): Boolean {
        return runBlocking {
            getAccountByUuid(player, accountID, name)
            true
        }
    }

    @Deprecated(message = "Does not identify player vs non-player accounts")
    override fun createAccount(
        accountID: UUID,
        name: String,
        worldName: String,
    ): Boolean {
        @Suppress("DEPRECATION")
        return createAccount(accountID, name)
    }

    override fun createAccount(accountID: UUID, name: String, worldName: String, player: Boolean): Boolean {
        return createAccount(accountID, name, player)
    }

    override fun getUUIDNameMap(): Map<UUID, String> {
        return runBlocking {
            storageHandler().getVaultUnlockedUuidNameMap()
        }
    }

    override fun getAccountName(
        accountId: UUID,
    ): Optional<String> {
        return runBlocking {
            Optional.ofNullable(getAccountByUuid(null, accountId).getName())
        }
    }

    override fun hasAccount(
        accountID: UUID,
    ): Boolean {
        return runBlocking {
            storageHandler().hasPlayerAccount(accountID) ||
                    storageHandler().hasNonPlayerAccount(vuNskForStdAccount(accountID))
        }
    }

    override fun hasAccount(
        accountID: UUID,
        worldName: String,
    ): Boolean {
        return hasAccount(accountID)
    }

    override fun renameAccount(
        accountID: UUID,
        name: String,
    ): Boolean {
        return runBlocking {
            getAccountByUuid(null, accountID, name).setName(name)
            true
        }
    }

    override fun renameAccount(
        plugin: String,
        accountID: UUID,
        name: String,
    ): Boolean {
        return renameAccount(accountID, name)
    }

    override fun deleteAccount(
        plugin: String,
        accountID: UUID,
    ): Boolean {
        return runBlocking {
            getAccountByUuid(null, accountID).deleteAccount()
            true
        }
    }

    override fun accountSupportsCurrency(
        plugin: String,
        accountID: UUID,
        currency: String,
    ): Boolean {
        return runBlocking {
            storageHandler().getCurrencies().any { it.name == currency }
        }
    }

    override fun accountSupportsCurrency(
        plugin: String,
        accountID: UUID,
        currency: String,
        world: String,
    ): Boolean {
        return accountSupportsCurrency(plugin, accountID, currency)
    }

    override fun getBalance(
        pluginName: String,
        accountID: UUID,
    ): BigDecimal {
        return runBlocking {
            getAccountByUuid(null, accountID).getBalance(primaryCurrency())
        }
    }

    override fun getBalance(
        pluginName: String,
        accountID: UUID,
        world: String,
    ): BigDecimal {
        return getBalance(pluginName, accountID)
    }

    override fun getBalance(
        pluginName: String,
        accountID: UUID,
        world: String,
        currency: String,
    ): BigDecimal {
        return runBlocking {
            getAccountByUuid(null, accountID).getBalance(storageHandler().getCurrency(currency)!!)
        }
    }

    override fun has(
        pluginName: String,
        accountID: UUID,
        amount: BigDecimal,
    ): Boolean {
        return runBlocking {
            getAccountByUuid(null, accountID).has(amount, primaryCurrency())
        }
    }

    override fun has(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        amount: BigDecimal,
    ): Boolean {
        return has(pluginName, accountID, amount)
    }

    override fun has(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        currency: String,
        amount: BigDecimal,
    ): Boolean {
        return runBlocking {
            getAccountByUuid(null, accountID).has(amount, storageHandler().getCurrency(currency)!!)
        }
    }

    override fun withdraw(
        pluginName: String,
        accountID: UUID,
        amount: BigDecimal,
    ): EconomyResponse {
        return runBlocking {
            getAccountByUuid(null, accountID).withdraw(
                amount,
                primaryCurrency(),
                vaultUnlockedCause,
                TransactionImportance.MEDIUM,
                null
            )

            EconomyResponse(
                amount,
                getBalance(pluginName, accountID),
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        }
    }

    override fun withdraw(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        amount: BigDecimal,
    ): EconomyResponse {
        return withdraw(pluginName, accountID, amount)
    }

    override fun withdraw(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        currency: String,
        amount: BigDecimal,
    ): EconomyResponse {
        return runBlocking {
            getAccountByUuid(null, accountID).withdraw(
                amount,
                storageHandler().getCurrency(currency)!!,
                vaultUnlockedCause,
                TransactionImportance.MEDIUM,
                null
            )

            EconomyResponse(
                amount,
                getBalance(pluginName, accountID),
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        }
    }

    override fun deposit(
        pluginName: String,
        accountID: UUID,
        amount: BigDecimal,
    ): EconomyResponse {
        return runBlocking {
            getAccountByUuid(null, accountID).deposit(
                amount,
                primaryCurrency(),
                vaultUnlockedCause,
                TransactionImportance.MEDIUM,
                null
            )

            EconomyResponse(
                amount,
                getBalance(pluginName, accountID),
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        }
    }

    override fun deposit(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        amount: BigDecimal,
    ): EconomyResponse {
        return deposit(pluginName, accountID, amount)
    }

    override fun deposit(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        currency: String,
        amount: BigDecimal,
    ): EconomyResponse {
        return runBlocking {
            getAccountByUuid(null, accountID).deposit(
                amount,
                storageHandler().getCurrency(currency)!!,
                vaultUnlockedCause,
                TransactionImportance.MEDIUM,
                null
            )

            EconomyResponse(
                amount,
                getBalance(pluginName, accountID),
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        }
    }

    override fun createSharedAccount(
        pluginName: String,
        accountID: UUID,
        name: String,
        owner: UUID,
    ): Boolean {
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(vuNskForSharedAccount(accountID), name)
                .setVaultBankOwner(owner)
            true
        }
    }

    override fun isAccountOwner(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
    ): Boolean {
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(vuNskForSharedAccount(accountID), accountID.toString())
                .isVaultBankOwner(accountID)
        }
    }

    override fun setOwner(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
    ): Boolean {
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(vuNskForSharedAccount(accountID), accountID.toString())
                .setVaultBankOwner(uuid)
            true
        }
    }

    override fun isAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
    ): Boolean {
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(vuNskForSharedAccount(accountID), accountID.toString())
                .isMember(uuid)
        }
    }

    override fun addAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
    ): Boolean {
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(vuNskForSharedAccount(accountID), accountID.toString())
                .addMember(uuid)
            true
        }
    }

    override fun addAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        vararg initialPermissions: VuAccountPermission,
    ): Boolean {
        return runBlocking {
            val acc = storageHandler()
                .getOrCreateNonPlayerAccount(vuNskForSharedAccount(accountID), accountID.toString())

            acc.addMember(uuid)
            acc.setPermissions(uuid, initialPermissions.associate { polyAccountPermFromVu(it) to true })
            true
        }
    }

    override fun removeAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
    ): Boolean {
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(vuNskForSharedAccount(accountID), accountID.toString())
                .removeMember(uuid)
            true
        }
    }

    override fun hasAccountPermission(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        permission: VuAccountPermission,
    ): Boolean {
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(vuNskForSharedAccount(accountID), accountID.toString())
                .hasPermissions(uuid, Collections.singletonList(polyAccountPermFromVu(permission)))
            true
        }
    }

    override fun updateAccountPermission(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        permission: VuAccountPermission,
        value: Boolean,
    ): Boolean {
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(vuNskForSharedAccount(accountID), accountID.toString())
                .setPermissions(uuid, mapOf(polyAccountPermFromVu(permission) to value))
            true
        }
    }

}