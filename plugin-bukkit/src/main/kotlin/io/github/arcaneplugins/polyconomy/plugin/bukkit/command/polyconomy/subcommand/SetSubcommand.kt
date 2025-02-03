package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.DoubleArgument
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.api.account.TransactionImportance
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.api.util.cause.ServerCause
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.misc.args.CustomArguments
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import kotlinx.coroutines.runBlocking
import org.bukkit.OfflinePlayer
import java.util.function.Supplier

object SetSubcommand : InternalCmd {
    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("set")
            .withArguments(
                OfflinePlayerArgument("player"),
                DoubleArgument("amount"),
            )
            .withOptionalArguments(
                CustomArguments.currencyArgument(plugin, "currency")
            )
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_DEPOSIT.toString())
            .executes(CommandExecutor { sender, args ->
                val targetPlayer = args.get("player") as OfflinePlayer
                val amount = args.get("amount") as Double

                if (!targetPlayer.hasPlayedBefore()) {
                    plugin.translations.commandGenericErrorNotPlayedBefore.sendTo(sender)
                    throw plugin.translations.commandApiFailure()
                }

                if (amount <= 0) {
                    plugin.translations.commandGenericAmountZeroOrLess.sendTo(sender, placeholders = mapOf(
                        "amount" to Supplier { amount.toString() }
                    ))
                    throw plugin.translations.commandApiFailure()
                }

                val targetAccount = runBlocking {
                    plugin.storageManager.handler
                        .getOrCreatePlayerAccount(
                            uuid = targetPlayer.uniqueId,
                            name = targetPlayer.name
                        )
                }

                val currency = runBlocking {
                    args.getOptional("currency")
                        .orElse(plugin.storageManager.handler.getPrimaryCurrency()) as Currency
                }

                runBlocking {
                    targetAccount.setBalance(
                        amount = amount.toBigDecimal(),
                        cause = ServerCause,
                        currency = currency,
                        importance = TransactionImportance.MEDIUM,
                        reason = null
                    )
                }

                val amountFormatted = runBlocking {
                    currency.format(amount.toBigDecimal(), plugin.settingsCfg.defaultLocale())
                }
                val targetName = targetPlayer.name ?: targetPlayer.uniqueId.toString()

                plugin.translations.commandPolyconomySetCompleted.sendTo(sender, placeholders = mapOf(
                    "target-name" to Supplier { targetName },
                    "amount" to Supplier { amountFormatted },
                    "currency" to Supplier { currency.name },
                ))
            })
    }
}