package io.github.arcaneplugins.polyconomy.plugin.bukkit.command

import dev.jorel.commandapi.CommandAPICommand
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy

interface InternalCmd {

    fun build(
        plugin: Polyconomy,
    ): CommandAPICommand

}