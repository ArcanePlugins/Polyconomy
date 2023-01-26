package io.github.arcaneplugins.polyconomy.plugin.bukkit.debug

import java.util.*

object DebugManager {

    private val enabledCategories: EnumSet<DebugCategory> = EnumSet.noneOf(DebugCategory::class.java)

    fun isCategoryEnabled(dCat: DebugCategory): Boolean {
        return enabledCategories.contains(dCat)
    }

}