package io.github.arcaneplugins.polyconomy.plugin.bukkit.util

import me.lokka30.treasury.api.common.misc.TriState

enum class PolyTriState(
    val asBoolean: Boolean?
) {
    TRUE(
        asBoolean = true
    ),

    FALSE(
        asBoolean = false
    ),

    UNSPECIFIED(
        asBoolean = null
    );


    @Suppress("unused")
    fun asFalsyBoolean(): Boolean {
        return asBoolean ?: false
    }

    fun toTreasury(): TriState {
        return TriState.valueOf(name)
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