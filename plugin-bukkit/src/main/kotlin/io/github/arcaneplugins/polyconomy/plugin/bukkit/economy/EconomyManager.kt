package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.ECONOMY_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.account.PolyAccountAccessor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ConcurrentManager.execSvc
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.economy.EconomyProvider
import me.lokka30.treasury.api.economy.account.Account
import me.lokka30.treasury.api.economy.account.AccountData
import me.lokka30.treasury.api.economy.account.accessor.AccountAccessor
import me.lokka30.treasury.api.economy.currency.Currency
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.CompletableFuture

object EconomyManager : EconomyProvider {

    const val PRECISION = 4

    lateinit var primCurrency: Currency
        private set

    lateinit var primLocale: Locale
        private set

    val registeredCurrencies = linkedSetOf<Currency>()

    fun load() {
        loadPrimaryLocale()
        loadCurrencies()
        loadPrimaryCurrency()
    }

    private fun loadCurrencies() {
        registeredCurrencies.clear()

        SettingsCfg
            .rootNode
            .node("currencies")
            .childrenList()
            .filter { currencyNode -> currencyNode.node("enabled").getBoolean(true) }
            .forEach { currencyNode ->
                Log.d(ECONOMY_MANAGER) { "Parsing currency node @ ${currencyNode.path()}" }

                registeredCurrencies.add(
                    object : Currency {
                        val parserRegex = Regex(
                            pattern = "[^\\\\d.]+"
                        )

                        val amountFormat = DecimalFormat(
                            currencyNode
                                .node("amount-format")
                                .getString("#,##0.00")
                        )

                        val decimalLocaleMap: Map<Locale, Char> = let { _ ->
                            val map: MutableMap<Locale, Char> = mutableMapOf()

                            currencyNode
                                .node("decimal")
                                .childrenList()
                                .forEach { localeDecimalNode ->
                                    val locale = Locale(
                                        localeDecimalNode
                                            .node("locale")
                                            .string!!
                                    )

                                    val character: Char = localeDecimalNode
                                        .node("character")
                                        .string!!
                                        .first()

                                    map[locale] = character
                                }

                            if(!map.containsKey(primLocale))
                                map[primLocale] = '.'

                            return@let map
                        }

                        val presentationFormat = currencyNode
                            .node("presentation-format")
                            .getString("%amount% %display-name%")

                        override fun getIdentifier(): String {
                            return currencyNode
                                .node("currency")
                                .string!!
                        }

                        override fun getSymbol(): String {
                            return currencyNode.node("symbol").string!!
                        }

                        override fun getDecimal(locale: Locale?): Char {
                            return if(locale == null || !decimalLocaleMap.containsKey(locale)) {
                                decimalLocaleMap.getOrDefault(
                                    primLocale,
                                    '.'
                                )
                            } else {
                                decimalLocaleMap.getValue(locale)
                            }
                        }

                        override fun getLocaleDecimalMap(): Map<Locale, Char> {
                            return decimalLocaleMap
                        }

                        override fun getDisplayName(value: BigDecimal, locale: Locale?): String {
                            TODO("Not yet implemented")
                        }

                        override fun getPrecision(): Int {
                            return PRECISION
                        }

                        override fun isPrimary(): Boolean {
                            return this == primCurrency
                        }

                        override fun getStartingBalance(account: Account): BigDecimal {
                            return BigDecimal(
                                currencyNode
                                    .node("starting-balance")
                                    .double
                            )
                        }

                        override fun getConversionRate(): BigDecimal {
                            return BigDecimal(
                                currencyNode.node("conversion-rate").getDouble(0.0)
                            )
                        }

                        override fun parse(
                            formattedAmount: String,
                            locale: Locale?,
                        ): CompletableFuture<BigDecimal> {
                            return CompletableFuture.supplyAsync(
                                {
                                    return@supplyAsync BigDecimal(
                                        formattedAmount.replace(
                                            parserRegex, ""
                                        )
                                    )
                                },
                                execSvc
                            )
                        }

                        override fun format(
                            amount: BigDecimal,
                            locale: Locale?
                        ): String {
                            return presentationFormat
                                .replace("%symbol%", symbol)
                                .replace("%amount%", let { _ ->
                                    return@let amountFormat
                                        .format(amount)
                                        .replace(".", getDecimal(locale).toString())
                                })
                                .replace("%display-name%", getDisplayName(amount, locale))
                        }

                        override fun format(
                            amount: BigDecimal,
                            locale: Locale?,
                            precision: Int
                        ): String {
                            return format(amount, locale)
                        }

                    }
                )
            }
    }

    private fun loadPrimaryCurrency() {
        primCurrency = findCurrencyNonNull(
            SettingsCfg
                .rootNode
                .node("primary-currency")
                .string!!
        )
    }

    private fun loadPrimaryLocale() {
        primLocale = Locale(
            SettingsCfg
                .rootNode
                .node("primary-locale")
                .getString("en_US")
        )
    }

    override fun accountAccessor(): AccountAccessor {
        return PolyAccountAccessor
    }

    override fun hasAccount(accountData: AccountData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync if(accountData.isPlayerAccount) {
                    StorageManager
                        .currentHandler!!
                        .hasPlayerAccountSync(accountData.playerIdentifier.get())
                } else {
                    StorageManager
                        .currentHandler!!
                        .hasNonPlayerAccountSync(accountData.nonPlayerIdentifier.get())
                }
            },
            execSvc
        )
    }

    override fun retrievePlayerAccountIds(): CompletableFuture<Collection<UUID>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync StorageManager.currentHandler!!.retrievePlayerAccountIdsSync()
            },
            execSvc
        )
    }

    override fun retrieveNonPlayerAccountIds(): CompletableFuture<Collection<NamespacedKey>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync StorageManager.currentHandler!!.retrieveNonPlayerAccountIdsSync()
            },
            execSvc
        )
    }

    override fun getPrimaryCurrency(): Currency {
        return primCurrency
    }

    override fun findCurrency(identifier: String): Optional<Currency> {
        return Optional.ofNullable(
            registeredCurrencies.firstOrNull {
                it.identifier.equals(identifier, ignoreCase = true)
            }
        )
    }

    fun findCurrencyNonNull(identifier: String): Currency {
        val currency = findCurrency(identifier)

        if(currency.isPresent) {
            return currency.get()
        }

        throw IllegalArgumentException(
            "Unable to find currency by ID '${identifier}'."
        )
    }

    override fun getCurrencies(): Set<Currency> {
        return registeredCurrencies
    }

    override fun registerCurrency(currency: Currency): CompletableFuture<TriState> {
        return CompletableFuture.supplyAsync(
            {
                if(registeredCurrencies
                    .any { it.identifier.equals(currency.identifier, true) }
                ) {
                    return@supplyAsync TriState.UNSPECIFIED
                }

                registeredCurrencies.add(currency)

                return@supplyAsync TriState.TRUE
            },
            execSvc
        )
    }

    override fun unregisterCurrency(currency: Currency): CompletableFuture<TriState> {
        return CompletableFuture.supplyAsync(
            {
                if(!registeredCurrencies.contains(currency))
                    return@supplyAsync TriState.UNSPECIFIED

                registeredCurrencies.remove(currency)

                return@supplyAsync TriState.TRUE
            },
            execSvc
        )
    }

}