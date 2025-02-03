package io.github.arcaneplugins.polyconomy.plugin.bukkit.command.polyconomy.subcommand

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.command.InternalCmd
import io.github.arcaneplugins.polyconomy.plugin.bukkit.misc.PolyconomyPerm
import java.util.function.Supplier

object VersionSubcommand : InternalCmd {

    override fun build(plugin: Polyconomy): CommandAPICommand {
        return CommandAPICommand("version")
            .withAliases("about", "info", "ver")
            .withPermission(PolyconomyPerm.COMMAND_POLYCONOMY_VERSION.toString())
            .executes(CommandExecutor { sender, _ ->
                val pdf = plugin.description

                plugin.translations.commandPolyconomyVersionView.sendTo(
                    sender, placeholders = mapOf(
                    "name" to Supplier { pdf.name },
                    "version" to Supplier { pdf.version },
                    "authors" to Supplier { plugin.translations.joinStrings(pdf.authors) },
                    "description" to Supplier { pdf.description ?: "\${project.description}" },
                    "website" to Supplier { pdf.website ?: "https://github.com/arcaneplugins/polyconomy" },
                    "support" to Supplier { "https://discord.gg/HqZwdcJ" }
                ))
            })
    }

}