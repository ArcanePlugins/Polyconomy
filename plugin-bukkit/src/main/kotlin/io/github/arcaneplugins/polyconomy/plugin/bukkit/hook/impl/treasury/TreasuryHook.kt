package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.HOOK_TREASURY
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.Hook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookType
import me.lokka30.treasury.api.common.service.Service
import me.lokka30.treasury.api.common.service.ServicePriority
import me.lokka30.treasury.api.common.service.ServiceRegistry
import me.lokka30.treasury.api.economy.EconomyProvider
import org.bukkit.Bukkit

class TreasuryHook(
    val plugin: Polyconomy,
) : Hook(
    id = TREASURY_PLUGIN_NAME,
    type = HookType.ECONOMY_API,
) {

    companion object {
        const val TREASURY_PLUGIN_NAME = "Treasury"
    }

    override fun canRegister(): Boolean {
        return !registered && Bukkit.getPluginManager().isPluginEnabled(TREASURY_PLUGIN_NAME)
    }

    override fun register() {
        ServiceRegistry.INSTANCE.registerService(
            EconomyProvider::class.java,
            plugin.economyManager,
            plugin.description.name,
            ServicePriority.NORMAL
        )
        registered = true
    }

    override fun unregister() {
        plugin.debugLog(HOOK_TREASURY) { "Unregistering service." }

        var service: Service<EconomyProvider>? = null
        for(otherService in ServiceRegistry.INSTANCE.allServicesFor(EconomyProvider::class.java)) {
            if(otherService.registrarName() != plugin.description.name) continue
            service = otherService
            break
        }

        if(service == null) {
            plugin.debugLog(HOOK_TREASURY) { "Can't unregister service: is already unregistered." }
            return
        }

        ServiceRegistry.INSTANCE.unregister(EconomyProvider::class.java, service)

        registered = false

        plugin.debugLog(HOOK_TREASURY) { "Unregistered service successfully." }
    }

}