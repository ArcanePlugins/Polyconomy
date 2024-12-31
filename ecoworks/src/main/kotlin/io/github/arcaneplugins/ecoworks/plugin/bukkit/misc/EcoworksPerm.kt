package io.github.arcaneplugins.ecoworks.plugin.bukkit.misc

import java.util.*

enum class EcoworksPerm {

    COMMAND_ECOWORKS,
    COMMAND_ECOWORKS_TEST,
    COMMAND_ECOWORKS_TEST_POLYCONOMY,
    COMMAND_ECOWORKS_TEST_TREASURY,
    COMMAND_ECOWORKS_TEST_VAULT_LEGACY,
    COMMAND_ECOWORKS_TEST_VAULT_UNLOCKED,
    COMMAND_ECOWORKS_VERSION;

    override fun toString(): String {
        return "ecoworks.${name.lowercase(Locale.ROOT).replace('_', '.')}"
    }

}