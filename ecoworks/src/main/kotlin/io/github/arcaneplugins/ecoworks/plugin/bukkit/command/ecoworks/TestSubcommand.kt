package io.github.arcaneplugins.ecoworks.plugin.bukkit.command.ecoworks

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.CommandExecutor
import io.github.arcaneplugins.ecoworks.plugin.bukkit.Ecoworks
import io.github.arcaneplugins.ecoworks.plugin.bukkit.misc.EcoworksPerm
import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.TestResult
import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.TestResult.Companion.Status
import io.github.arcaneplugins.ecoworks.plugin.bukkit.testing.TestType
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.CommandSender

class TestSubcommand(val plugin: Ecoworks) {

    val cmd: CommandAPICommand = CommandAPICommand("test")
        .withPermission(EcoworksPerm.COMMAND_ECOWORKS_TEST.toString())
        .withSubcommands(
            TestPolyconomySubcommand(this).cmd,
            TestVaultLegacySubcommand(this).cmd,
            TestVaultUnlockedSubcommand(this).cmd,
            TestTreasurySubcommand(this).cmd,
        )

    private fun handleTest(
        sender: CommandSender,
        type: TestType,
    ) {
        sender.spigot().sendMessage(
            ComponentBuilder("=== ${type.name} STARTING TEST ===")
                .color(ChatColor.GREEN)
                .bold(true)
                .append("\nHover over the test context for a description of the test.")
                .italic(true)
                .bold(false)
                .color(ChatColor.GRAY)
                .build()
        )

        val results = Status.entries.associateWith { mutableListOf<TestResult>() }

        val consumer = { result: TestResult ->
            results[result.status]!!.add(result)
            sender.spigot().sendMessage(
                ComponentBuilder("[${result.status.name}] ")
                    .color(result.status.color)
                    .append("[${result.context}]")
                    .event(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Text(ComponentBuilder(result.description).color(ChatColor.GRAY).build())
                        )
                    )
                    .color(ChatColor.GRAY)
                    .append(": ${result.message ?: "..."}")
                    .color(ChatColor.DARK_GRAY)
                    .build()
            )
        }

        when (type) {
            TestType.POLYCONOMY -> plugin.testMgr.testPolyconomy(consumer)
            TestType.VAULT_LEGACY -> plugin.testMgr.testVaultLegacy(consumer)
            TestType.VAULT_UNLOCKED -> plugin.testMgr.testVaultUnlocked(consumer)
            TestType.TREASURY -> plugin.testMgr.testTreasury(consumer)
        }

        val isFailure = results.filterKeys { it.shouldStopTest }.any { it.value.size > 0 }

        if (isFailure) {
            sender.spigot().sendMessage(
                ComponentBuilder("=== ${type.name} TEST FAILED ===")
                    .color(ChatColor.RED)
                    .bold(true)
                    .build()
            )
        } else {
            sender.spigot().sendMessage(
                ComponentBuilder("=== ${type.name} TEST SUCCESS ===")
                    .color(ChatColor.GREEN)
                    .bold(true)
                    .build()
            )
        }

        results.forEach { (status, results) ->
            sender.spigot().sendMessage(
                ComponentBuilder(" \u2022 ")
                    .color(ChatColor.DARK_GRAY)
                    .append(status.name)
                    .color(status.color)
                    .append(": ${results.size}")
                    .color(ChatColor.GRAY)
                    .build()
            )
        }
    }

    class TestVaultLegacySubcommand(val parent: TestSubcommand) {
        val cmd: CommandAPICommand = CommandAPICommand("vault-legacy")
            .withPermission(EcoworksPerm.COMMAND_ECOWORKS_TEST_VAULT_LEGACY.toString())
            .executes(CommandExecutor { sender, _ ->
                parent.handleTest(sender, TestType.VAULT_LEGACY)
            })
    }

    class TestVaultUnlockedSubcommand(val parent: TestSubcommand) {
        val cmd: CommandAPICommand = CommandAPICommand("vault-unlocked")
            .withPermission(EcoworksPerm.COMMAND_ECOWORKS_TEST_VAULT_UNLOCKED.toString())
            .executes(CommandExecutor { sender, _ ->
                parent.handleTest(sender, TestType.VAULT_UNLOCKED)
            })
    }

    class TestTreasurySubcommand(val parent: TestSubcommand) {
        val cmd: CommandAPICommand = CommandAPICommand("treasury")
            .withPermission(EcoworksPerm.COMMAND_ECOWORKS_TEST_TREASURY.toString())
            .executes(CommandExecutor { sender, _ ->
                parent.handleTest(sender, TestType.TREASURY)
            })
    }

    class TestPolyconomySubcommand(val parent: TestSubcommand) {
        val cmd: CommandAPICommand = CommandAPICommand("polyconomy")
            .withPermission(EcoworksPerm.COMMAND_ECOWORKS_TEST_POLYCONOMY.toString())
            .executes(CommandExecutor { sender, _ ->
                parent.handleTest(sender, TestType.POLYCONOMY)
            })
    }

}