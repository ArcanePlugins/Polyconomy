package io.github.arcaneplugins.polyconomy.api.util.cause

import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import java.util.*

abstract class Cause(
    val type: CauseType,
    val data: Any,
) {

    companion object {
        fun serialize(type: CauseType, data: String): Cause {
            return when (type) {
                CauseType.PLAYER -> PlayerCause(UUID.fromString(data))
                CauseType.NON_PLAYER -> NonPlayerCause(NamespacedKey.fromString(data))
                CauseType.PLUGIN -> PluginCause(NamespacedKey.fromString(data))
                CauseType.SERVER -> ServerCause
            }
        }
    }

}
