package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.pay

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.DoubleArgument
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.cause.PlayerCause
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.misc.args.CustomArguments
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import kotlinx.coroutines.runBlocking
import org.bukkit.OfflinePlayer
import java.util.function.Supplier

object PayCommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("pay")
            .withPermission(PolyconomyPerm.COMMAND_PAY.toString())
            .withArguments(
                OfflinePlayerArgument("player"),
                DoubleArgument("amount"),
            )
            .withOptionalArguments(
                CustomArguments.currencyArgument(plugin, "currency"),
            )
            .executesPlayer(PlayerCommandExecutor { sender, args ->
                val targetPlayer = args.get("player") as OfflinePlayer

                if (!targetPlayer.hasPlayedBefore()) {
                    plugin.translations.commandGenericErrorNotPlayedBefore.sendTo(sender)
                    throw plugin.translations.commandApiFailure()
                }

                if (sender.uniqueId == targetPlayer.uniqueId) {
                    plugin.translations.commandPayErrorNotYourself.sendTo(sender)
                    throw plugin.translations.commandApiFailure()
                }

                val amount = args.get("amount") as Double
                val amountBd = amount.toBigDecimal()

                if (amount <= 0) {
                    plugin.translations.commandGenericAmountZeroOrLess.sendTo(sender, placeholders = mapOf(
                        "amount" to Supplier { amount.toString() }
                    ))
                    throw plugin.translations.commandApiFailure()
                }

                val currency = args.getOptional("currency").orElseGet {
                    runBlocking {
                        plugin.storageManager.handler.getPrimaryCurrency()
                    }
                } as Currency

                val senderAccount = runBlocking {
                    plugin.storageManager.handler.getOrCreatePlayerAccount(
                        sender.uniqueId,
                        sender.name
                    )
                }

                val canAfford = runBlocking {
                    senderAccount.has(amountBd, currency)
                }

                if (!canAfford) {
                    plugin.translations.commandPayErrorCantAfford.sendTo(sender, placeholders = mapOf(
                        "amount" to Supplier { amount.toString() },
                        "balance" to Supplier { runBlocking { senderAccount.getBalance(currency).toString() } },
                        "currency" to Supplier { currency.name }
                    ))
                    throw plugin.translations.commandApiFailure()
                }

                val targetAccount = runBlocking {
                    plugin.storageManager.handler.getOrCreatePlayerAccount(
                        targetPlayer.uniqueId,
                        targetPlayer.name
                    )
                }

                runBlocking {
                    // take money out of sender's account
                    senderAccount.withdraw(
                        amount = amountBd,
                        currency = currency,
                        cause = PlayerCause(senderAccount.uuid),
                        reason = "Payment to ${targetPlayer.uniqueId}",
                        importance = TransactionImportance.MEDIUM,
                    )

                    // take money out of target's account
                    targetAccount.deposit(
                        amount = amountBd,
                        currency = currency,
                        cause = PlayerCause(senderAccount.uuid),
                        reason = "Payment from ${senderAccount.uuid}",
                        importance = TransactionImportance.MEDIUM,
                    )
                }

                val amountFmt = runBlocking {
                    currency.format(amount.toBigDecimal(), plugin.settingsCfg.defaultLocale())
                }

                val newBalance = runBlocking {
                    currency.format(senderAccount.getBalance(currency), plugin.settingsCfg.defaultLocale())
                }

                plugin.translations.commandPaySuccess.sendTo(sender, placeholders = mapOf(
                    "amount" to Supplier { amountFmt },
                    "balance" to Supplier { newBalance },
                    "currency" to Supplier { currency.name },
                    "target-name" to Supplier { targetPlayer.name ?: targetPlayer.uniqueId.toString() },
                    "target-balance" to Supplier { runBlocking { targetAccount.getBalance(currency).toString() } },
                ))
            })
    }

}