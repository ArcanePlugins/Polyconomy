package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.Hook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookType
import org.bukkit.Bukkit

class VaultHook(
    val plugin: Polyconomy,
) : Hook(
    id = VAULT_PLUGIN_NAME,
    type = HookType.ECONOMY_API,
) {

    companion object {
        const val VAULT_PLUGIN_NAME = "Vault"
    }

    override fun canRegister(): Boolean {
        return !registered && Bukkit.getPluginManager().isPluginEnabled(VAULT_PLUGIN_NAME)
    }

    override fun register() {
        TODO("Not yet implemented")
    }

    override fun unregister() {
        TODO("Not yet implemented")
    }

}