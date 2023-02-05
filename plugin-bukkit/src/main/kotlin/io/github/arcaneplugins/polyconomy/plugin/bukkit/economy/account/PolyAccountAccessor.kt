package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.account

import me.lokka30.treasury.api.common.response.Response
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
        ): CompletableFuture<Response<PlayerAccount>> {
            return CompletableFuture.completedFuture(
                Response.success(
                    PolyPlayerAccount(context.uniqueId)
                )
            )
        }
    }

    object NonPlayerAccountAccessorImpl : NonPlayerAccountAccessor() {
        override fun getOrCreate(context: NonPlayerAccountCreateContext): CompletableFuture<Response<NonPlayerAccount>> {
            return CompletableFuture.completedFuture(
                Response.success(
                    PolyNonPlayerAccount(context.identifier)
                )
            )
        }
    }

}