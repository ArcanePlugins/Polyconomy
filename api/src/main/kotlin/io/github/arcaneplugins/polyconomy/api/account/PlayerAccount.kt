package io.github.arcaneplugins.polyconomy.api.account

import java.util.*

abstract class PlayerAccount(
    name: String,
    val playerUuid: UUID,
) : Account(name)
