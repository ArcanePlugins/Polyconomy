package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.HOOK_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryHook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault.VaultHook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import org.bukkit.Bukkit

object HookManager {

    val hooks: Set<Hook> = mutableSetOf(
        TreasuryHook,
        VaultHook,
    )

    fun registerAll() {
        Log.d(HOOK_MANAGER) { "Registering hooks." }

        hooks
            .filter(Hook::canRegister)
            .forEach { hook ->
                Log.d(HOOK_MANAGER) { "Registering hook '${hook.id}'." }
                hook.register()
                Log.d(HOOK_MANAGER) { "Registered hook '${hook.id}'." }
            }

        Log.d(HOOK_MANAGER) {
            "Registered ${hooks.count { it.registered }} of ${hooks.size} hooks."
        }

        warnIfMissingEconomyApi()
    }

    fun unregisterAll() {
        Log.d(HOOK_MANAGER) {
            "Unregistering ${hooks.count { it.registered }} hooks."
        }

        hooks
            .filter(Hook::registered)
            .forEach { hook ->
                Log.d(HOOK_MANAGER) { "Unregistering hook '${hook.id}'." }
                hook.unregister()
                Log.d(HOOK_MANAGER) { "Unregistered hook '${hook.id}'." }
            }

        Log.d(HOOK_MANAGER) { "Unregistered hooks." }
    }

    // TODO migrate to a compatibility checking system
    private fun warnIfMissingEconomyApi() {
        Log.d(HOOK_MANAGER) { "Checking which compatible economy APIs are present." }

        if(!SettingsCfg
            .rootNode
            .node("advanced", "important-plugin-recommendations")
            .getBoolean(true)
        ) return

        val hasTreasury: Boolean = Bukkit.getPluginManager().isPluginEnabled("Treasury")
        val hasVault: Boolean = Bukkit.getPluginManager().isPluginEnabled("Vault")

        if(!hasTreasury && !hasVault) {
            Log.w(
                """
                
                You don't have Treasury and/or Vault installed on your server.
                
                Treasury, like Vault, provides its own Economy API which economy plugins use to communicate with each other.
                
                Although Treasury is more modern, most plugins still use a legacy Vault hook, so we recommend installing both.
                
                How to resolve this issue:
                    • Install Treasury:   https://www.spigotmc.org/resources/99531/
                    • Install Vault:      https://www.spigotmc.org/resources/34315/
                    
                To remove this warning, resolve the issue - or disable the advanced setting, `important-plugin-recommendations`.
                """.trimIndent()
            )
        } else if(!hasTreasury) {
            Log.w(
                """
                
                You don't have Treasury installed on your server.
                
                Treasury, like Vault, provides its own Economy API which economy plugins use to communicate with each other.
                
                By not having Treasury installed alongside Vault, you miss out on these features:
                    ✘ Ability for plugins to use their modern Treasury hooks
                    ✘ Concurrency support (which improves performance and TPS)
                    ✘ Ability for plugins to use more than a single currency
                    ✘ Ability for plugins to directly access transaction history
                    ✘ Ability for plugins to manage transaction events
                    ✘ Ability for plugins to manage account permissions
                 
                How to resolve this issue:
                    • Install Treasury:   https://www.spigotmc.org/resources/99531/
                    • We recommend keeping Vault installed as well for legacy compatibility.
                
                To remove this warning, resolve the issue - or disable the advanced setting, `important-plugin-recommendations`. 
                """.trimIndent()
            )
        }
    }
}