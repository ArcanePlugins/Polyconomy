package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

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
import org.bukkit.Bukkit
import java.util.*
import java.util.function.Supplier

@Suppress("UNUSED_VARIABLE")
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
                        plugin.translations.commandGenericNotYetImplemented.sendTo(sender)
                        throw plugin.translations.commandApiFailure()
                    }),
                CommandAPICommand("symbol")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(
                        CustomArguments.currencyArgument(plugin, "currency"),
                        TextArgument("newValue"))
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as String
                        plugin.translations.commandGenericNotYetImplemented.sendTo(sender)
                        throw plugin.translations.commandApiFailure()
                    }),
                CommandAPICommand("amountFormat")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(CustomArguments.currencyArgument(plugin, "currency"),
                        TextArgument("newValue"))
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as String
                        plugin.translations.commandGenericNotYetImplemented.sendTo(sender)
                        throw plugin.translations.commandApiFailure()
                    }),
                CommandAPICommand("presentationFormat")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(TextArgument("newValue"))
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as String
                        plugin.translations.commandGenericNotYetImplemented.sendTo(sender)
                        throw plugin.translations.commandApiFailure()
                    }),
                CommandAPICommand("conversionRate")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(CustomArguments.currencyArgument(plugin, "currency"),
                        DoubleArgument("newValue"))
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as Double
                        plugin.translations.commandGenericNotYetImplemented.sendTo(sender)
                        throw plugin.translations.commandApiFailure()
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
                                plugin.translations.commandGenericNotYetImplemented.sendTo(sender)
                                throw plugin.translations.commandApiFailure()
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
                                plugin.translations.commandGenericNotYetImplemented.sendTo(sender)
                                throw plugin.translations.commandApiFailure()
                            }),
                        CommandAPICommand("unregister")
                            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                            .withArguments(
                                CustomArguments.currencyArgument(plugin, "currency"),
                                CustomArguments.localeArgument(plugin, "locale"),
                            )
                            .executes(CommandExecutor { sender, args ->
                                val locale = args.get("locale") as Locale
                                plugin.translations.commandGenericNotYetImplemented.sendTo(sender)
                                throw plugin.translations.commandApiFailure()
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

                val currencyAlreadyExists = runBlocking {
                    plugin.storageManager.handler.hasCurrency(name)
                }

                if (currencyAlreadyExists) {
                    plugin.translations.commandPolyconomyCurrencyRegisterErrorAlreadyExists.sendTo(sender, mapOf(
                        "currency" to Supplier { name }
                    ))
                    throw plugin.translations.commandApiFailure()
                }

                plugin.translations.commandPolyconomyCurrencyRegisterStarted.sendTo(sender, placeholders = mapOf(
                    "currency" to Supplier { name },
                ))

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

                    plugin.translations.commandPolyconomyCurrencyRegisterSuccess.sendTo(sender, placeholders = mapOf(
                        "currency" to Supplier { name }
                    ))
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

                plugin.translations.commandPolyconomyCurrencyUnregisterStarted.sendTo(sender, mapOf(
                    "currency" to Supplier { currency.name }
                ))

                runBlocking {
                    if (currency.isPrimary()) {
                        plugin.translations.commandPolyconomyCurrencyUnregisterErrorIsPrimary.sendTo(sender, mapOf(
                            "currency" to Supplier { currency.name }
                        ))
                        throw plugin.translations.commandApiFailure()
                    }
                }

                Bukkit.getServer().scheduler.runTaskAsynchronously(plugin) { ->
                    runBlocking {
                        plugin.storageManager.handler.unregisterCurrency(currency)

                        plugin.translations.commandPolyconomyCurrencyUnregisterComplete.sendTo(sender, mapOf(
                            "currency" to Supplier { currency.name }
                        ))
                    }
                }
            })
    }

}