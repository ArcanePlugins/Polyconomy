package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.HOOK_TREASURY
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.Hook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookType
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.account.AccountAccessorImpl
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.common.response.Response
import me.lokka30.treasury.api.common.service.Service
import me.lokka30.treasury.api.common.service.ServicePriority
import me.lokka30.treasury.api.common.service.ServiceRegistry
import me.lokka30.treasury.api.economy.EconomyProvider
import me.lokka30.treasury.api.economy.account.AccountData
import me.lokka30.treasury.api.economy.account.accessor.AccountAccessor
import me.lokka30.treasury.api.economy.currency.Currency
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.CompletableFuture

object TreasuryHook : Hook(
    id = "Treasury",
    type = HookType.ECONOMY_API
) {

    override fun canRegister(): Boolean {
        return !registered && Bukkit.getPluginManager().isPluginEnabled("Treasury")
    }

    override fun register() {
        ServiceRegistry.INSTANCE.registerService(
            EconomyProvider::class.java,
            PolyTreasuryEconomyProvider,
            Polyconomy.instance!!.description.name,
            ServicePriority.NORMAL
        )
    }

    override fun unregister() {
        Log.d(HOOK_TREASURY) { "Unregistering Treasury service." }

        var service: Service<EconomyProvider>? = null
        for(otherService in ServiceRegistry.INSTANCE.allServicesFor(EconomyProvider::class.java)) {
            if(otherService.registrarName() != Polyconomy.instance!!.description.name) continue
            service = otherService
            break
        }

        if(service == null) {
            Log.d(HOOK_TREASURY) { "Can't unregister service: is already unregistered." }
            return
        }

        ServiceRegistry.INSTANCE.unregister(EconomyProvider::class.java, service)

        Log.d(HOOK_TREASURY) { "Unregistered Treasury service successfully." }
    }

    object PolyTreasuryEconomyProvider : EconomyProvider {
        override fun accountAccessor(): AccountAccessor {
            return AccountAccessorImpl
        }

        override fun hasAccount(accountData: AccountData): CompletableFuture<Response<TriState>> {
            TODO("Not yet implemented")
        }

        override fun retrievePlayerAccountIds(): CompletableFuture<Response<MutableCollection<UUID>>> {
            TODO("Not yet implemented")
        }

        override fun retrieveNonPlayerAccountIds(): CompletableFuture<Response<MutableCollection<NamespacedKey>>> {
            TODO("Not yet implemented")
        }

        override fun getPrimaryCurrency(): Currency {
            TODO("Not yet implemented")
        }

        override fun findCurrency(identifier: String): Optional<Currency> {
            TODO("Not yet implemented")
        }

        override fun getCurrencies(): MutableSet<Currency> {
            TODO("Not yet implemented")
        }

        override fun registerCurrency(currency: Currency): CompletableFuture<Response<TriState>> {
            TODO("Not yet implemented")
        }

        override fun unregisterCurrency(currency: Currency): CompletableFuture<Response<TriState>> {
            TODO("Not yet implemented")
        }
    }

}