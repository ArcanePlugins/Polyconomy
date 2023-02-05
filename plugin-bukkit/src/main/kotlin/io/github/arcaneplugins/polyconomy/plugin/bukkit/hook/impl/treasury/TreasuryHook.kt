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
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ConcurrentManager.execSvc
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
                        .hasPlayerAccountSync(
                            accountData.playerIdentifier.get()
                        )
                } else {
                    StorageManager
                        .currentHandler!!
                        .hasNonPlayerAccountSync(
                            PolyNamespacedKey.fromTreasury(
                                accountData.nonPlayerIdentifier.get()
                            )
                        )
                }

                if(polyResponse.successful()) {
                    return@supplyAsync Response.success(polyResponse.result!!.toTreasury())
                } else {
                    return@supplyAsync Response.failure(polyResponse.error!!.toTreasury())
                }
            }, execSvc)
        }

        override fun retrievePlayerAccountIds(): CompletableFuture<Response<Collection<UUID>>> {
            return CompletableFuture.supplyAsync({
                StorageManager.currentHandler!!.retrievePlayerAccountIdsSync().toTreasury()
            }, execSvc)
        }

        override fun retrieveNonPlayerAccountIds(): CompletableFuture<Response<Collection<NamespacedKey>>> {
            return CompletableFuture.supplyAsync({
                val polyResponse = StorageManager
                    .currentHandler!!
                    .retrieveNonPlayerAccountIdsSync()

                return@supplyAsync if(polyResponse.result != null) {
                    Response.success(
                        polyResponse
                            .result
                            .stream()
                            .map(PolyNamespacedKey::toTreasury)
                            .collect(Collectors.toList())
                    )
                } else if(polyResponse.error != null) {
                    Response.failure { polyResponse.error.desc() }
                } else {
                    throw IllegalStateException("PolyResponse state unknown")
                }
            }, execSvc)
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
                    return@supplyAsync Response.failure {
                        "Currency ID '${treasuryCurrency.identifier}' is already registered."
                    }
                }

                return@supplyAsync try {
                    EconomyManager.currencies.add(
                        PolyCurrency.fromTreasury(treasuryCurrency)
                    )
                    Response.success(TriState.TRUE)
                } catch(ex: Exception) {
                    Response.failure { ex.message!! }
                }
            }, execSvc)
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
            }, execSvc)
        }
    }

}