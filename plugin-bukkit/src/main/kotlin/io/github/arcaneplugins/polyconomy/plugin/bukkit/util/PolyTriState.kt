package io.github.arcaneplugins.polyconomy.plugin.bukkit.util

enum class PolyTriState(
    @Suppress("MemberVisibilityCanBePrivate")
    val asBoolean: Boolean?
) {
    TRUE(true),
    FALSE(false),
    UNSPECIFIED(null);

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