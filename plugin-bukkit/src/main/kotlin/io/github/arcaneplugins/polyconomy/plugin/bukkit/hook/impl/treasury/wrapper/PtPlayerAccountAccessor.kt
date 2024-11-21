package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryEconomyProvider
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.account.accessor.PlayerAccountAccessor
import java.util.concurrent.CompletableFuture

class PtPlayerAccountAccessor(
    val plugin: Polyconomy,
    val provider: TreasuryEconomyProvider,
) : PlayerAccountAccessor() {
    override fun getOrCreate(context: PlayerAccountCreateContext): CompletableFuture<PlayerAccount> {
        return CompletableFuture<PlayerAccount>.supplyAsync {
            TODO("Not yet implemented")
        }
    }
}