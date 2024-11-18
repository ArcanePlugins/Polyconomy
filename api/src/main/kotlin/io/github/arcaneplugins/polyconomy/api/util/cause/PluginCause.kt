package io.github.arcaneplugins.polyconomy.api.util.cause

import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey

class PluginCause(
    namespacedKey: NamespacedKey,
) : Cause(
    data = namespacedKey
)