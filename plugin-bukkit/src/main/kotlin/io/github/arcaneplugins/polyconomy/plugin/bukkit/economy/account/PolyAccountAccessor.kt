package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.account

import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.account.accessor.AccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.NonPlayerAccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.PlayerAccountAccessor
import java.util.concurrent.CompletableFuture

object PolyAccountAccessor : AccountAccessor {

    override fun player(): PlayerAccountAccessor {
        return PlayerAccountAccessorImpl
    }

    override fun nonPlayer(): NonPlayerAccountAccessor {
        return NonPlayerAccountAccessorImpl
    }

    object PlayerAccountAccessorImpl : PlayerAccountAccessor() {
        override fun getOrCreate(
            context: PlayerAccountCreateContext
        ): CompletableFuture<PlayerAccount> {
            return CompletableFuture.completedFuture(
                PolyPlayerAccount(context.uniqueId)
            )
        }
    }

    object NonPlayerAccountAccessorImpl : NonPlayerAccountAccessor() {
        override fun getOrCreate(
            context: NonPlayerAccountCreateContext
        ): CompletableFuture<NonPlayerAccount> {
            return CompletableFuture.completedFuture(
                PolyNonPlayerAccount(context.identifier)
            )
        }
    }

}