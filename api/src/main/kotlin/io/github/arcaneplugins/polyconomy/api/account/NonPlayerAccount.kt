package io.github.arcaneplugins.polyconomy.api.account

import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
import java.util.*

abstract class NonPlayerAccount(
    val namespacedKey: NamespacedKey,
) : Account {

    abstract suspend fun isVaultBankAccount(): Boolean

    @Deprecated(
        message = "Legacy support for Vault",
        replaceWith = ReplaceWith("isVaultBankOwner(UUID)")
    )
    abstract suspend fun isLegacyVaultBankOwner(
        memberId: NamespacedKey,
    ): Boolean

    abstract suspend fun isVaultBankOwner(
        memberId: UUID,
    ): Boolean

    @Deprecated(
        message = "Legacy support for Vault",
        replaceWith = ReplaceWith("isVaultBankOwner(UUID)")
    )
    abstract suspend fun isLegacyVaultBankMember(
        memberId: NamespacedKey,
    ): Boolean

    abstract suspend fun setLegacyVaultBankOwner(
        ownerId: NamespacedKey,
    )

    abstract suspend fun setVaultBankOwner(
        ownerId: UUID,
    )

}
