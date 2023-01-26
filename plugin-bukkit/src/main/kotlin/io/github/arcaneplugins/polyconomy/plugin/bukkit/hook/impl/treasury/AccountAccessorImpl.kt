package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury

import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.account.accessor.AccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.NonPlayerAccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.PlayerAccountAccessor
import java.util.concurrent.CompletableFuture

object AccountAccessorImpl : AccountAccessor {

    override fun player(): PlayerAccountAccessor {
        return object : PlayerAccountAccessor() {
            override fun getOrCreate(context: PlayerAccountCreateContext): CompletableFuture<Response<PlayerAccount>> {
                TODO("Not yet implemented")
            }
        }
    }

    override fun nonPlayer(): NonPlayerAccountAccessor {
        return object : NonPlayerAccountAccessor() {
            override fun getOrCreate(context: NonPlayerAccountCreateContext): CompletableFuture<Response<NonPlayerAccount>> {
                TODO("Not yet implemented")
            }
        }
    }

}