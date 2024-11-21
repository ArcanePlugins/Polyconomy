package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryEconomyProvider
import me.lokka30.treasury.api.economy.account.accessor.AccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.NonPlayerAccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.PlayerAccountAccessor

class PtAccountAccessor(
    plugin: Polyconomy,
    provider: TreasuryEconomyProvider,
) : AccountAccessor {
    val player = PtPlayerAccountAccessor(plugin, provider)
    val nonPlayer = PtNonPlayerAccountAccessor(plugin, provider)

    override fun player(): PlayerAccountAccessor = player
    override fun nonPlayer(): NonPlayerAccountAccessor = nonPlayer

}
