package io.github.arcaneplugins.polyconomy.api.util.cause

import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey

class NonPlayerCause(
    val namespacedKey: NamespacedKey,
) : Cause(
    type = CauseType.NON_PLAYER,
    data = namespacedKey,
)