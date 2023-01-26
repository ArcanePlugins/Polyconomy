package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook

import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryHook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.vault.VaultHook

object HookManager {

    val hooks: Set<Hook> = mutableSetOf(
        TreasuryHook,
        VaultHook,
    )

    fun registerAll() {
        hooks.filter(Hook::canRegister).forEach(Hook::register)
    }

    fun unregisterAll() {
        hooks.filter(Hook::registered).forEach(Hook::unregister)
    }
}