package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryEconomyProvider
import kotlinx.coroutines.runBlocking
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.accessor.NonPlayerAccountAccessor
import java.util.concurrent.CompletableFuture

class PtNonPlayerAccountAccessor(
    val plugin: Polyconomy,
    val provider: TreasuryEconomyProvider,
) : NonPlayerAccountAccessor() {
    override fun getOrCreate(context: NonPlayerAccountCreateContext): CompletableFuture<NonPlayerAccount> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync runBlocking {
                PtNonPlayerAccount(
                    provider = provider,
                    polyObj = provider.storageHandler().getOrCreateNonPlayerAccount(
                        provider.treasuryNskToPoly(context.identifier),
                        context.name
                    )
                )
            }
        }
    }
}