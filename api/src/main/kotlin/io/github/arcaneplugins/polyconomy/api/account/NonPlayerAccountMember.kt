package io.github.arcaneplugins.polyconomy.api.account

import java.util.*

class NonPlayerAccountMember(
    val memberId: UUID,
    val permMap: Map<AccountPermission, Boolean?>
)