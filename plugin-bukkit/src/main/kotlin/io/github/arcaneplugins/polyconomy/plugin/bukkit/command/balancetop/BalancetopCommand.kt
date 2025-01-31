package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.balancetop

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.misc.args.CustomArguments
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.function.Supplier

object BalancetopCommand : InternalCmd {

    const val PAGE_SIZE = 10 // players per page
    private val activeRequests: MutableSet<String> = Collections.synchronizedSet(HashSet())

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("balancetop")
            .withAliases("baltop")
            .withPermission(PolyconomyPerm.COMMAND_BALANCETOP.toString())
            .withOptionalArguments(
                IntegerArgument("page"),
                CustomArguments.currencyArgument(plugin, "currency")
            )
            .executes(CommandExecutor { sender, args ->
                val page = args.getOptional("page").orElse(1) as Int

                if (page < 1) {
                    plugin.translations.commandBalancetopErrorPageTooLow.sendTo(sender, placeholders = mapOf(
                        "%page%" to Supplier { page.toString()}
                    ))
                    throw plugin.translations.commandApiFailure()
                }

                val currency = args.getOptional("currency").orElseGet {
                    runBlocking { plugin.storageManager.handler.getPrimaryCurrency() }
                } as Currency

                plugin.server.scheduler.runTaskAsynchronously(plugin) { _ ->
                    synchronized(activeRequests) {
                        if (activeRequests.contains(sender.name)) {
                            plugin.translations.commandBalancetopErrorAlreadySearching.sendTo(sender)
                            throw plugin.translations.commandApiFailure()
                        }

                        activeRequests.add(sender.name)
                    }

                    plugin.translations.commandBalancetopProcessingRequqest.sendTo(sender)

                    val baltop = runBlocking {
                        plugin.storageManager.handler.baltop(page, PAGE_SIZE, currency)
                    }

                    synchronized(activeRequests) {
                        activeRequests.remove(sender.name)
                    }

                    val locale = plugin.settingsCfg.defaultLocale()
                    val currencyName = runBlocking {
                        currency.getDisplayName(true, locale)
                    }

                    plugin.translations.commandBalancetopHeader.sendTo(sender, placeholders = mapOf(
                        "page" to Supplier { page.toString() },
                        "currency" to Supplier { currencyName }
                    ))

                    if (baltop.isEmpty()) {
                        plugin.translations.commandBalancetopNoEntriesOnPage.sendTo(sender)
                    } else {
                        baltop.onEachIndexed { index, (username, balance) ->
                            plugin.translations.commandBalancetopEntry.sendTo(sender, placeholders = mapOf(
                                "rank" to Supplier { (((page - 1) * PAGE_SIZE) + index + 1).toString() },
                                "target-name" to Supplier { username },
                                "balance" to Supplier { runBlocking { currency.format(balance, locale) } }
                            ))
                        }
                    }
                }
            })
    }
}