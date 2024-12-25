package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy

import dev.jorel.commandapi.CommandAPICommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.BackupSubcommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.DepositSubcommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.ReloadSubcommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.ResetSubcommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.SetSubcommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.SubroutineSubcommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.VersionSubcommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand.WithdrawSubcommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyPermission

object PolyconomyCommand: InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("polyconomy")
            .withAliases("economy", "eco")
            .withPermission(PolyPermission.COMMAND_POLYCONOMY.toString())
            .withSubcommands(
                *listOf(
                    BackupSubcommand,
                    DepositSubcommand,
                    ReloadSubcommand,
                    ResetSubcommand,
                    SetSubcommand,
                    SubroutineSubcommand,
                    VersionSubcommand,
                    WithdrawSubcommand,
                ).map {
                    it.build(plugin)
                }.toTypedArray()
            )
    }

}