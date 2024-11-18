package io.github.arcaneplugins.polyconomy.api.util.cause

import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey

class NonPlayerCause(
    namespacedKey: NamespacedKey
) : Cause(
    data = namespacedKey
)