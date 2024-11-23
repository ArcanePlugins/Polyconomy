package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault

import io.github.arcaneplugins.polyconomy.api.Economy.Companion.PRECISION
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import io.github.arcaneplugins.polyconomy.api.util.cause.PluginCause
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

    private val vaultCause = PluginCause(NamespacedKey("polyconomy", "vault"))

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
        return runBlocking {
            storageHandler().hasNonPlayerAccount(toVaultNsKey(p0))
        }
    }

    override fun hasAccount(p0: OfflinePlayer): Boolean {
        return runBlocking {
            storageHandler().hasPlayerAccount(p0.uniqueId)
        }
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
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    toVaultNsKey(p0),
                    p0
                )
                .has(BigDecimal.valueOf(p1), primaryCurrency())
        }
    }

    override fun has(p0: OfflinePlayer, p1: Double): Boolean {
        return runBlocking {
            storageHandler()
                .getOrCreatePlayerAccount(
                    p0.uniqueId,
                    name = null
                )
                .has(BigDecimal.valueOf(p1), primaryCurrency())
        }
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
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    toVaultNsKey(p0),
                    p0
                )
                .withdraw(
                    amount = BigDecimal.valueOf(p1),
                    currency = primaryCurrency(),
                    cause = vaultCause,
                    importance = TransactionImportance.MEDIUM,
                    reason = null,
                )

            @Suppress("DEPRECATION")
            EconomyResponse(
                p1,
                getBalance(p0),
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        }
    }

    override fun withdrawPlayer(p0: OfflinePlayer, p1: Double): EconomyResponse {
        return runBlocking {
            storageHandler()
                .getOrCreatePlayerAccount(
                    p0.uniqueId,
                    p0.name
                )
                .withdraw(
                    amount = BigDecimal.valueOf(p1),
                    currency = primaryCurrency(),
                    cause = vaultCause,
                    importance = TransactionImportance.MEDIUM,
                    reason = null,
                )

            EconomyResponse(
                p1,
                getBalance(p0),
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        }
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
        return runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    toVaultNsKey(p0),
                    p0
                )
                .deposit(
                    amount = BigDecimal.valueOf(p1),
                    currency = primaryCurrency(),
                    cause = vaultCause,
                    importance = TransactionImportance.MEDIUM,
                    reason = null,
                )

            @Suppress("DEPRECATION")
            EconomyResponse(
                p1,
                getBalance(p0),
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        }
    }

    override fun depositPlayer(p0: OfflinePlayer, p1: Double): EconomyResponse {
        return runBlocking {
            storageHandler()
                .getOrCreatePlayerAccount(
                    p0.uniqueId,
                    p0.name
                )
                .deposit(
                    amount = BigDecimal.valueOf(p1),
                    currency = primaryCurrency(),
                    cause = vaultCause,
                    importance = TransactionImportance.MEDIUM,
                    reason = null,
                )

            EconomyResponse(
                p1,
                getBalance(p0),
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        }
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
        runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    namespacedKey = toVaultNsKey(p0),
                    name = p0,
                )

            storageHandler()
                .getOrCreateNonPlayerAccount(
                    namespacedKey = toVaultNsKey(p0),
                    name = p0,
                )
                .setLegacyVaultBankOwner(
                    ownerId = toVaultNsKey(p1)
                )
        }

        return EconomyResponse(
            0.0,
            bankBalance(p0).balance,
            EconomyResponse.ResponseType.SUCCESS,
            null,
        )
    }

    override fun createBank(p0: String, p1: OfflinePlayer): EconomyResponse {
        runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    namespacedKey = toVaultNsKey(p0),
                    name = p0,
                )

            storageHandler()
                .getOrCreateNonPlayerAccount(
                    namespacedKey = toVaultNsKey(p0),
                    name = p0,
                )
                .setVaultBankOwner(
                    ownerId = p1.uniqueId
                )
        }

        return EconomyResponse(
            0.0,
            bankBalance(p0).balance,
            EconomyResponse.ResponseType.SUCCESS,
            null,
        )
    }

    override fun deleteBank(p0: String): EconomyResponse {
        runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    namespacedKey = toVaultNsKey(p0),
                    name = p0,
                )
                .deleteAccount()
        }

        return EconomyResponse(
            0.0,
            0.0,
            EconomyResponse.ResponseType.SUCCESS,
            null,
        )
    }

    override fun bankBalance(p0: String): EconomyResponse {
        val bal = runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    namespacedKey = toVaultNsKey(p0),
                    name = p0,
                )
                .getBalance(primaryCurrency())
        }

        return EconomyResponse(
            0.0,
            bal.toDouble(),
            EconomyResponse.ResponseType.SUCCESS,
            null,
        )
    }

    override fun bankHas(p0: String, p1: Double): EconomyResponse {
        val has = runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    namespacedKey = toVaultNsKey(p0),
                    name = p0,
                )
                .has(BigDecimal.valueOf(p1), primaryCurrency())
        }

        return EconomyResponse(
            p1,
            bankBalance(p0).balance,
            if (has) EconomyResponse.ResponseType.SUCCESS else EconomyResponse.ResponseType.FAILURE,
            "FAILURE if bank can't afford"
        )
    }

    override fun bankWithdraw(p0: String, p1: Double): EconomyResponse {
        runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    namespacedKey = toVaultNsKey(p0),
                    name = p0,
                )
                .withdraw(
                    amount = BigDecimal.valueOf(p1),
                    currency = primaryCurrency(),
                    cause = vaultCause,
                    importance = TransactionImportance.MEDIUM,
                    reason = null,
                )
        }

        return EconomyResponse(
            p1,
            bankBalance(p0).balance,
            EconomyResponse.ResponseType.SUCCESS,
            null
        )
    }

    override fun bankDeposit(p0: String, p1: Double): EconomyResponse {
        runBlocking {
            storageHandler()
                .getOrCreateNonPlayerAccount(
                    namespacedKey = toVaultNsKey(p0),
                    name = p0,
                )
                .deposit(
                    amount = BigDecimal.valueOf(p1),
                    currency = primaryCurrency(),
                    cause = vaultCause,
                    importance = TransactionImportance.MEDIUM,
                    reason = null,
                )
        }

        return EconomyResponse(
            p1,
            bankBalance(p0).balance,
            EconomyResponse.ResponseType.SUCCESS,
            null
        )
    }

    @Deprecated(message = "Use offline players", replaceWith = ReplaceWith("isBankOwner(String, OfflinePlayer)"))
    override fun isBankOwner(p0: String, p1: String): EconomyResponse {
        return runBlocking {
            val account = storageHandler().getOrCreateNonPlayerAccount(toVaultNsKey(p0), p0)

            @Suppress("DEPRECATION") val status = if (account.isLegacyVaultBankOwner(memberId = toVaultNsKey(p1))) {
                EconomyResponse.ResponseType.SUCCESS
            } else {
                EconomyResponse.ResponseType.FAILURE
            }

            EconomyResponse(
                0.0,
                bankBalance(p0).balance,
                status,
                "FAILURE if not a owner"
            )
        }
    }

    override fun isBankOwner(p0: String, p1: OfflinePlayer): EconomyResponse {
        return runBlocking {
            val account = storageHandler().getOrCreateNonPlayerAccount(toVaultNsKey(p0), p0)
            val status = if (account.isVaultBankOwner(memberId = p1.uniqueId)) {
                EconomyResponse.ResponseType.SUCCESS
            } else {
                EconomyResponse.ResponseType.FAILURE
            }

            EconomyResponse(
                0.0,
                bankBalance(p0).balance,
                status,
                "FAILURE if not a owner"
            )
        }
    }

    @Deprecated(message = "Use offline players", replaceWith = ReplaceWith("isBankMember(String, OfflinePlayer)"))
    override fun isBankMember(p0: String, p1: String): EconomyResponse {
        return runBlocking {
            val account = storageHandler().getOrCreateNonPlayerAccount(toVaultNsKey(p0), p0)
            @Suppress("DEPRECATION") val status = if (account.isLegacyVaultBankMember(toVaultNsKey(p1))) {
                EconomyResponse.ResponseType.SUCCESS
            } else {
                EconomyResponse.ResponseType.FAILURE
            }

            EconomyResponse(
                0.0,
                bankBalance(p0).balance,
                status,
                "FAILURE if not a owner"
            )
        }
    }

    override fun isBankMember(p0: String, p1: OfflinePlayer): EconomyResponse {
        return runBlocking {
            val account = storageHandler().getOrCreateNonPlayerAccount(toVaultNsKey(p0), p0)
            val status = if (account.isMember(p1.uniqueId)) {
                EconomyResponse.ResponseType.SUCCESS
            } else {
                EconomyResponse.ResponseType.FAILURE
            }

            EconomyResponse(
                0.0,
                bankBalance(p0).balance,
                status,
                "FAILURE if not a owner"
            )
        }
    }

    override fun getBanks(): List<String> {
        return runBlocking {
            storageHandler()
                .getVaultBankAccountIds()
                .map { it.key }
        }
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