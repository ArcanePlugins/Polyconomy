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

    fun getStorageImplementation(): String {
        return rootNode.node("storage", "implementation").string
            ?: throw IllegalArgumentException("Required setting not set: Storage Implementation")
    }

    fun getPrimaryCurrencyId(): String {
        return rootNode.node("primary-currency").string
            ?: throw IllegalArgumentException("Required setting not set: Primary Currency (ID)")
    }

    fun getMinimumBalance(): BigDecimal {
        return BigDecimal.valueOf(rootNode.node("advanced", "minimum-balance").double)
    }

    fun getDbHost(): String {
        return rootNode.node("storage", "database", "host").string
            ?: throw IllegalArgumentException("Required setting not set: Storage DB Host Address")
    }

    fun getDbPort(): String {
        return rootNode.node("storage", "database", "port").string
            ?: throw IllegalArgumentException("Required setting not set: Storage DB Host Port")
    }

    fun getDbName(): String {
        return rootNode.node("storage", "database", "name").string
            ?: throw IllegalArgumentException("Required setting not set: Storage DB Database Name")
    }

    fun getDbUser(): String {
        return rootNode.node("storage", "database", "user").string
            ?: throw IllegalArgumentException("Required setting not set: Storage DB Username")
    }

    fun getDbPass(): String {
        return rootNode.node("storage", "database", "pass").string
            ?: throw IllegalArgumentException("Required setting not set: Storage DB Password")
    }

}