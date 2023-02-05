package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.permission.PolyAccountPermission
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.PolyTransaction
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyNamespacedKey
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyTriState
import me.lokka30.treasury.api.economy.account.Account
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.PlayerAccount
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class PolyAccount {

    companion object {
        fun fromTreasury(
            treasuryAccount: Account
        ): PolyAccount {
            return when (treasuryAccount) {
                is PlayerAccount -> {
                    PolyPlayerAccount(
                        player = treasuryAccount.uniqueId
                    )
                }

                is NonPlayerAccount -> {
                    PolyNonPlayerAccount(
                        id = PolyNamespacedKey.fromTreasury(treasuryAccount.identifier)
                    )
                }

                else -> {
                    throw IllegalStateException(
                        "Expected PlayerAccount or NonPlayerAccount, got " +
                                "'${treasuryAccount::class.simpleName}'"
                    )
                }
            }
        }
    }

    abstract fun retrieveNameAsync(): CompletableFuture<PolyResponse<String?>>

    abstract fun retrieveNameSync(): PolyResponse<String?>

    abstract fun renameAsync(
        name: String?
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun renameSync(
        name: String?
    ): PolyResponse<PolyTriState>

    abstract fun retrieveBalance(
        currency: PolyCurrency
    ): CompletableFuture<PolyResponse<BigDecimal>>

    abstract fun doTransaction(
        transaction: PolyTransaction
    ): CompletableFuture<PolyResponse<BigDecimal>>

    abstract fun delete(): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun retrieveHeldCurrencies(): CompletableFuture<PolyResponse<Collection<String>>>

    abstract fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): CompletableFuture<PolyResponse<Collection<PolyTransaction>>>

    abstract fun retrieveMemberIds(): CompletableFuture<PolyResponse<Collection<UUID>>>

    abstract fun isMember(
        player: UUID
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun setPermissions(
        player: UUID,
        permissionValue: PolyTriState,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>>

    abstract fun retrievePermissions(
        player: UUID
    ): CompletableFuture<PolyResponse<Map<PolyAccountPermission, PolyTriState>>>

    abstract fun retrievePermissionsMap(): CompletableFuture<PolyResponse<Map<UUID, Map<PolyAccountPermission, PolyTriState>>>>

    abstract fun hasPermissions(
        player: UUID,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>>

}