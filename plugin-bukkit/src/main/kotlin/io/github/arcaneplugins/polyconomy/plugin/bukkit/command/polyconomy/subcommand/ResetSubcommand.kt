package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

import dev.jorel.commandapi.CommandAPICommand
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
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.OfflinePlayer
import kotlin.jvm.optionals.getOrNull

object ResetSubcommand : InternalCmd {
    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("reset")
            .withArguments(
                OfflinePlayerArgument("player"),
            )
            .withOptionalArguments(
                CustomArguments.currencyArgument(plugin, "currency")
            )
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_DEPOSIT.toString())
            .executes(CommandExecutor { sender, args ->
                val targetPlayer = args.get("player") as OfflinePlayer

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

                runBlocking {
                    targetAccount.resetBalance(
                        cause = ServerCause,
                        currency = currency,
                        importance = TransactionImportance.MEDIUM,
                        reason = null
                    )
                }

                val targetName = targetPlayer.name ?: targetPlayer.uniqueId.toString()

                sender.spigot().sendMessage(
                    ComponentBuilder("Reset ${targetName}'s balance in currency '${currency.name}'.")
                        .color(ChatColor.GREEN)
                        .build()
                )
            })
    }
}