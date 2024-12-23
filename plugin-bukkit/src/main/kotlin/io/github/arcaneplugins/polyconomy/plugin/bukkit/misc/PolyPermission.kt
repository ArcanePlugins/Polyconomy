package io.github.arcaneplugins.polyconomy.plugin.bukkit.misc

import java.util.*

enum class PolyPermission {

    COMMAND_POLYCONOMY,
    COMMAND_POLYCONOMY_BALANCE,
    COMMAND_POLYCONOMY_VERSION;

    override fun toString(): String {
        return name.lowercase(Locale.ROOT).replace('_', '.')
    }

}