package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.ECONOMY_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.account.PolyAccountAccessor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ConcurrentManager.execSvc
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.common.response.Response
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

    lateinit var primaryCurrency: Currency
        private set

    lateinit var primaryLocaleId: Locale
        private set

    val currencies: MutableList<Currency> = mutableListOf()

    fun load() {
        loadPrimaryLocale()
        loadCurrencies()
        loadPrimaryCurrency()
    }

    private fun loadCurrencies() {
        currencies.clear()

        SettingsCfg
            .rootNode
            .node("currencies")
            .childrenList()
            .filter { currencyNode -> currencyNode.node("enabled").getBoolean(true) }
            .forEach { currencyNode ->
                Log.d(ECONOMY_MANAGER) { "Parsing currency node @ ${currencyNode.path()}" }

                currencies.add(
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

                            if(!map.containsKey(primaryLocaleId))
                                map[primaryLocaleId] = '.'

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
                                    primaryLocaleId,
                                    '.'
                                )
                            } else {
                                decimalLocaleMap.getValue(locale)
                            }
                        }

                        override fun getLocaleDecimalMap(): Map<Locale, Char> {
                            return decimalLocaleMap
                        }

                        override fun getDisplayNameSingular(): String {
                            return currencyNode
                                .node("display-name-singular")
                                .string!!
                        }

                        override fun getDisplayNamePlural(): String {
                            return currencyNode
                                .node("display-name-plural")
                                .string!!
                        }

                        override fun getPrecision(): Int {
                            return PRECISION
                        }

                        override fun isPrimary(): Boolean {
                            return this == primaryCurrency
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
                            locale: Locale?
                        ): CompletableFuture<Response<BigDecimal>> {
                            return CompletableFuture.supplyAsync(
                                {
                                    return@supplyAsync try {
                                        Response.success(
                                            BigDecimal(
                                                formattedAmount.replace(
                                                    parserRegex, ""
                                                )
                                            )
                                        )
                                    } catch (ex: Exception) {
                                        Response.failure { ex.message!! }
                                    }
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
                                .replace("%display-name%", let { _ ->
                                    return@let if(amount.compareTo(BigDecimal.ONE) == 1) {
                                        displayNameSingular
                                    } else {
                                        displayNamePlural
                                    }
                                })
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
        primaryCurrency = findCurrencyNonNull(
            SettingsCfg
                .rootNode
                .node("primary-currency")
                .string!!
        )
    }

    private fun loadPrimaryLocale() {
        primaryLocaleId = Locale(
            SettingsCfg
                .rootNode
                .node("primary-locale")
                .getString("en_US")
        )
    }

    override fun accountAccessor(): AccountAccessor {
        return PolyAccountAccessor
    }

    override fun hasAccount(accountData: AccountData): CompletableFuture<Response<TriState>> {
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

    override fun retrievePlayerAccountIds(): CompletableFuture<Response<Collection<UUID>>> {
        return CompletableFuture.supplyAsync(
            {
                StorageManager.currentHandler!!.retrievePlayerAccountIdsSync()
            },
            execSvc
        )
    }

    override fun retrieveNonPlayerAccountIds(): CompletableFuture<Response<Collection<NamespacedKey>>> {
        return CompletableFuture.supplyAsync(
            {
                StorageManager.currentHandler!!.retrieveNonPlayerAccountIdsSync()
            },
            execSvc
        )
    }

    override fun getPrimaryCurrency(): Currency {
        return primaryCurrency
    }

    override fun findCurrency(identifier: String): Optional<Currency> {
        return Optional.ofNullable(
            currencies.firstOrNull { it.identifier.equals(identifier, true) }
        )
    }

    fun findCurrencyNonNull(identifier: String): Currency {
        val currency = findCurrency(identifier)

        if(currency.isPresent) {
            return currency.get()
        }

        throw IllegalArgumentException(
            """
            Unable to find currency by ID '${identifier}'.
            """.trimIndent()
        )
    }

    override fun getCurrencies(): Set<Currency> {
        return currencies.toSet()
    }

    override fun registerCurrency(currency: Currency): CompletableFuture<Response<TriState>> {
        return CompletableFuture.supplyAsync(
            {
                try {
                    if(currencies
                        .any { it.identifier.equals(currency.identifier, true) }
                    ) {
                        return@supplyAsync Response.failure {
                            """
                            A currency of the ID '${currency.identifier}' is already registered.
                            """.trimIndent()
                        }
                    }

                    currencies.add(currency)

                    return@supplyAsync Response.success(TriState.TRUE)
                } catch (ex: Exception) {
                    return@supplyAsync Response.failure { ex.message ?: "?" }
                }
            },
            execSvc
        )
    }

    override fun unregisterCurrency(currency: Currency): CompletableFuture<Response<TriState>> {
        return CompletableFuture.supplyAsync(
            {
                try {
                    if(!currencies.contains(currency))
                        return@supplyAsync Response.success(TriState.UNSPECIFIED)

                    currencies.remove(currency)

                    return@supplyAsync Response.success(TriState.TRUE)
                } catch(ex: Exception) {
                    return@supplyAsync Response.failure { ex.message ?: "?" }
                }
            },
            execSvc
        )
    }

}