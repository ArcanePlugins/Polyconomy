package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.permission

import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyTriState

/*
 * This enum must have full parity with Treasury's AccountPermission enum.
 */
enum class PolyAccountPermission {

    BALANCE,

    WITHDRAW,

    DEPOSIT,

    MODIFY_PERMISSIONS;

    companion object {
        val allPermissions: Map<PolyAccountPermission, PolyTriState> = let {
            val map: MutableMap<PolyAccountPermission, PolyTriState> = mutableMapOf()
            PolyAccountPermission.values().forEach { map[it] = PolyTriState.TRUE }
            return@let map
        }
    }

}