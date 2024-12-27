package io.github.arcaneplugins.polyconomy.plugin.bukkit.misc

import java.util.*

enum class PolyPermission {

    COMMAND_BALANCE,
    COMMAND_BALANCETOP,
    COMMAND_PAY,
    COMMAND_POLYCONOMY,
    COMMAND_POLYCONOMY_BACKUP,
    COMMAND_POLYCONOMY_DEPOSIT,
    COMMAND_POLYCONOMY_RELOAD,
    COMMAND_POLYCONOMY_SUBROUTINE,
    COMMAND_POLYCONOMY_VERSION,
    COMMAND_POLYCONOMY_WITHDRAW;

    override fun toString(): String {
        return name.lowercase(Locale.ROOT).replace('_', '.')
    }

}