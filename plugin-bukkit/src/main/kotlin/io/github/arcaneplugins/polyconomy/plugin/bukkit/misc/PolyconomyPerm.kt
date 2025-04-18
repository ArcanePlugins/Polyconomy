package io.github.arcaneplugins.polyconomy.plugin.bukkit.misc


import java.util.*

enum class PolyconomyPerm {

    COMMAND_BALANCE,
    COMMAND_BALANCETOP,
    COMMAND_PAY,
    COMMAND_POLYCONOMY,
    COMMAND_POLYCONOMY_BACKUP,
    COMMAND_POLYCONOMY_CURRENCY,
    COMMAND_POLYCONOMY_CURRENCY_SET,
    COMMAND_POLYCONOMY_CURRENCY_REGISTER,
    COMMAND_POLYCONOMY_CURRENCY_UNREGISTER,
    COMMAND_POLYCONOMY_DEPOSIT,
    COMMAND_POLYCONOMY_RELOAD,
    COMMAND_POLYCONOMY_SUBROUTINE,
    COMMAND_POLYCONOMY_VERSION,
    COMMAND_POLYCONOMY_WITHDRAW;

    override fun toString(): String {
        return "polyconomy." + name.lowercase(Locale.ROOT).replace('_', '.')
    }

}