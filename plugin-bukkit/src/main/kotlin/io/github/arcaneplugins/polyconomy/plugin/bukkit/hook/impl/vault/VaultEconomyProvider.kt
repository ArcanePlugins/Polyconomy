package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault

import io.github.arcaneplugins.polyconomy.api.Economy.Companion.PRECISION
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault.VaultHook.Companion.VAULT_PLUGIN_NAME
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.StorageHandler
import kotlinx.coroutines.runBlocking
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.*

class VaultEconomyProvider(
    val plugin: Polyconomy,
) : Economy {

    /*
    JAVADOCS for Vault API
    https://github.com/MilkBowl/VaultAPI/blob/master/src/main/java/net/milkbowl/vault/economy/Economy.java
     */

    private fun storageHandler(): StorageHandler = plugin.storageManager.currentHandler!!

    private fun toVaultNsKey(str: String?) = NamespacedKey(VAULT_PLUGIN_NAME, str ?: "null")

    private suspend fun primaryCurrency(): Currency = storageHandler().getPrimaryCurrency()

    override fun isEnabled(): Boolean = plugin.isEnabled

    override fun getName(): String = plugin.description.name

    override fun hasBankSupport(): Boolean = true

    override fun fractionalDigits(): Int = PRECISION

    override fun format(p0: Double): String {
        return runBlocking {
            primaryCurrency().format(BigDecimal.valueOf(p0), Locale.getDefault())
        }
    }

    override fun currencyNamePlural(): String {
        return runBlocking {
            primaryCurrency().getDisplayName(true, Locale.getDefault())
        }
    }

    override fun currencyNameSingular(): String {
        return runBlocking {
            primaryCurrency().getDisplayName(false, Locale.getDefault())
        }
    }

    @Deprecated(message = "No per-world support.", replaceWith = ReplaceWith("hasAccount(OfflinePlayer)"))
    override fun hasAccount(p0: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasAccount(p0: OfflinePlayer): Boolean {
        TODO("Not yet implemented")
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        message = "Use offline players. No per-world support.",
        replaceWith = ReplaceWith("hasAccount(OfflinePlayer)")
    )
    override fun hasAccount(p0: String, p1: String): Boolean = hasAccount(p0)

    @Deprecated(message = "No per-world support.", replaceWith = ReplaceWith("hasAccount(OfflinePlayer)"))
    override fun hasAccount(p0: OfflinePlayer, p1: String): Boolean = hasAccount(p0)

    @Deprecated(message = "Use offline players.", replaceWith = ReplaceWith("getBalance(OfflinePlayer)"))
    override fun getBalance(p0: String): Double {
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    toVaultNsKey(p0),
                    p0
                )
                .getBalance(primaryCurrency())
                .toDouble()
        }
    }

    override fun getBalance(p0: OfflinePlayer): Double {
        return runBlocking {
            storageHandler()
                .getOrCreatePlayerAccount(
                    p0.uniqueId,
                    if (p0.isOnline) p0.name else null
                )
                .getBalance(primaryCurrency())
                .toDouble()
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        message = "Use offline players. No per-world support.",
        replaceWith = ReplaceWith("getBalance(OfflinePlayer)")
    )
    override fun getBalance(p0: String, p1: String): Double = getBalance(p0)

    @Deprecated(message = "No per-world support.", replaceWith = ReplaceWith("getBalance(OfflinePlayer)"))
    override fun getBalance(p0: OfflinePlayer, p1: String): Double = getBalance(p0)

    @Deprecated(message = "Use offline players.", replaceWith = ReplaceWith("has(OfflinePlayer, Double)"))
    override fun has(p0: String, p1: Double): Boolean {
        TODO("Not yet implemented")
    }

    override fun has(p0: OfflinePlayer, p1: Double): Boolean {
        TODO("Not yet implemented")
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        message = "Use offline players. No per-world support.",
        replaceWith = ReplaceWith("has(OfflinePlayer, Double)")
    )
    override fun has(p0: String, p1: String, p2: Double): Boolean = has(p0, p2)

    @Deprecated(message = "No per-world support.", replaceWith = ReplaceWith("has(OfflinePlayer, Double)"))
    override fun has(p0: OfflinePlayer, p1: String, p2: Double): Boolean = has(p0, p2)

    @Deprecated(message = "Use offline players.", replaceWith = ReplaceWith("withdrawPlayer(OfflinePlayer, Double)"))
    override fun withdrawPlayer(p0: String, p1: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun withdrawPlayer(p0: OfflinePlayer, p1: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        message = "Use offline players. No per-world support.",
        replaceWith = ReplaceWith("withdrawPlayer(OfflinePlayer, Double)")
    )
    override fun withdrawPlayer(p0: String, p1: String, p2: Double): EconomyResponse = withdrawPlayer(p0, p2)

    @Deprecated(message = "No per-world support.", replaceWith = ReplaceWith("withdrawPlayer(OfflinePlayer, Double)"))
    override fun withdrawPlayer(p0: OfflinePlayer, p1: String, p2: Double): EconomyResponse = withdrawPlayer(p0, p2)

    @Deprecated(message = "Use offline players.", replaceWith = ReplaceWith("depositPlayer(OfflinePlayer, Double)"))
    override fun depositPlayer(p0: String, p1: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun depositPlayer(p0: OfflinePlayer, p1: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        message = "Use offline players. No per-world support.",
        replaceWith = ReplaceWith("depositPlayer(OfflinePlayer, Double)")
    )
    override fun depositPlayer(p0: String, p1: String, p2: Double): EconomyResponse = depositPlayer(p0, p2)

    @Deprecated(message = "No per-world support.", replaceWith = ReplaceWith("depositPlayer(OfflinePlayer, Double)"))
    override fun depositPlayer(p0: OfflinePlayer, p1: String, p2: Double): EconomyResponse = depositPlayer(p0, p2)

    @Deprecated(message = "Use offline players", replaceWith = ReplaceWith("createBank(String, OfflinePlayer)"))
    override fun createBank(p0: String, p1: String): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun createBank(p0: String, p1: OfflinePlayer): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun deleteBank(p0: String): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankBalance(p0: String): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankHas(p0: String, p1: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankWithdraw(p0: String, p1: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun bankDeposit(p0: String, p1: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    @Deprecated(message = "Use offline players", replaceWith = ReplaceWith("isBankOwner(String, OfflinePlayer)"))
    override fun isBankOwner(p0: String, p1: String): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun isBankOwner(p0: String, p1: OfflinePlayer): EconomyResponse {
        TODO("Not yet implemented")
    }

    @Deprecated(message = "Use offline players", replaceWith = ReplaceWith("isBankMember(String, OfflinePlayer)"))
    override fun isBankMember(p0: String, p1: String): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun isBankMember(p0: String, p1: OfflinePlayer): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun getBanks(): MutableList<String> {
        TODO("Not yet implemented")
    }

    @Deprecated(message = "Use offline players", replaceWith = ReplaceWith("createPlayerAccount(OfflinePlayer)"))
    override fun createPlayerAccount(p0: String): Boolean {
        runBlocking {
            storageHandler().getOrCreateNonPlayerAccount(
                namespacedKey = toVaultNsKey(p0),
                name = p0,
            )
        }
        return true
    }

    override fun createPlayerAccount(p0: OfflinePlayer): Boolean {
        runBlocking {
            storageHandler().getOrCreatePlayerAccount(
                uuid = p0.uniqueId,
                name = p0.name,
            )
        }
        return true
    }

    @Suppress("DEPRECATION")
    @Deprecated(message = "Use offline players", replaceWith = ReplaceWith("createPlayerAccount(OfflinePlayer)"))
    override fun createPlayerAccount(p0: String, p1: String): Boolean = createPlayerAccount(p0)

    @Deprecated(
        message = "Polyconomy does not support per-world accounts",
        replaceWith = ReplaceWith("createPlayerAccount(OfflinePlayer)")
    )
    override fun createPlayerAccount(p0: OfflinePlayer, p1: String): Boolean = createPlayerAccount(p0)
}