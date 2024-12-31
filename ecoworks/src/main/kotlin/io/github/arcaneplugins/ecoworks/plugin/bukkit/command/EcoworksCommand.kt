package io.github.arcaneplugins.ecoworks.plugin.bukkit.command

import dev.jorel.commandapi.CommandAPICommand
import io.github.arcaneplugins.ecoworks.plugin.bukkit.Ecoworks
import io.github.arcaneplugins.ecoworks.plugin.bukkit.command.ecoworks.TestSubcommand
import io.github.arcaneplugins.ecoworks.plugin.bukkit.command.ecoworks.VersionSubcommand
import io.github.arcaneplugins.ecoworks.plugin.bukkit.misc.EcoworksPerm

class EcoworksCommand(plugin: Ecoworks) {

    val cmd = CommandAPICommand("ecoworks")
        .withPermission(EcoworksPerm.COMMAND_ECOWORKS.toString())
        .withSubcommands(
            TestSubcommand(plugin).cmd,
            VersionSubcommand(plugin).cmd,
        )

}