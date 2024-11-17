package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.account.accessor.AccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.NonPlayerAccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.PlayerAccountAccessor
import java.util.concurrent.CompletableFuture

class PolyAccountAccessor(
    val plugin: Polyconomy,
) : AccountAccessor {

    override fun player(): PlayerAccountAccessor {
        return PlayerAccountAccessorImpl(plugin)
    }

    override fun nonPlayer(): NonPlayerAccountAccessor {
        return NonPlayerAccountAccessorImpl(plugin)
    }

    class PlayerAccountAccessorImpl(
        val plugin: Polyconomy,
    ) : PlayerAccountAccessor() {
        override fun getOrCreate(
            context: PlayerAccountCreateContext,
        ): CompletableFuture<PlayerAccount> {
            return CompletableFuture.completedFuture(
                PolyPlayerAccount(plugin, context.uniqueId)
            )
        }
    }

    class NonPlayerAccountAccessorImpl(
        val plugin: Polyconomy,
    ) : NonPlayerAccountAccessor() {
        override fun getOrCreate(
            context: NonPlayerAccountCreateContext,
        ): CompletableFuture<NonPlayerAccount> {
            return CompletableFuture.completedFuture(
                PolyNonPlayerAccount(plugin, context.identifier)
            )
        }
    }

}