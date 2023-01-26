package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.HOOKS
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.EconomyApiHook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.Hook
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
import java.util.*
import java.util.concurrent.CompletableFuture

class TreasuryHook : Hook("Treasury"), EconomyApiHook, EconomyProvider {

    override fun registerService() {
        ServiceRegistry.INSTANCE.registerService(
            EconomyProvider::class.java,
            this,
            Polyconomy.instance!!.description.name,
            ServicePriority.NORMAL
        )
    }

    override fun unregisterService() {
        Log.d(HOOKS) { "Unregistering Treasury Service." }

        var service: Service<EconomyProvider>? = null
        for(otherService in ServiceRegistry.INSTANCE.allServicesFor(EconomyProvider::class.java)) {
            if(otherService.registrarName() != Polyconomy.instance!!.description.name) continue
            service = otherService
            break
        }

        if(service == null) {
            Log.i("Skipped unregistration of Treasury " +
                    "service as the service is not registered.")
            return
        }

        ServiceRegistry.INSTANCE.unregister(EconomyProvider::class.java, service)

        Log.d(HOOKS) { "Unregistered Treasury Service." }
    }

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