package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.DoubleArgument
import dev.jorel.commandapi.arguments.TextArgument
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.misc.args.CustomArguments
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import java.util.*

@Suppress("UNUSED_ANONYMOUS_PARAMETER", "UNUSED_VARIABLE")
object CurrencySubcommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("currency")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY.toString())
            .withSubcommands(
                buildRegisterSubcommand(plugin),
                buildSetSubcommand(plugin),
                buildUnregisterSubcommand(plugin),
            )
    }

    private fun buildSetSubcommand(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("set")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
            .withSubcommands(
                CommandAPICommand("startingBalance")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(
                        CustomArguments.currencyArgument(plugin, "currency"),
                        DoubleArgument("newValue"))
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as Double
                        throw CommandAPI.failWithString("Not yet implemented!")
                    }),
                CommandAPICommand("symbol")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(
                        CustomArguments.currencyArgument(plugin, "currency"),
                        TextArgument("newValue"))
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as String
                        throw CommandAPI.failWithString("Not yet implemented!")
                    }),
                CommandAPICommand("amountFormat")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(CustomArguments.currencyArgument(plugin, "currency"),
                        TextArgument("newValue"))
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as String
                        throw CommandAPI.failWithString("Not yet implemented!")
                    }),
                CommandAPICommand("presentationFormat")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(TextArgument("newValue"))
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as String
                        throw CommandAPI.failWithString("Not yet implemented!")
                    }),
                CommandAPICommand("conversionRate")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(CustomArguments.currencyArgument(plugin, "currency"),
                        DoubleArgument("newValue"))
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as Double
                        throw CommandAPI.failWithString("Not yet implemented!")
                    }),
                CommandAPICommand("locale")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withSubcommands(
                        CommandAPICommand("register")
                            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                            .withArguments(
                                CustomArguments.currencyArgument(plugin, "currency"),
                                CustomArguments.localeArgument(plugin, "locale"),
                                TextArgument("dispNameSingular"),
                                TextArgument("dispNamePlural"),
                                TextArgument("dispDecimal"),
                            )
                            .executes(CommandExecutor { sender, args ->
                                val locale = args.get("locale") as Locale
                                throw CommandAPI.failWithString("Not yet implemented!")
                            }),
                        CommandAPICommand("set")
                            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                            .withArguments(
                                CustomArguments.currencyArgument(plugin, "currency"),
                                CustomArguments.localeArgument(plugin, "locale"),
                                TextArgument("dispNameSingular"),
                                TextArgument("dispNamePlural"),
                                TextArgument("dispDecimal"),
                            )
                            .executes(CommandExecutor { sender, args ->
                                val locale = args.get("locale") as Locale
                                throw CommandAPI.failWithString("Not yet implemented!")
                            }),
                        CommandAPICommand("unregister")
                            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                            .withArguments(
                                CustomArguments.currencyArgument(plugin, "currency"),
                                CustomArguments.localeArgument(plugin, "locale"),
                            )
                            .executes(CommandExecutor { sender, args ->
                                val locale = args.get("locale") as Locale
                                throw CommandAPI.failWithString("Not yet implemented!")
                            })
                    )
            )
    }

    private fun buildRegisterSubcommand(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("register")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_REGISTER.toString())
            .withArguments(
                CustomArguments.identityStringArgument(plugin, "name"),
                DoubleArgument("startingBalance"),
                TextArgument("symbol"),
                DoubleArgument("conversionRate"),
                CustomArguments.localeArgument(plugin, "dispLocale"),
                TextArgument("dispNameSingular"),
                TextArgument("dispNamePlural"),
                TextArgument("dispDecimal"),
            )
            .withOptionalArguments(
                TextArgument("presentationFormat"),
                TextArgument("amountFormat"),
            )
            .executes(CommandExecutor { sender, args ->
                val name = args.get("name") as String
                val startingBalance = args.get("startingBalance") as Double
                val symbol = args.get("symbol") as String
                val conversionRate = args.get("conversionRate") as Double
                val dispLocale = args.get("dispLocale") as Locale
                val dispNameSingular = args.get("dispNameSingular") as String
                val dispNamePlural = args.get("dispNamePlural") as String
                val dispDecimal = args.get("dispDecimal") as String
                val presentationFormat: String = args.getOptional("presentationFormat").orElse(Currency.DEFAULT_PRESENTATION_FORMAT) as String
                val amountFormat: String = args.getOptional("amountFormat").orElse(Currency.DEFAULT_AMOUNT_FORMAT) as String

                sender.spigot().sendMessage(
                    ComponentBuilder("Registering currency...")
                        .color(ChatColor.GREEN)
                        .build()
                )

                Bukkit.getServer().scheduler.runTaskAsynchronously(plugin) { ->
                    runBlocking {
                        plugin.storageManager.handler.registerCurrency(
                            name = name,
                            amountFormat = amountFormat,
                            conversionRate = conversionRate.toBigDecimal(),
                            decimalLocaleMap = mapOf(dispLocale to dispDecimal),
                            displayNamePluralLocaleMap = mapOf(dispLocale to dispNamePlural),
                            displayNameSingularLocaleMap = mapOf(dispLocale to dispNameSingular),
                            startingBalance = startingBalance.toBigDecimal(),
                            presentationFormat = presentationFormat,
                            symbol = symbol,
                        )
                    }

                    sender.spigot().sendMessage(
                        ComponentBuilder("Currency registered succesfully!")
                            .color(ChatColor.GREEN)
                            .build()
                    )
                }
            })
    }

    private fun buildUnregisterSubcommand(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("unregister")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_UNREGISTER.toString())
            .withArguments(
                CustomArguments.currencyArgument(plugin, "currency")
            )
            .executes(CommandExecutor { sender, args ->
                val currency = args.get("currency") as Currency

                sender.spigot().sendMessage(
                    ComponentBuilder("Unregistering currency '${currency.name}'...")
                        .color(ChatColor.GREEN)
                        .build()
                )

                Bukkit.getServer().scheduler.runTaskAsynchronously(plugin) { ->
                    runBlocking {
                        if (currency.isPrimary()) {
                            sender.spigot().sendMessage(
                                ComponentBuilder("Cannot unregister '${currency.name}' as it is a primary currency.")
                                    .color(ChatColor.RED)
                                    .build()
                            )
                            return@runBlocking
                        }

                        plugin.storageManager.handler.unregisterCurrency(currency)

                        sender.spigot().sendMessage(
                            ComponentBuilder("Unregistered successfully.")
                                .color(ChatColor.GREEN)
                                .build()
                        )
                    }
                }
            })
    }

}