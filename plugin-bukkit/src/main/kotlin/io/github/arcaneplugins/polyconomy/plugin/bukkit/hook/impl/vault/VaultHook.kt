package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault

import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.Hook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookType
import org.bukkit.Bukkit

object VaultHook : Hook(
    id = "Vault",
    type = HookType.ECONOMY_API
) {

    override fun canRegister(): Boolean {
        return !registered && Bukkit.getPluginManager().isPluginEnabled("Vault")
    }

    override fun register() {
        TODO("Not yet implemented")
    }

    override fun unregister() {
        TODO("Not yet implemented")
    }

    object PolyVaultEconomyProvider {
        //TODO
    }
}