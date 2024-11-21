package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper

import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryEconomyProvider
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
        TODO("Not yet implemented")
    }

    override fun deleteAccount(): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<Collection<String>> {
        TODO("Not yet implemented")
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal,
    ): CompletableFuture<MutableCollection<EconomyTransaction>> {
        TODO("Not yet implemented")
    }

    override fun retrieveMemberIds(): CompletableFuture<Collection<UUID>> {
        TODO("Not yet implemented")
    }

    override fun isMember(player: UUID): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun setPermissions(
        player: UUID,
        permissionsMap: MutableMap<AccountPermission, TriState>,
    ): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun retrievePermissions(player: UUID): CompletableFuture<Map<AccountPermission, TriState>> {
        TODO("Not yet implemented")
    }

    override fun retrievePermissionsMap(): CompletableFuture<MutableMap<UUID, Map<AccountPermission, TriState>>> {
        TODO("Not yet implemented")
    }

    override fun hasPermissions(player: UUID, vararg permissions: AccountPermission): CompletableFuture<TriState> {
        TODO("Not yet implemented")
    }

    override fun identifier(): NamespacedKey {
        return TreasuryUtil.polyNskToTreasury(polyObj.namespacedKey)
    }
}