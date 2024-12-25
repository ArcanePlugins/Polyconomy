package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.DoubleArgument
import dev.jorel.commandapi.arguments.OfflinePlayerArgument
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.misc.args.CustomArguments
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission

object DepositSubcommand : InternalCmd {
    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("deposit")
            .withAliases("give", "add")
            .withArguments(
                OfflinePlayerArgument("player"),
                DoubleArgument("amount"),
            )
            .withOptionalArguments(
                CustomArguments.currencyArgument(plugin, "currency")
            )
            .withPermission(PolyPermission.COMMAND_POLYCONOMY_DEPOSIT.toString())
            .executes(CommandExecutor { _, _ ->
                throw CommandAPI.failWithString("Not yet implemented!")
            })
    }
}