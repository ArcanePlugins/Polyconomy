package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.account

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.PolyNonPlayerAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyNamespacedKey
import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.accessor.NonPlayerAccountAccessor
import java.util.concurrent.CompletableFuture

object NonPlayerAccountAccessorImpl : NonPlayerAccountAccessor() {

    override fun getOrCreate(context: NonPlayerAccountCreateContext): CompletableFuture<Response<NonPlayerAccount>> {
        return CompletableFuture.completedFuture(
            Response.success(
                NonPlayerAccountImpl(
                    PolyNonPlayerAccount(
                        PolyNamespacedKey.fromTreasury(
                            context.identifier
                        )
                    )
                )
            )
        )
    }

}