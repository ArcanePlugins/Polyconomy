package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper

import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryEconomyProvider
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper.TreasuryUtil.convertAccountPermissionFromTreasury
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper.TreasuryUtil.convertAccountPermissionToTreasury
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper.TreasuryUtil.convertTransactionFromTreasury
import kotlinx.coroutines.runBlocking
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.economy.account.AccountPermission
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.CompletableFuture

class PtNonPlayerAccount(
    val provider: TreasuryEconomyProvider,
    val polyObj: io.github.arcaneplugins.polyconomy.api.account.NonPlayerAccount,
) : NonPlayerAccount {

    override fun getName(): Optional<String> {
        return runBlocking { Optional.ofNullable(polyObj.getName()) }
    }

    override fun setName(name: String?): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync runBlocking {
                polyObj.setName(name)
                return@runBlocking true
            }
        }
    }

    override fun retrieveBalance(currency: Currency): CompletableFuture<BigDecimal> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync runBlocking {
                polyObj.getBalance(
                    currency = provider.storageHandler().getCurrency(currency.identifier)!!
                )
            }
        }
    }

    override fun doTransaction(economyTransaction: EconomyTransaction): CompletableFuture<BigDecimal> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                val currency = provider.storageHandler().getCurrency(economyTransaction.currencyId)!!
                val balBefore = polyObj.getBalance(currency)
                polyObj.makeTransaction(convertTransactionFromTreasury(provider, economyTransaction))
                val balAfter = polyObj.getBalance(currency)

                balAfter.subtract(balBefore)
            }
        }
    }

    override fun deleteAccount(): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj.deleteAccount()
                true
            }
        }
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<Collection<String>> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj.getHeldCurrencies().map { it.name }
            }
        }
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal,
    ): CompletableFuture<Collection<EconomyTransaction>> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj
                    .getTransactionHistory(
                        transactionCount,
                        from,
                        to,
                    )
                    .map { TreasuryUtil.convertTransactionToTreasury(it) }
            }
        }
    }

    override fun retrieveMemberIds(): CompletableFuture<Collection<UUID>> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj.getMemberIds()
            }
        }
    }

    override fun isMember(player: UUID): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj.isMember(player)
            }
        }
    }

    override fun setPermissions(
        player: UUID,
        permissionsMap: Map<AccountPermission, TriState>,
    ): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj.setPermissions(
                    player = player,
                    perms = permissionsMap
                        .mapKeys { convertAccountPermissionFromTreasury(it.key) }
                        .mapValues { it.value.asBoolean() }
                )
                true
            }
        }
    }

    override fun retrievePermissions(player: UUID): CompletableFuture<Map<AccountPermission, TriState>> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj.getPermissions(player)
                    .mapKeys { convertAccountPermissionToTreasury(it.key) }
                    .filterKeys { it != null }
                    .mapKeys { it.key!! }
                    .mapValues { TriState.fromBoolean(it.value) }
            }
        }
    }

    override fun retrievePermissionsMap(): CompletableFuture<Map<UUID, Map<AccountPermission, TriState>>> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                polyObj.getPermissionsMap()
                    .mapValues { it1 ->
                        it1.value
                            .mapKeys { it2 ->
                                convertAccountPermissionToTreasury(it2.key)
                            }
                            .filterKeys { it != null }
                            .mapKeys { it.key!! }
                            .mapValues { it2 ->
                                TriState.fromBoolean(it2.value)
                            }
                    }
            }
        }
    }

    override fun hasPermissions(player: UUID, vararg permissions: AccountPermission): CompletableFuture<TriState> {
        return CompletableFuture.supplyAsync {
            runBlocking {
                TriState.fromBoolean(
                    polyObj.hasPermissions(
                        player,
                        permissions.map { convertAccountPermissionFromTreasury(it) }
                    )
                )
            }
        }
    }

    override fun identifier(): NamespacedKey {
        return TreasuryUtil.convertNamespacedKeyToTreasury(polyObj.namespacedKey)
    }
}