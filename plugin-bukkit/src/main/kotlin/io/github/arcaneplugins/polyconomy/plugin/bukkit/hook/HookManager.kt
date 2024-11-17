package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.HOOK_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryHook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault.VaultHook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable.ThrowableUtil
import org.bukkit.Bukkit

class HookManager(
    val plugin: Polyconomy,
) {

    val hooks: Set<Hook> = mutableSetOf(
        TreasuryHook(plugin),
        VaultHook(plugin),
    )

    fun registerAll() {
        plugin.debugLog(HOOK_MANAGER) { "Registering hooks." }

        hooks
            .filter(Hook::canRegister)
            .forEach { hook ->
                plugin.debugLog(HOOK_MANAGER) { "Registering hook '${hook.id}'." }
                hook.register()
                plugin.debugLog(HOOK_MANAGER) { "Registered hook '${hook.id}'." }
            }

        plugin.debugLog(HOOK_MANAGER) {
            "Registered ${hooks.count { it.registered }} of ${hooks.size} hooks."
        }
    }

    fun unregisterAll() {
        plugin.debugLog(HOOK_MANAGER) {
            "Unregistering ${hooks.count { it.registered }} hooks."
        }

        hooks
            .filter(Hook::registered)
            .forEach { hook ->
                plugin.debugLog(HOOK_MANAGER) { "Unregistering hook '${hook.id}'." }
                hook.unregister()
                plugin.debugLog(HOOK_MANAGER) { "Unregistered hook '${hook.id}'." }
            }

        plugin.debugLog(HOOK_MANAGER) { "Unregistered hooks." }
    }

    fun ensureHardDependencies() {
        plugin.debugLog(HOOK_MANAGER) { "Enforcing hard dependencies..." }

        val missing = listOf("Treasury")
            .filter { !Bukkit.getPluginManager().isPluginEnabled(it) }

        if (missing.isEmpty()) {
            plugin.debugLog(HOOK_MANAGER) { "All hard dependencies are present." }
            return
        }

        throw ThrowableUtil.explainHelpfully(
            plugin,
            IllegalStateException(),
            otherInfo = """
            
            You have not followed Polyconomy's installation instructions!
            You are missing the following plugin dependencies:
            
            ${missing.joinToString(separator = "\n", prefix = " ✘ ")}
            
            Polyconomy will not be able to load until you resolve this issue.
            Install the missing plugin(s) and then restart your server.
            """.trimIndent(),
            otherContext = "Whilst ensuring hard dependencies are met."
        )
    }

}