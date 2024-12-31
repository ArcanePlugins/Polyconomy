@file:Suppress("DEPRECATION")

package io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.test

import io.github.arcaneplugins.ecoworks.plugin.bukkit.Ecoworks
import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.TestResult
import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.TestResult.Companion.Status.FAILURE
import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.TestResult.Companion.Status.OBSERVATION
import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.TestResult.Companion.Status.SUCCESS
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import java.util.function.Consumer
import java.util.function.Supplier

class VaultLegacyTester(val plugin: Ecoworks) : Tester {

    lateinit var economy: Economy

    override fun checkAndInit(): Boolean {
        // This test works with Vault (Legacy) and VaultUnlocked,
        // it will use the legacy API of course on both.
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            plugin.logger.severe("Vault plugin is not installed and enabled!")
            return false
        }

        if (Bukkit.getOnlinePlayers().size < 2) {
            plugin.logger.severe("This test requires at least 2 online players!")
            return false
        }

        val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java)

        if (rsp == null) {
            plugin.logger.severe("No economy plugin has registered with Vault (Legacy API)!")
            return false
        }

        economy = rsp.provider
        plugin.logger.info("Hooked with ${rsp.plugin.name} via Vault (Legacy API)")
        return true
    }

    override fun runTests(resultConsumer: Consumer<TestResult>) {
        tests.forEach { resultConsumer.accept(it.get()) }
    }

    val tests: List<Supplier<TestResult>> = listOf(
        Supplier {
            val isEnabled = economy.isEnabled

            return@Supplier if (isEnabled) {
                TestResult(SUCCESS, "isEnabled", "Value")
            } else {
                TestResult(FAILURE, "isEnabled", "Value", "Provider has disabled economy API integration")
            }
        },
        Supplier {
            val hasBankSupport = economy.hasBankSupport()

            return@Supplier if (hasBankSupport) {
                TestResult(SUCCESS, "hasBankSupport", "Value")
            } else {
                TestResult(OBSERVATION, "hasBankSupport", "Value", "Provider has disabled bank API support")
            }
        },
        Supplier {
            val digits = economy.fractionalDigits()
            if (digits > 0) {
                TestResult(SUCCESS, "fractionalDigits", "Value", digits.toString())
            } else {
                TestResult(
                    FAILURE,
                    "fractionalDigits",
                    "Value",
                    "Fractional digits must be greater than zero, got ${digits}"
                )
            }
        },
        Supplier {
            val currencyNamePlural = economy.currencyNamePlural()
            TestResult(SUCCESS, "currencyNamePlural", "General", currencyNamePlural)
        },
        Supplier {
            val currencyNamePlural = economy.currencyNameSingular()
            TestResult(SUCCESS, "currencyNameSingular", "General", currencyNamePlural)
        },
        Supplier {
            val username = "Notch"
            val hasLegacyImpossibleAccount = economy.hasAccount(username)
            val desc = "Whether ${username} has an account (impossible?)"
            return@Supplier if (hasLegacyImpossibleAccount) {
                TestResult(FAILURE, "hasAccount.String.ImpossibleAccounts", desc, "How did this happen?")
            } else {
                TestResult(SUCCESS, "hasAccount.String.ImpossibleAccounts", desc)
            }
        },
        Supplier {
            val username = "Notch"
            val offp = Bukkit.getOfflinePlayer(username)
            val hasLegacyImpossibleAccount = economy.hasAccount(offp)
            val desc = "Whether ${username} has an account (impossible?)"
            return@Supplier if (hasLegacyImpossibleAccount) {
                TestResult(FAILURE, "hasAccount.Offp.ImpossibleAccounts", desc, "How did this happen?")
            } else {
                TestResult(SUCCESS, "hasAccount.Offp.ImpossibleAccounts", desc)
            }
        },
    )

    /* TODO:
        createPlayerAccount.string for first online player (if any online)
        createPlayerAccount.offlineplayer for second online player (if any online)
        hasAccount.string for first online player (if any online)
        hasAccount.offlineplayer for second online player (if any online)
        getBalance.string ...
        getBalance.offp ...
        has.string ...
        has.offp ...
        withdrawPlayer.string ...
        withdrawPlayer.offp ...
        depositBalance.string ...
        depositBalance.offp ...
        createBank.string ...
        createBank.offp ...
        deleteBank ...
        bankBalance ...
        bankHas ...
        bankWithdraw ...
        bankDeposit ...
        isBankOwner.string ...
        isBankOwner.offlineplayer ...
        isBankMember.string ...
        isBankMember.offlineplayer ...
        getBanks ...

     */


}