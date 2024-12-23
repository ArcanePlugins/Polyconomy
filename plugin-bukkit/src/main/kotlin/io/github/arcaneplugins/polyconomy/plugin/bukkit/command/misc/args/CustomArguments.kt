package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.misc.args

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException
import dev.jorel.commandapi.arguments.StringArgument
import io.github.arcaneplugins.polyconomy.api.currency.Currency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import kotlinx.coroutines.runBlocking

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

}
