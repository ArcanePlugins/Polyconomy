package io.github.arcaneplugins.polyconomy.api.account

import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey

abstract class NonPlayerAccount(
    val namespacedKey: NamespacedKey
) : Account
