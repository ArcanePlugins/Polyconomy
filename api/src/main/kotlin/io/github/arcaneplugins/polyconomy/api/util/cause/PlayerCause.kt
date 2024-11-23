package io.github.arcaneplugins.polyconomy.api.util.cause

import java.util.*

class PlayerCause(
    val uuid: UUID,
) : Cause(
    type = CauseType.PLAYER,
    data = uuid,
)