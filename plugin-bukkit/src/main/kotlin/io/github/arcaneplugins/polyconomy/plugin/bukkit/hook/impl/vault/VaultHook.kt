@file:Suppress("DEPRECATION")

package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.Hook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookType
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault.legacy.VaultLegacyEconomyProvider
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault.unlocked.VaultUnlockedEconomyProvider
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.ClassUtil
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import net.milkbowl.vault.economy.Economy as EconomyLegacy
import net.milkbowl.vault2.economy.Economy as EconomyUnlocked

class VaultHook(
    val plugin: Polyconomy,
) : Hook(
    id = VAULT_PLUGIN_NAME,
    type = HookType.ECONOMY_API,
) {

    companion object {
        const val VAULT_PLUGIN_NAME = "Vault"
        const val VAULT_UNLOCKED_CLASSPATH = "net.milkbowl.vault2.economy.Economy"

        fun isVaultUnlocked(): Boolean {
            return ClassUtil.isValidClasspath(VAULT_UNLOCKED_CLASSPATH)
        }
    }

    override fun canRegister(): Boolean {
        return !registered && Bukkit.getPluginManager().isPluginEnabled(VAULT_PLUGIN_NAME)
    }

    override fun register() {
        @Suppress("DEPRECATION")
        plugin.server.servicesManager.register(
            EconomyLegacy::class.java,
            VaultLegacyEconomyProvider(plugin),
            plugin,
            ServicePriority.Highest
        )

        if (isVaultUnlocked()) {
            plugin.server.servicesManager.register(
                EconomyUnlocked::class.java,
                VaultUnlockedEconomyProvider(plugin),
                plugin,
                ServicePriority.Highest
            )
        }
    }

    override fun unregister() {
        @Suppress("DEPRECATION")
        plugin.server.servicesManager.unregister(EconomyLegacy::class.java)

        if (isVaultUnlocked()) {
            plugin.server.servicesManager.unregister(EconomyUnlocked::class.java)
        }
    }

}