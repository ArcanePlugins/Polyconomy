package io.github.arcaneplugins.polyconomy.api.util.cause

import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey

class NonPlayerCause(
    val namespacedKey: NamespacedKey,
) : Cause(
    data = namespacedKey
)