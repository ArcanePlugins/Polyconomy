package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.balance

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.misc.args.CustomArguments
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import kotlinx.coroutines.runBlocking
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.function.Supplier

object BalanceCommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("balance")
            .withPermission(PolyconomyPerm.COMMAND_BALANCE.toString())
            .withAliases("bal")
            .withOptionalArguments(
                OfflinePlayerArgument("player"),
                CustomArguments.currencyArgument(plugin, "currency")
            )
            .executes(CommandExecutor { sender, args ->
                val targetPlayer = args.getOptional("player").orElseGet {
                    if (sender is Player) {
                        sender
                    } else {
                        plugin.translations.commandBalanceErrorNoPlayer.sendTo(sender)
                        throw plugin.translations.commandApiFailure()
                    }
                } as OfflinePlayer

                if (!targetPlayer.hasPlayedBefore()) {
                    plugin.translations.commandGenericErrorNotPlayedBefore.sendTo(sender)
                    throw plugin.translations.commandApiFailure()
                }

                val currency: Currency = args.getOptional("currency").orElseGet {
                    runBlocking {
                        plugin.storageManager.handler.getPrimaryCurrency()
                    }
                } as Currency

                val balance = runBlocking {
                    plugin.storageManager.handler.getOrCreatePlayerAccount(
                        targetPlayer.uniqueId,
                        targetPlayer.name,
                    ).getBalance(currency)
                }

                val balanceFmt = runBlocking {
                    currency.format(balance, plugin.settingsCfg.defaultLocale())
                }

                plugin.translations.commandBalanceView.sendTo(
                    sender, placeholders = mapOf(
                        "target-name" to Supplier { targetPlayer.name ?: targetPlayer.uniqueId.toString() },
                        "balance" to Supplier { balanceFmt },
                        "currency" to Supplier { currency.name },
                    )
                )
            })
    }

}