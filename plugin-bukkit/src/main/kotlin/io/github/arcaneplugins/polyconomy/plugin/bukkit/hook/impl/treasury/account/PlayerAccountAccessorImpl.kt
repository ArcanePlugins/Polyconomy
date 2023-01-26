package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.account

import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.account.accessor.PlayerAccountAccessor
import java.util.concurrent.CompletableFuture

object PlayerAccountAccessorImpl : PlayerAccountAccessor() {

    override fun getOrCreate(context: PlayerAccountCreateContext): CompletableFuture<Response<PlayerAccount>> {
        TODO("Not yet implemented")
    }

}