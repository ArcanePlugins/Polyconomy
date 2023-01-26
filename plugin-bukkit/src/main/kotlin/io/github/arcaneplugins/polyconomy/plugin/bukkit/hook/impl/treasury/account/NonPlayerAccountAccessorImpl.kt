package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.account

import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.accessor.NonPlayerAccountAccessor
import java.util.concurrent.CompletableFuture

object NonPlayerAccountAccessorImpl : NonPlayerAccountAccessor() {

    override fun getOrCreate(context: NonPlayerAccountCreateContext): CompletableFuture<Response<NonPlayerAccount>> {
        TODO("Not yet implemented")
    }

}