package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.account

import me.lokka30.treasury.api.economy.account.accessor.AccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.NonPlayerAccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.PlayerAccountAccessor

object AccountAccessorImpl : AccountAccessor {

    override fun player(): PlayerAccountAccessor {
        return PlayerAccountAccessorImpl
    }

    override fun nonPlayer(): NonPlayerAccountAccessor {
        return NonPlayerAccountAccessorImpl
    }

}