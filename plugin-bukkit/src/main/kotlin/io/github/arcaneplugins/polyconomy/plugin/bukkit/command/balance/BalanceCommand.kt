package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.balance

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.misc.args.CustomArguments
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import kotlin.jvm.optionals.getOrNull

object BalanceCommand: InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("balance")
            .withPermission(PolyPermission.COMMAND_BALANCE.toString())
            .withOptionalArguments(OfflinePlayerArgument("player"), CustomArguments.currencyArgument(plugin, "currency"))
            .executes(CommandExecutor { sender, args ->
                //todo translatability

                val targetPlayer = args.getOptional("player").getOrNull() as OfflinePlayer?
                    ?: if (sender is Player) {
                        sender
                    } else {
                        throw CommandAPI.failWithString("Enter the username of the player you wish to check.")
                    }

                val currency = args.getOptional("currency").getOrNull() as Currency?
                    ?: runBlocking {
                        plugin.storageManager.handler.getPrimaryCurrency()
                    }

                val balance = runBlocking {
                    plugin.storageManager.handler.getOrCreatePlayerAccount(
                        targetPlayer.uniqueId,
                        targetPlayer.name,
                    ).getBalance(currency)
                }

                val balanceFmt = runBlocking {
                    currency.format(balance, Locale.getDefault())
                }

                sender.spigot().sendMessage(ComponentBuilder(
                    "Player '${targetPlayer.name}' has '${balanceFmt}' (currency: '${currency.name}')."
                ).color(ChatColor.GREEN).build())
            })
    }

}