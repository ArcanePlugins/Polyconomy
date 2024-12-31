package io.github.arcaneplugins.ecoworks.plugin.bukkit.testing

import net.md_5.bungee.api.ChatColor

data class TestResult(
    val status: Status, // how did the test go?
    val context: String, // context of the test i.e. what thing was tested
    val description: String, // what is the test doing?
    val message: String? = null, // anything important the user should know
) {
    companion object {
        enum class Status(
            val color: ChatColor,
            val shouldStopTest: Boolean,
        ) {
            SUCCESS(ChatColor.GREEN, false),
            OBSERVATION(ChatColor.LIGHT_PURPLE, false),
            WARNING(ChatColor.YELLOW, false),
            FAILURE(ChatColor.RED, true);
        }
    }
}
