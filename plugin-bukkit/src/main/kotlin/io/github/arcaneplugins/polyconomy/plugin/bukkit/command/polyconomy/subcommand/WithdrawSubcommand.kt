package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

import dev.jorel.commandapi.CommandAPI
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
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.OfflinePlayer
import java.util.*
import kotlin.jvm.optionals.getOrNull

object WithdrawSubcommand : InternalCmd {
    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("withdraw")
            .withAliases("take", "remove")
            .withArguments(
                OfflinePlayerArgument("player"),
                DoubleArgument("amount"),
            )
            .withOptionalArguments(
                CustomArguments.currencyArgument(plugin, "currency")
            )
            .withPermission(PolyPermission.COMMAND_POLYCONOMY_WITHDRAW.toString())
            .executes(CommandExecutor { sender, args ->
                // TODO Translatable Messages

                val targetPlayer = args.get("player") as OfflinePlayer
                val amount = args.get("amount") as Double

                if (amount <= 0) {
                    throw CommandAPI.failWithString("Amount must be greater than zero.")
                }

                val targetAccount = runBlocking {
                    plugin.storageManager.handler
                        .getOrCreatePlayerAccount(
                            uuid = targetPlayer.uniqueId,
                            name = targetPlayer.name
                        )
                }

                val currency = runBlocking {
                    args.getOptional("currency").getOrNull() as Currency?
                        ?: plugin.storageManager.handler.getPrimaryCurrency()
                }

                val amountBd = amount.toBigDecimal()

                val amountFormatted = runBlocking {
                    currency.format(amountBd, Locale.getDefault())
                }

                val targetName = targetPlayer.name ?: targetPlayer.uniqueId.toString()

                val canAfford = runBlocking {
                    targetAccount.has(amountBd, currency)
                }

                if (!canAfford) {
                    throw CommandAPI.failWithString("Player ${targetName} can't afford ${amount} to withdraw (currency: '${currency.name}').")
                }

                runBlocking {
                    targetAccount.withdraw(
                        amount = amount.toBigDecimal(),
                        cause = ServerCause,
                        currency = currency,
                        importance = TransactionImportance.MEDIUM,
                        reason = null
                    )
                }

                sender.spigot().sendMessage(
                    ComponentBuilder("Withdrawn ${amountFormatted} from ${targetName}'s account (currency: '${currency.name}').")
                        .color(ChatColor.GREEN)
                        .build()
                )
            })
    }
}