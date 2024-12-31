package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.pay

import dev.jorel.commandapi.CommandAPI
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
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.OfflinePlayer
import kotlin.jvm.optionals.getOrNull

object PayCommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("pay")
            .withPermission(PolyPermission.COMMAND_PAY.toString())
            .withArguments(
                OfflinePlayerArgument("player"),
                DoubleArgument("amount"),
            )
            .withOptionalArguments(
                CustomArguments.currencyArgument(plugin, "currency"),
            )
            .executesPlayer(PlayerCommandExecutor { sender, args ->
                //todo translatability

                val targetPlayer = args.get("player") as OfflinePlayer

                if (sender.uniqueId == targetPlayer.uniqueId) {
                    throw CommandAPI.failWithString("You can't pay yourself.")
                }

                val amount = args.get("amount") as Double
                val amountBd = amount.toBigDecimal()

                if (amount <= 0) {
                    throw CommandAPI.failWithString("Amount must be greater than zero.")
                }

                val currency = args.getOptional("currency").getOrNull() as Currency?
                    ?: runBlocking {
                        plugin.storageManager.handler.getPrimaryCurrency()
                    }

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
                    throw CommandAPI.failWithString("You can't afford that payment.")
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
                    currency.format(amount.toBigDecimal(), plugin.settings.defaultLocale())
                }

                val newBalance = runBlocking {
                    currency.format(senderAccount.getBalance(currency), plugin.settings.defaultLocale())
                }

                sender.spigot().sendMessage(
                    ComponentBuilder(
                        "Paid '${amountFmt}' to '${targetPlayer.name ?: ("UUID ${targetPlayer.uniqueId}")}' (currency: '${currency.name}'). Your new balance is '${newBalance}'."
                    ).color(ChatColor.GREEN).build()
                )
            })
    }

}