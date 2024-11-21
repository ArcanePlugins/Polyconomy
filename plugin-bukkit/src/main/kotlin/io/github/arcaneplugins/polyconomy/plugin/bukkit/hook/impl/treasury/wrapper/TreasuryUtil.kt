package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper

import me.lokka30.treasury.api.common.NamespacedKey

object TreasuryUtil {

    fun treasuryNskToPoly(
        treasuryNsk: NamespacedKey
    ): io.github.arcaneplugins.polyconomy.api.util.NamespacedKey {
        return io.github.arcaneplugins.polyconomy.api.util.NamespacedKey(
            namespace = treasuryNsk.namespace,
            key = treasuryNsk.key,
        )
    }

    fun polyNskToTreasury(
        polyNsk: io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
    ): NamespacedKey {
        return NamespacedKey.of(
            polyNsk.namespace,
            polyNsk.key,
        )
    }

}