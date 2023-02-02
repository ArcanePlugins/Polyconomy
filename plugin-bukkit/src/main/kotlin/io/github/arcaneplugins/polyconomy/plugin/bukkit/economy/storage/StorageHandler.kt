package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage

import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.PolyNonPlayerAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.PolyPlayerAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.account.permission.PolyAccountPermission
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.currency.PolyCurrency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponse
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response.PolyResponseError
import io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.transaction.PolyTransaction
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.ConcurrentManager
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyNamespacedKey
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.PolyTriState
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

abstract class StorageHandler(
    val id: String
) {

    var connected: Boolean = false
        protected set

    abstract fun connect()

    abstract fun disconnect()

    @Suppress("unused") //TODO use
    fun hasPlayerAccountAsync(
        player: UUID
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    hasPlayerAccountSync(player)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "hasPlayerAccountAsync; player=${player}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun hasPlayerAccountSync(
        player: UUID
    ): PolyResponse<PolyTriState>

    @Suppress("unused") //TODO use
    fun hasNonPlayerAccountAsync(
        id: PolyNamespacedKey
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    hasNonPlayerAccountSync(id)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "hasNonPlayerAccountAsync; id=${id}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun hasNonPlayerAccountSync(
        id: PolyNamespacedKey
    ): PolyResponse<PolyTriState>

    fun retrieveNameAsync(
        account: PolyPlayerAccount
    ): CompletableFuture<PolyResponse<String?>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrieveNameSync(account)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrieveNameAsync; player=${account.player}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrieveNameSync(
        account: PolyPlayerAccount
    ): PolyResponse<String?>

    fun retrieveNameAsync(
        account: PolyNonPlayerAccount
    ): CompletableFuture<PolyResponse<String?>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrieveNameSync(account)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrieveNameAsync; id=${account.id}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrieveNameSync(
        account: PolyNonPlayerAccount
    ): PolyResponse<String?>

    @Suppress("unused") //TODO use
    fun renameAsync(
        account: PolyPlayerAccount,
        name: String?
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    renameSync(account, name)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "renameAsync; player=${account.player}; name=${name}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun renameSync(
        account: PolyPlayerAccount,
        name: String?
    ): PolyResponse<PolyTriState>

    @Suppress("unused") //TODO use
    fun renameAsync(
        account: PolyNonPlayerAccount,
        name: String?
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    renameSync(account, name)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "renameAsync; player=${account.id}; name=${name}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun renameSync(
        account: PolyNonPlayerAccount,
        name: String?
    ): PolyResponse<PolyTriState>

    fun deleteAccountAsync(
        account: PolyPlayerAccount,
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    deleteAccountSync(account)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "deleteAccountAsync; player=${account.player}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun deleteAccountSync(
        account: PolyPlayerAccount,
    ): PolyResponse<PolyTriState>

    fun deleteAccountAsync(
        account: PolyNonPlayerAccount,
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    deleteAccountSync(account)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "deleteAccountAsync; id=${account.id}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun deleteAccountSync(
        account: PolyNonPlayerAccount,
    ): PolyResponse<PolyTriState>

    fun retrieveBalanceAsync(
        account: PolyPlayerAccount,
        currency: PolyCurrency
    ): CompletableFuture<PolyResponse<BigDecimal>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrieveBalanceSync(account, currency)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrieveBalanceAsync; player=${account.player}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrieveBalanceSync(
        account: PolyPlayerAccount,
        currency: PolyCurrency
    ): PolyResponse<BigDecimal>

    fun retrieveBalanceAsync(
        account: PolyNonPlayerAccount,
        currency: PolyCurrency
    ): CompletableFuture<PolyResponse<BigDecimal>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrieveBalanceSync(account, currency)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrieveBalanceAsync; player=${account.id}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrieveBalanceSync(
        account: PolyNonPlayerAccount,
        currency: PolyCurrency
    ): PolyResponse<BigDecimal>

    fun doTransactionAsync(
        account: PolyPlayerAccount,
        transaction: PolyTransaction
    ): CompletableFuture<PolyResponse<BigDecimal>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    doTransactionSync(account, transaction)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "doTransactionAsync; player=${account.player}; amount=${transaction.amount}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun doTransactionSync(
        account: PolyPlayerAccount,
        transaction: PolyTransaction
    ): PolyResponse<BigDecimal>

    fun doTransactionAsync(
        account: PolyNonPlayerAccount,
        transaction: PolyTransaction
    ): CompletableFuture<PolyResponse<BigDecimal>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    doTransactionSync(account, transaction)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "doTransactionAsync; id=${account.id}; amount=${transaction.amount}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun doTransactionSync(
        account: PolyNonPlayerAccount,
        transaction: PolyTransaction
    ): PolyResponse<BigDecimal>

    fun retrieveHeldCurrenciesAsync(
        account: PolyPlayerAccount
    ): CompletableFuture<PolyResponse<Collection<String>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrieveHeldCurrenciesSync(account)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrieveHeldCurrenciesAsync; player=${account.player}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrieveHeldCurrenciesSync(
        account: PolyPlayerAccount
    ): PolyResponse<Collection<String>>

    fun retrieveHeldCurrenciesAsync(
        account: PolyNonPlayerAccount
    ): CompletableFuture<PolyResponse<Collection<String>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrieveHeldCurrenciesSync(account)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrieveHeldCurrenciesAsync; id=${account.id}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrieveHeldCurrenciesSync(
        account: PolyNonPlayerAccount
    ): PolyResponse<Collection<String>>

    fun retrieveTransactionHistoryAsync(
        account: PolyPlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): CompletableFuture<PolyResponse<Collection<PolyTransaction>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrieveTransactionHistorySync(account, transactionCount, from, to)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrieveTransactionHistoryAsync; player=${account.player}; " +
                            "transactionCount=${transactionCount}; from=${from}; to=${to}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrieveTransactionHistorySync(
        account: PolyPlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): PolyResponse<Collection<PolyTransaction>>

    fun retrieveTransactionHistoryAsync(
        account: PolyNonPlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): CompletableFuture<PolyResponse<Collection<PolyTransaction>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrieveTransactionHistorySync(account, transactionCount, from, to)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrieveTransactionHistoryAsync; id=${account.id}; " +
                            "transactionCount=${transactionCount}; from=${from}; to=${to}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrieveTransactionHistorySync(
        account: PolyNonPlayerAccount,
        transactionCount: Int,
        from: Instant,
        to: Instant
    ): PolyResponse<Collection<PolyTransaction>>

    fun retrieveMemberIdsAsync(
        account: PolyNonPlayerAccount
    ): CompletableFuture<PolyResponse<Collection<UUID>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrieveMemberIdsSync(account)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrieveMemberIdsAsync; id=${account.id}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrieveMemberIdsSync(
        account: PolyNonPlayerAccount
    ): PolyResponse<Collection<UUID>>

    fun isMemberAsync(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    isMemberSync(account, memberPlayer)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "isMemberAsync; id=${account.id}; memberPlayer=${memberPlayer}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun isMemberSync(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID
    ): PolyResponse<PolyTriState>

    fun setPermissionsAsync(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID,
        permissionValue: PolyTriState,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    setPermissionsSync(account, memberPlayer, permissionValue, *permissions)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "setPermissionsSync; id=${account.id}; memberPlayer=" +
                            "${memberPlayer}; permissionValue=${permissionValue}; " +
                            "permissions=${permissions}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun setPermissionsSync(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID,
        permissionValue: PolyTriState,
        vararg permissions: PolyAccountPermission
    ): PolyResponse<PolyTriState>

    fun retrievePermissionsAsync(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID
    ): CompletableFuture<PolyResponse<Map<PolyAccountPermission, PolyTriState>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrievePermissionsSync(account, memberPlayer)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrievePermissionsAsync; id=${account.id}; " +
                                "memberPlayer=${memberPlayer}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrievePermissionsSync(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID
    ): PolyResponse<Map<PolyAccountPermission, PolyTriState>>

    fun retrievePermissionsMapAsync(
        account: PolyNonPlayerAccount
    ): CompletableFuture<PolyResponse<Map<UUID, Map<PolyAccountPermission, PolyTriState>>>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    retrievePermissionsMapSync(account)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "retrievePermissionsMapAsync; id=${account.id}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun retrievePermissionsMapSync(
        account: PolyNonPlayerAccount
    ): PolyResponse<Map<UUID, Map<PolyAccountPermission, PolyTriState>>>

    fun hasPermissionsAsync(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID,
        vararg permissions: PolyAccountPermission
    ): CompletableFuture<PolyResponse<PolyTriState>> {
        return CompletableFuture.supplyAsync(
            {
                return@supplyAsync try {
                    hasPermissionsSync(account, memberPlayer, *permissions)
                } catch(ex: Exception) {
                    PolyResponse(
                        name = "hasPermissionsAsync; id=${account.id}; memberPlayer=" +
                                "${memberPlayer}; permissions=${permissions}",
                        error = PolyResponseError.fromException(ex),
                        result = null
                    )
                }
            },
            ConcurrentManager.execSvc
        )
    }

    abstract fun hasPermissionsSync(
        account: PolyNonPlayerAccount,
        memberPlayer: UUID,
        vararg permissions: PolyAccountPermission
    ): PolyResponse<PolyTriState>

    abstract fun getOrGrantCurrencyDbIdSync(
        currencyId: String
    ): Int

}