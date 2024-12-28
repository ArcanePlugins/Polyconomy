package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.balancetop

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.misc.args.CustomArguments
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import java.util.*
import kotlin.jvm.optionals.getOrNull

object BalancetopCommand : InternalCmd {

    const val PAGE_SIZE = 10 // players per page
    private val activeRequests: MutableSet<String> = Collections.synchronizedSet(HashSet())

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("balancetop")
            .withAliases("baltop")
            .withPermission(PolyPermission.COMMAND_BALANCETOP.toString())
            .withOptionalArguments(
                IntegerArgument("page"),
                CustomArguments.currencyArgument(plugin, "currency")
            )
            .executes(CommandExecutor { sender, args ->
                val page = args.getOptional("page").getOrNull() as Int? ?: 1
                val currency = args.getOptional("currency").getOrNull() as Currency?
                    ?: runBlocking { plugin.storageManager.handler.getPrimaryCurrency() }

                plugin.server.scheduler.runTaskAsynchronously(plugin) { _ ->
                    synchronized(activeRequests) {
                        if (activeRequests.contains(sender.name)) {
                            throw CommandAPI.failWithString("You already have an active baltop search, please wait for it to complete.")
                        }

                        activeRequests.add(sender.name)
                    }

                    sender.spigot().sendMessage(
                        ComponentBuilder("Processing request...")
                            .color(ChatColor.GREEN)
                            .build()
                    )

                    val baltop = runBlocking {
                        plugin.storageManager.handler.baltop(page, PAGE_SIZE, currency)
                    }

                    synchronized(activeRequests) {
                        activeRequests.remove(sender.name)
                    }

                    val locale = plugin.settings.defaultLocale()
                    val currencyName = runBlocking {
                        currency.getDisplayName(true, locale)
                    }
                    val compBldr = ComponentBuilder("******* Balance Top (Page ${page} - ${currencyName}) *******")
                        .color(ChatColor.GREEN)
                        .underlined(true)
                        .append("\n")
                        .underlined(false)

                    if (baltop.isEmpty()) {
                        compBldr
                            .append("\nNo results to display on this page.")
                            .color(ChatColor.GRAY)
                            .italic(true)
                    } else {
                        baltop.forEach { (username, balance) ->
                            compBldr.append("\n \u2022 ")
                                .color(ChatColor.DARK_GRAY)
                                .append(username)
                                .color(ChatColor.WHITE)
                                .append(": ${runBlocking { currency.format(balance, locale) }}")
                                .color(ChatColor.GRAY)
                        }
                    }

                    sender.spigot().sendMessage(compBldr.build())
                }
            })
    }
}