package io.github.arcaneplugins.polyconomy.plugin.bukkit.util

import me.lokka30.treasury.api.common.misc.TriState

enum class PolyTriState(
    @Suppress("MemberVisibilityCanBePrivate")
    val asBoolean: Boolean?,
    val toTreasury: TriState
) {
    TRUE(
        asBoolean = true,
        toTreasury = TriState.TRUE
    ),

    FALSE(
        asBoolean = false,
        toTreasury = TriState.FALSE
    ),

    UNSPECIFIED(
        asBoolean = null,
        toTreasury = TriState.UNSPECIFIED
    );


    fun asFalsyBoolean(): Boolean {
        return asBoolean ?: false
    }

    companion object {
        fun fromBool(bool: Boolean?): PolyTriState {
            return if(bool == null) {
                UNSPECIFIED
            } else {
                if(bool) TRUE else FALSE
            }
        }
    }
}