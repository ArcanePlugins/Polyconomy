package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.HOOK_TREASURY
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.EconomyManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.Hook
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.HookType
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.account.AccountAccessorImpl
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ConcurrentManager.singleThreadExecSvc
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyNamespacedKey
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyTriState
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
import me.lokka30.treasury.api.economy.response.EconomyFailureReason
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

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

    object PolyTreasuryEconomyProvider : EconomyProvider {
        override fun accountAccessor(): AccountAccessor {
            return AccountAccessorImpl
        }

        override fun hasAccount(accountData: AccountData): CompletableFuture<Response<TriState>> {
            return CompletableFuture.supplyAsync({
                val polyResponse: PolyResponse<PolyTriState> = if(accountData.isPlayerAccount) {
                    StorageManager
                        .currentHandler!!
                        .hasPlayerAccount(
                            accountData.playerIdentifier.get()
                        )
                        .join()
                } else {
                    StorageManager
                        .currentHandler!!
                        .hasNonPlayerAccount(
                            PolyNamespacedKey.fromTreasury(
                                accountData.nonPlayerIdentifier.get()
                            )
                        )
                        .join()
                }

                if(polyResponse.error == null) {
                    return@supplyAsync Response.success(polyResponse.result!!.toTreasury)
                } else {
                    return@supplyAsync Response.failure(polyResponse.error.toTreasury())
                }
            }, singleThreadExecSvc)
        }

        override fun retrievePlayerAccountIds(): CompletableFuture<Response<Collection<UUID>>> {
            return CompletableFuture.supplyAsync({
                TODO("Not yet implemented")
            }, singleThreadExecSvc)
        }

        override fun retrieveNonPlayerAccountIds(): CompletableFuture<Response<Collection<NamespacedKey>>> {
            return CompletableFuture.supplyAsync({
                TODO("Not yet implemented")
            }, singleThreadExecSvc)
        }

        override fun getPrimaryCurrency(): Currency {
            return EconomyManager.primaryCurrency.toTreasury()
        }

        override fun findCurrency(identifier: String): Optional<Currency> {
            val polyCurrency: PolyCurrency? = EconomyManager.findCurrencyById(identifier)

            return if(polyCurrency == null) {
                Optional.empty()
            } else {
                Optional.of(polyCurrency.toTreasury())
            }
        }

        override fun getCurrencies(): Set<Currency> {
            return EconomyManager
                .currencies
                .stream()
                .map(PolyCurrency::toTreasury)
                .collect(Collectors.toSet())
        }

        override fun registerCurrency(treasuryCurrency: Currency): CompletableFuture<Response<TriState>> {
            return CompletableFuture.supplyAsync({
                if(EconomyManager.currencies.any { polyCurrency ->
                        polyCurrency.id.equals(
                            other = treasuryCurrency.identifier,
                            ignoreCase = true
                        )
                }) {
                    return@supplyAsync Response.failure(
                        EconomyFailureReason.CURRENCY_ALREADY_REGISTERED
                    )
                }

                return@supplyAsync try {
                    EconomyManager.currencies.add(
                        PolyCurrency.fromTreasury(treasuryCurrency)
                    )
                    Response.success(TriState.TRUE)
                } catch(ex: Exception) {
                    Response.failure { ex.message!! }
                }
            }, singleThreadExecSvc)
        }

        override fun unregisterCurrency(
            currency: Currency
        ): CompletableFuture<Response<TriState>> {
            return CompletableFuture.supplyAsync({
                if(currency.isPrimary) {
                    return@supplyAsync Response.failure { "Unable to unregister primary currency" }
                }

                return@supplyAsync try {
                    EconomyManager.currencies.removeIf { it.id == currency.identifier }
                    Response.success(TriState.TRUE)
                } catch(ex: Exception) {
                    Response.failure { ex.message!! }
                }
            }, singleThreadExecSvc)
        }
    }

}