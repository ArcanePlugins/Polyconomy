package io.github.arcaneplugins.polyconomy.plugin.bukkit.debug

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import java.util.*

object DebugManager {

    private val enabledCategories: EnumSet<DebugCategory> = EnumSet.noneOf(DebugCategory::class.java)

    fun isCategoryEnabled(dCat: DebugCategory): Boolean {
        return enabledCategories.contains(dCat)
    }

    fun load() {
        enabledCategories.clear()
        enabledCategories.addAll(SettingsCfg.rootNode!!.getList(DebugCategory::class.java)!!)
    }

}