package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.wrapper

import io.github.arcaneplugins.polyconomy.api.account.AccountTransaction
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.account.TransactionType
import io.github.arcaneplugins.polyconomy.api.util.cause.NonPlayerCause
import io.github.arcaneplugins.polyconomy.api.util.cause.PlayerCause
import io.github.arcaneplugins.polyconomy.api.util.cause.PluginCause
import io.github.arcaneplugins.polyconomy.api.util.cause.ServerCause
import io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryEconomyProvider
import kotlinx.coroutines.runBlocking
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionImportance
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionType
import kotlin.jvm.optionals.getOrNull
import io.github.arcaneplugins.polyconomy.api.account.AccountPermission as PolyAccountPermission
import io.github.arcaneplugins.polyconomy.api.util.NamespacedKey as PolyNamespacedKey
import io.github.arcaneplugins.polyconomy.api.util.cause.Cause as PolyCause
import me.lokka30.treasury.api.common.Cause as TreasuryCause
import me.lokka30.treasury.api.economy.account.AccountPermission as TreasuryAccountPermission

object TreasuryUtil {

    fun convertNamespacedKeyFromTreasury(
        treasuryNsk: NamespacedKey,
    ): PolyNamespacedKey {
        return PolyNamespacedKey(
            namespace = treasuryNsk.namespace,
            key = treasuryNsk.key,
        )
    }

    fun convertNamespacedKeyToTreasury(
        polyNsk: PolyNamespacedKey,
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
        treasuryObj: EconomyTransactionType,
    ): TransactionType {
        return when (treasuryObj) {
            EconomyTransactionType.SET -> TransactionType.SET
            EconomyTransactionType.DEPOSIT -> TransactionType.DEPOSIT
            EconomyTransactionType.WITHDRAWAL -> TransactionType.WITHDRAW
        }
    }

    fun convertTransactionTypeToTreasury(
        polyObj: TransactionType,
    ): EconomyTransactionType {
        return when (polyObj) {
            TransactionType.SET -> EconomyTransactionType.SET
            TransactionType.DEPOSIT -> EconomyTransactionType.DEPOSIT
            TransactionType.WITHDRAW -> EconomyTransactionType.WITHDRAWAL
            TransactionType.RESET -> EconomyTransactionType.SET
        }
    }

    fun convertTransactionCauseToTreasury(
        polyObj: PolyCause,
    ): TreasuryCause<*> {
        return when (polyObj) {
            is PlayerCause -> TreasuryCause.player(polyObj.uuid)
            is NonPlayerCause -> TreasuryCause.nonPlayer(convertNamespacedKeyToTreasury(polyObj.namespacedKey))
            is PluginCause -> TreasuryCause.plugin(convertNamespacedKeyToTreasury(polyObj.namespacedKey))
            is ServerCause -> TreasuryCause.SERVER
            else -> throw IllegalArgumentException("${polyObj.javaClass.simpleName} type is not expected")
        }
    }

    fun convertTransactionCauseFromTreasury(
        treasuryObj: TreasuryCause<*>,
    ): PolyCause {
        return when {
            treasuryObj is TreasuryCause.Player -> PlayerCause(treasuryObj.identifier())
            treasuryObj is TreasuryCause.NonPlayer -> NonPlayerCause(convertNamespacedKeyFromTreasury(treasuryObj.identifier()))
            treasuryObj is TreasuryCause.Plugin -> PluginCause(convertNamespacedKeyFromTreasury(treasuryObj.identifier()))
            treasuryObj.identifier() == "Server" -> ServerCause
            else -> NonPlayerCause(PolyNamespacedKey("treasury", treasuryObj.identifier().toString()))
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
        polyObj: PolyAccountPermission,
    ): TreasuryAccountPermission {
        return when (polyObj) {
            PolyAccountPermission.MODIFY_PERMISSIONS ->
                TreasuryAccountPermission.MODIFY_PERMISSIONS

            PolyAccountPermission.DEPOSIT ->
                TreasuryAccountPermission.DEPOSIT

            PolyAccountPermission.WITHDRAW ->
                TreasuryAccountPermission.WITHDRAW

            PolyAccountPermission.BALANCE ->
                TreasuryAccountPermission.BALANCE
        }
    }

    fun convertAccountPermissionFromTreasury(
        treasuryObj: TreasuryAccountPermission,
    ): PolyAccountPermission {
        return when (treasuryObj) {
            TreasuryAccountPermission.MODIFY_PERMISSIONS ->
                PolyAccountPermission.MODIFY_PERMISSIONS

            TreasuryAccountPermission.DEPOSIT ->
                PolyAccountPermission.DEPOSIT

            TreasuryAccountPermission.WITHDRAW ->
                PolyAccountPermission.WITHDRAW

            TreasuryAccountPermission.BALANCE ->
                PolyAccountPermission.BALANCE
        }
    }

}














