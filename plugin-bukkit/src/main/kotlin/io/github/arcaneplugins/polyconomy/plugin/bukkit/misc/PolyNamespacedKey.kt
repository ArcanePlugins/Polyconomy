package io.github.arcaneplugins.polyconomy.plugin.bukkit.misc

import me.lokka30.treasury.api.common.NamespacedKey

data class PolyNamespacedKey(
    val namespace: String,
    val key: String
) {

    companion object {
        fun fromTreasury(treasuryType: NamespacedKey): PolyNamespacedKey {
            return PolyNamespacedKey(
                treasuryType.namespace,
                treasuryType.key
            )
        }

        @Suppress("unused") //TODO use
        fun toTreasury(polyType: PolyNamespacedKey): NamespacedKey {
            return NamespacedKey.of(
                polyType.namespace,
                polyType.key
            )
        }
    }

    override fun toString(): String {
        return "${namespace}:${key}"
    }
}
