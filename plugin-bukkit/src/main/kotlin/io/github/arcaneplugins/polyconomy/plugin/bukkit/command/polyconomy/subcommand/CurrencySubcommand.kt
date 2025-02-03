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
import org.bukkit.ChatColor
import org.bukkit.ChatColor.RED
import java.util.*
import java.util.function.Supplier

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
                CommandAPICommand("name")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(
                        CustomArguments.currencyArgument(plugin, "currency"),
                        CustomArguments.identityStringArgument(plugin, "new")
                    )
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val new = args.get("new") as String

                        //TODO Translatable messages
                        val operation = "${currency.name}.name -> ${new}"
                        sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")
                        if (runBlocking { currency.isPrimary() }) {
                            sender.sendMessage("${RED}Error: You can't rename a primary currency.")
                            throw plugin.translations.commandApiFailure()
                        }
                        runBlocking { currency.setName(new) }
                        sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
                    }),
                CommandAPICommand("startingBalance")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(
                        CustomArguments.currencyArgument(plugin, "currency"),
                        DoubleArgument("newValue")
                    )
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as Double

                        //TODO Translatable messages
                        val operation = "${currency.name}.startingBalance -> ${newValue}"
                        sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                            runBlocking {
                                currency.setStartingBalance(newValue.toBigDecimal())
                                sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
                                sender.sendMessage("${ChatColor.LIGHT_PURPLE}WARNING: The new starting balance will only affect new economy accounts, not existing accounts, to avoid unwanted mutation to your database. You can reset balances for individual players using `/eco reset`.")
                            }
                        })
                    }),
                CommandAPICommand("symbol")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(
                        CustomArguments.currencyArgument(plugin, "currency"),
                        TextArgument("newValue")
                    )
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as String

                        //TODO Translatable messages
                        val operation = "${currency.name}.symbol -> ${newValue}"
                        sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                            runBlocking {
                                currency.setSymbol(newValue)
                                sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
                            }
                        })
                    }),
                CommandAPICommand("amountFormat")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(
                        CustomArguments.currencyArgument(plugin, "currency"),
                        TextArgument("newValue")
                    )
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as String

                        //TODO Translatable messages
                        val operation = "${currency.name}.amountFormat -> ${newValue}"
                        sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                            runBlocking {
                                currency.setAmountFormat(newValue)
                                sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
                            }
                        })
                    }),
                CommandAPICommand("presentationFormat")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(TextArgument("newValue"))
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as String

                        //TODO Translatable messages
                        val operation = "${currency.name}.presentationFormat -> ${newValue}"
                        sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                            runBlocking {
                                currency.setPresentationFormat(newValue)
                                sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
                            }
                        })
                    }),
                CommandAPICommand("conversionRate")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withArguments(
                        CustomArguments.currencyArgument(plugin, "currency"),
                        DoubleArgument("newValue")
                    )
                    .executes(CommandExecutor { sender, args ->
                        val currency = args.get("currency") as Currency
                        val newValue = args.get("newValue") as Double

                        //TODO Translatable messages
                        val operation = "${currency.name}.conversionRate -> ${newValue}"
                        sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                            runBlocking {
                                currency.setConversionRate(newValue.toBigDecimal())
                                sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
                            }
                        })
                    }),
                CommandAPICommand("locale")
                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                    .withSubcommands(
                        CommandAPICommand("register")
                            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                            .withArguments(
                                CustomArguments.currencyArgument(plugin, "currency"),
                                CustomArguments.localeArgument(plugin, "locale"),
                                TextArgument("nameSingular"),
                                TextArgument("namePlural"),
                                TextArgument("decimal"),
                            )
                            .executes(CommandExecutor { sender, args ->
                                val currency = args.get("currency") as Currency
                                val locale = args.get("locale") as Locale
                                val dispNameSingular = args.get("nameSingular") as String
                                val dispNamePlural = args.get("namePlural") as String
                                val decimal = args.get("decimal") as String

                                //TODO Translatable messages
                                val operation = "${currency.name}.locale.${locale}.register"
                                sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")
                                runBlocking {
                                    if (currency.hasLocale(locale)) {
                                        sender.sendMessage("$RED${operation}: Error: This currency already has this locale defined - instead of attempting to register a new locale, you should use the `set` subcommand to adjust values on the existing currency locale data.")
                                        throw plugin.translations.commandApiFailure()
                                    }
                                    currency.registerLocale(
                                        locale = locale,
                                        dispNameSingular = dispNameSingular,
                                        dispNamePlural = dispNamePlural,
                                        decimal = decimal
                                    )
                                }
                                sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
                            }),
                        CommandAPICommand("set")
                            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                            .withSubcommands(
                                CommandAPICommand("nameSingular")
                                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                                    .withArguments(
                                        CustomArguments.currencyArgument(plugin, "currency"),
                                        CustomArguments.localeArgument(plugin, "locale"),
                                        TextArgument("newValue"),
                                    )
                                    .executes(CommandExecutor { sender, args ->
                                        val currency = args.get("currency") as Currency
                                        val locale = args.get("locale") as Locale
                                        val new = args.get("newValue") as String

                                        //TODO Translatable messages
                                        val operation = "${currency.name}.locale.${locale}.nameSingular -> ${new}"
                                        sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")
                                        runBlocking {
                                            currency.setDisplayName(
                                                plural = false,
                                                locale = locale,
                                                new = new
                                            )
                                        }
                                        sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
                                    }),
                                CommandAPICommand("namePlural")
                                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                                    .withArguments(
                                        CustomArguments.currencyArgument(plugin, "currency"),
                                        CustomArguments.localeArgument(plugin, "locale"),
                                        TextArgument("newValue"),
                                    )
                                    .executes(CommandExecutor { sender, args ->
                                        val currency = args.get("currency") as Currency
                                        val locale = args.get("locale") as Locale
                                        val new = args.get("newValue") as String

                                        //TODO Translatable messages
                                        val operation = "${currency.name}.locale.${locale}.namePlural -> ${new}"
                                        sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")
                                        runBlocking {
                                            currency.setDisplayName(
                                                plural = true,
                                                locale = locale,
                                                new = new
                                            )
                                        }
                                        sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
                                    }),
                                CommandAPICommand("decimal")
                                    .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                                    .withArguments(
                                        CustomArguments.currencyArgument(plugin, "currency"),
                                        CustomArguments.localeArgument(plugin, "locale"),
                                        TextArgument("newValue"),
                                    )
                                    .executes(CommandExecutor { sender, args ->
                                        val currency = args.get("currency") as Currency
                                        val locale = args.get("locale") as Locale
                                        val new = args.get("newValue") as String

                                        //TODO Translatable messages
                                        val operation = "${currency.name}.locale.${locale}.decimal -> ${new}"
                                        sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")
                                        runBlocking {
                                            currency.setDecimal(
                                                locale = locale,
                                                new = new
                                            )
                                        }
                                        sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
                                    }),
                            ),
                        CommandAPICommand("unregister")
                            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_CURRENCY_SET.toString())
                            .withArguments(
                                CustomArguments.currencyArgument(plugin, "currency"),
                                CustomArguments.localeArgument(plugin, "locale"),
                            )
                            .executes(CommandExecutor { sender, args ->
                                val currency = args.get("currency") as Currency
                                val locale = args.get("locale") as Locale

                                //TODO Translatable messages
                                val operation = "${currency.name}.locale.${locale}.unregister"
                                sender.sendMessage("${ChatColor.YELLOW}${operation}: Processing...")

                                // if they are trying to unregister the last locale left, stop
                                if (runBlocking { currency.getLocaleDecimalMap().size < 2 }) {
                                    // todo translatable message
                                    sender.sendMessage("${RED}${operation} Error: There must be 2 or more locales in this currency so the currency has another locale to fallback one. Try creating the new locale first with `register`, or adjust the values of this existing one with `set`.")
                                    throw plugin.translations.commandApiFailure()
                                }

                                // Unregister the locale
                                runBlocking { currency.unregisterLocale(locale) }

                                // todo translatable message
                                sender.sendMessage("${ChatColor.GREEN}${operation}: Ok.")
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
                val presentationFormat: String =
                    args.getOptional("presentationFormat").orElse(Currency.DEFAULT_PRESENTATION_FORMAT) as String
                val amountFormat: String =
                    args.getOptional("amountFormat").orElse(Currency.DEFAULT_AMOUNT_FORMAT) as String

                val currencyAlreadyExists = runBlocking {
                    plugin.storageManager.handler.hasCurrency(name)
                }

                if (currencyAlreadyExists) {
                    plugin.translations.commandPolyconomyCurrencyRegisterErrorAlreadyExists.sendTo(
                        sender, mapOf(
                        "currency" to Supplier { name }
                    ))
                    throw plugin.translations.commandApiFailure()
                }

                plugin.translations.commandPolyconomyCurrencyRegisterStarted.sendTo(
                    sender, placeholders = mapOf(
                        "currency" to Supplier { name },
                    )
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

                    plugin.translations.commandPolyconomyCurrencyRegisterSuccess.sendTo(
                        sender, placeholders = mapOf(
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

                plugin.translations.commandPolyconomyCurrencyUnregisterStarted.sendTo(
                    sender, mapOf(
                    "currency" to Supplier { currency.name }
                ))

                runBlocking {
                    if (currency.isPrimary()) {
                        plugin.translations.commandPolyconomyCurrencyUnregisterErrorIsPrimary.sendTo(
                            sender, mapOf(
                            "currency" to Supplier { currency.name }
                        ))
                        throw plugin.translations.commandApiFailure()
                    }
                }

                Bukkit.getServer().scheduler.runTaskAsynchronously(plugin) { ->
                    runBlocking {
                        plugin.storageManager.handler.unregisterCurrency(currency)

                        plugin.translations.commandPolyconomyCurrencyUnregisterCompleted.sendTo(
                            sender, mapOf(
                            "currency" to Supplier { currency.name }
                        ))
                    }
                }
            })
    }

}