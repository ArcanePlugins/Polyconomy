package io.github.arcaneplugins.polyconomy.api.account

import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey

abstract class NonPlayerAccount(
    name: String,
    val namespacedKey: NamespacedKey
) : Account(name)
