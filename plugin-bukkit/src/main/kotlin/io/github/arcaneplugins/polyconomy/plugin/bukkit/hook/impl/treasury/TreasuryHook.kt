package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.HOOK_TREASURY
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.EconomyManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.Hook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookType
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import me.lokka30.treasury.api.common.service.Service
import me.lokka30.treasury.api.common.service.ServicePriority
import me.lokka30.treasury.api.common.service.ServiceRegistry
import me.lokka30.treasury.api.economy.EconomyProvider
import org.bukkit.Bukkit

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
            EconomyManager,
            Polyconomy.instance.description.name,
            ServicePriority.NORMAL
        )
        registered = true
    }

    override fun unregister() {
        Log.d(HOOK_TREASURY) { "Unregistering Treasury service." }

        var service: Service<EconomyProvider>? = null
        for(otherService in ServiceRegistry.INSTANCE.allServicesFor(EconomyProvider::class.java)) {
            if(otherService.registrarName() != Polyconomy.instance.description.name) continue
            service = otherService
            break
        }

        if(service == null) {
            Log.d(HOOK_TREASURY) { "Can't unregister service: is already unregistered." }
            return
        }

        ServiceRegistry.INSTANCE.unregister(EconomyProvider::class.java, service)

        registered = false

        Log.d(HOOK_TREASURY) { "Unregistered Treasury service successfully." }
    }

}