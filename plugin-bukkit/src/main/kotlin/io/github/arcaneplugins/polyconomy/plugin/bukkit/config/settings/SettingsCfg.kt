package io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.Config
import java.math.BigDecimal
import kotlin.io.path.Path

class SettingsCfg(
    plugin: Polyconomy,
) : Config(
    plugin = plugin,
    name = "Settings",
    relativePath = Path("settings.yml")
) {
    override fun load() {
        read()
        plugin.debugManager.load()
    }

    fun getPrimaryCurrencyId(): String {
        return rootNode.node("primary-currency").string!!
    }

    fun getMinimumBalance(): BigDecimal {
        return BigDecimal.valueOf(rootNode.node("advanced", "minimum-balance").getDouble(0.0))
    }

    fun getDbHost(): String {
        return rootNode.node("storage", "database", "host").string!!
    }

    fun getDbPort(): String {
        return rootNode.node("storage", "database", "port").string!!
    }

    fun getDbName(): String {
        return rootNode.node("storage", "database", "name").string!!
    }

    fun getDbUser(): String {
        return rootNode.node("storage", "database", "user").string!!
    }

    fun getDbPass(): String {
        return rootNode.node("storage", "database", "pass").string!!
    }

}