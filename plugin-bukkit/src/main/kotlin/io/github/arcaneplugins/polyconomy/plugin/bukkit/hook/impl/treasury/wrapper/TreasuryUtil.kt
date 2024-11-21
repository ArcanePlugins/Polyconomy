package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper

import io.github.arcaneplugins.polyconomy.api.account.AccountPermission
import io.github.arcaneplugins.polyconomy.api.account.AccountTransaction
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.account.TransactionType
import io.github.arcaneplugins.polyconomy.api.util.cause.NonPlayerCause
import io.github.arcaneplugins.polyconomy.api.util.cause.PlayerCause
import io.github.arcaneplugins.polyconomy.api.util.cause.PluginCause
import io.github.arcaneplugins.polyconomy.api.util.cause.ServerCause
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryEconomyProvider
import kotlinx.coroutines.runBlocking
import me.lokka30.treasury.api.common.Cause
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionImportance
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionType
import kotlin.jvm.optionals.getOrNull

object TreasuryUtil {

    fun convertNamespacedKeyFromTreasury(
        treasuryNsk: NamespacedKey
    ): io.github.arcaneplugins.polyconomy.api.util.NamespacedKey {
        return io.github.arcaneplugins.polyconomy.api.util.NamespacedKey(
            namespace = treasuryNsk.namespace,
            key = treasuryNsk.key,
        )
    }

    fun convertNamespacedKeyToTreasury(
        polyNsk: io.github.arcaneplugins.polyconomy.api.util.NamespacedKey
    ): NamespacedKey {
        return NamespacedKey.of(
            polyNsk.namespace,
            polyNsk.key,
        )
    }

    fun convertTransactionToTreasury(
        polyObj: AccountTransaction,
    ): EconomyTransaction {
        return EconomyTransaction(
            polyObj.currency.name,
            convertTransactionCauseToTreasury(polyObj.cause),
            polyObj.timestamp,
            convertTransactionTypeToTreasury(polyObj.type),
            polyObj.reason,
            polyObj.amount,
            convertTransactionImportanceToTreasury(polyObj.importance),
        )
    }

    fun convertTransactionFromTreasury(
        provider: TreasuryEconomyProvider,
        treasuryObj: EconomyTransaction,
    ): AccountTransaction {
        return runBlocking {
            AccountTransaction(
                amount = treasuryObj.amount,
                cause = convertTransactionCauseFromTreasury(treasuryObj.cause),
                currency = provider.storageHandler().getCurrency(treasuryObj.currencyId)!!,
                importance = convertTransactionImportanceFromTreasury(treasuryObj.importance),
                type = convertTransactionTypeFromTreasury(treasuryObj.type),
                reason = treasuryObj.reason.getOrNull(),
                timestamp = treasuryObj.timestamp,
            )
        }
    }

    fun convertTransactionTypeFromTreasury(
        treasuryObj: EconomyTransactionType
    ): TransactionType {
        return when (treasuryObj) {
            EconomyTransactionType.SET -> TransactionType.SET
            EconomyTransactionType.DEPOSIT -> TransactionType.DEPOSIT
            EconomyTransactionType.WITHDRAWAL -> TransactionType.WITHDRAW
            else -> TransactionType.UNKNOWN
        }
    }

    fun convertTransactionTypeToTreasury(
        polyObj: TransactionType
    ): EconomyTransactionType {
        return when (polyObj) {
            TransactionType.SET -> EconomyTransactionType.SET
            TransactionType.DEPOSIT -> EconomyTransactionType.DEPOSIT
            TransactionType.WITHDRAW -> EconomyTransactionType.WITHDRAWAL
            TransactionType.RESET -> EconomyTransactionType.SET
            TransactionType.UNKNOWN -> EconomyTransactionType.SET
        }
    }

    fun convertTransactionCauseToTreasury(
        polyObj: io.github.arcaneplugins.polyconomy.api.util.cause.Cause
    ): Cause<*> {
        return when (polyObj) {
            is PlayerCause -> Cause.player(polyObj.uuid)
            is NonPlayerCause -> Cause.nonPlayer(convertNamespacedKeyToTreasury(polyObj.namespacedKey))
            is PluginCause -> Cause.plugin(convertNamespacedKeyToTreasury(polyObj.namespacedKey))
            is ServerCause -> Cause.SERVER
            else -> throw IllegalArgumentException("${polyObj.javaClass.simpleName} type is not expected")
        }
    }

    fun convertTransactionCauseFromTreasury(
        treasuryObj: Cause<*>
    ): io.github.arcaneplugins.polyconomy.api.util.cause.Cause {
        return when {
            treasuryObj is Cause.Player -> PlayerCause(treasuryObj.identifier())
            treasuryObj is Cause.NonPlayer -> NonPlayerCause(convertNamespacedKeyFromTreasury(treasuryObj.identifier()))
            treasuryObj is Cause.Plugin -> PluginCause(convertNamespacedKeyFromTreasury(treasuryObj.identifier()))
            treasuryObj.identifier() == "Server" -> ServerCause
            else -> object : io.github.arcaneplugins.polyconomy.api.util.cause.Cause(treasuryObj.identifier()) {}
        }
    }

    fun convertTransactionImportanceFromTreasury(
        treasuryObj: EconomyTransactionImportance,
    ): TransactionImportance {
        return when (treasuryObj) {
            EconomyTransactionImportance.LOW -> TransactionImportance.LOW
            EconomyTransactionImportance.NORMAL -> TransactionImportance.MEDIUM
            EconomyTransactionImportance.HIGH -> TransactionImportance.HIGH
        }
    }

    fun convertTransactionImportanceToTreasury(
        polyObj: TransactionImportance,
    ): EconomyTransactionImportance {
        return when (polyObj) {
            TransactionImportance.HIGH -> EconomyTransactionImportance.HIGH
            TransactionImportance.MEDIUM -> EconomyTransactionImportance.NORMAL
            TransactionImportance.LOW -> EconomyTransactionImportance.LOW
        }
    }

    fun convertAccountPermissionToTreasury(
        polyObj: AccountPermission
    ): me.lokka30.treasury.api.economy.account.AccountPermission {
        return when (polyObj) {
            AccountPermission.MODIFY_PERMISSIONS ->
                me.lokka30.treasury.api.economy.account.AccountPermission.MODIFY_PERMISSIONS
            AccountPermission.DEPOSIT ->
                me.lokka30.treasury.api.economy.account.AccountPermission.DEPOSIT
            AccountPermission.WITHDRAW ->
                me.lokka30.treasury.api.economy.account.AccountPermission.WITHDRAW
            AccountPermission.BALANCE ->
                me.lokka30.treasury.api.economy.account.AccountPermission.BALANCE
        }
    }

    fun convertAccountPermissionFromTreasury(
        treasuryObj: me.lokka30.treasury.api.economy.account.AccountPermission
    ): AccountPermission {
        return when (treasuryObj) {
            me.lokka30.treasury.api.economy.account.AccountPermission.MODIFY_PERMISSIONS ->
                AccountPermission.MODIFY_PERMISSIONS
            me.lokka30.treasury.api.economy.account.AccountPermission.DEPOSIT ->
                AccountPermission.DEPOSIT
            me.lokka30.treasury.api.economy.account.AccountPermission.WITHDRAW ->
                AccountPermission.WITHDRAW
            me.lokka30.treasury.api.economy.account.AccountPermission.BALANCE ->
                AccountPermission.BALANCE
        }
    }

}














