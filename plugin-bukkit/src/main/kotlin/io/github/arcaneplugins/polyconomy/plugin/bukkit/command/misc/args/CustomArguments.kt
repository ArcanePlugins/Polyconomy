package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.misc.args

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException
import dev.jorel.commandapi.arguments.StringArgument
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.stream.Collectors

object CustomArguments {

    fun currencyArgument(
        plugin: Polyconomy,
        nodeName: String,
    ): Argument<Currency> {
        return CustomArgument(StringArgument(nodeName)) { info ->
            val curr = runBlocking {
                plugin.storageManager.handler.getCurrency(info.input)
            }

            return@CustomArgument curr ?: throw CustomArgumentException.fromMessageBuilder(
                CustomArgument.MessageBuilder("Unknown currency: ").appendArgInput()
            )
        }.replaceSuggestions(ArgumentSuggestions.strings {
            runBlocking {
                plugin.storageManager.handler.getCurrencies().map { it.name }.toTypedArray()
            }
        })
    }

    fun localeArgument(
        nodeName: String,
    ): Argument<Locale> {
        return CustomArgument(StringArgument(nodeName)) { info ->
            return@CustomArgument try {
                Locale.Builder().setLanguageTag(info.input).build()
            } catch (ex: IllformedLocaleException) {
                throw CustomArgumentException.fromMessageBuilder(
                    CustomArgument.MessageBuilder("Illformed locale: ").appendArgInput()
                )
            }
        }.replaceSuggestions(ArgumentSuggestions.strings {
            return@strings Locale
                .availableLocales()
                .map { it.toLanguageTag() }
                .collect(Collectors.toList())
                .toTypedArray()
        })
    }

    fun identityStringArgument(
        nodeName: String,
    ): Argument<String> {
        return CustomArgument(StringArgument(nodeName)) { info ->
            val valid = info.input.all {
                it in arrayOf('_', '-')
                !it.isWhitespace() &&
                        it.isLowerCase() &&
                        !it.isDigit()
            }

            if (valid) {
                return@CustomArgument info.input
            } else {
                throw CustomArgumentException.fromMessageBuilder(
                    CustomArgument.MessageBuilder("Illformed identifier: ").appendArgInput()
                )
            }
        }
    }

}
