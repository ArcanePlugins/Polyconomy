package io.github.arcaneplugins.polyconomy.plugin.core.config.settings

import io.github.arcaneplugins.polyconomy.plugin.core.Platform
import io.github.arcaneplugins.polyconomy.plugin.core.config.Config
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@OptIn(ExperimentalPathApi::class)
class SettingsCfg(
    plugin: Platform,
) : Config(
    plugin = plugin,
    name = "Settings",
    resourcePath = Path("settings.yml")
) {
    override fun load() {
        read()
        plugin.debugManager.load()
    }

    fun storageImplementation(): String {
        return rootNode.node("storage", "implementation").string
            ?: throw IllegalArgumentException("Required setting not set: Storage Implementation")
    }

    fun primaryCurrencyId(): String {
        return rootNode.node("primary-currency").string
            ?: throw IllegalArgumentException("Required setting not set: Primary Currency (ID)")
    }

    fun minimumBalance(): BigDecimal {
        return BigDecimal.valueOf(rootNode.node("advanced", "minimum-balance").double)
    }

    fun dbHost(): String {
        return rootNode.node("storage", "database", "host").string
            ?: throw IllegalArgumentException("Required setting not set: Storage DB Host Address")
    }

    fun dbPort(): String {
        return rootNode.node("storage", "database", "port").string
            ?: throw IllegalArgumentException("Required setting not set: Storage DB Host Port")
    }

    fun dbName(): String {
        return rootNode.node("storage", "database", "name").string
            ?: throw IllegalArgumentException("Required setting not set: Storage DB Database Name")
    }

    fun dbUser(): String {
        return rootNode.node("storage", "database", "user").string
            ?: throw IllegalArgumentException("Required setting not set: Storage DB Username")
    }

    fun dbPass(): String {
        return rootNode.node("storage", "database", "pass").string
            ?: throw IllegalArgumentException("Required setting not set: Storage DB Password")
    }

    fun dbShouldRunCleanupTask(): Boolean {
        return rootNode.node("storage", "database", "cleanup-task", "run").boolean
    }

    fun dbCleanupTaskPeriod(): Long {
        return TimeUnit.SECONDS.convert(
            rootNode.node("storage", "database", "cleanup-task", "period").long,
            TimeUnit.MINUTES
        ) * 20L
    }

    fun defaultLocale(): Locale {
        val langTag = rootNode.node("primary-locale").string

        return if (langTag == null) {
            Locale.getDefault(Locale.Category.DISPLAY)
        } else {
            Locale.forLanguageTag(langTag)
        }
    }

}