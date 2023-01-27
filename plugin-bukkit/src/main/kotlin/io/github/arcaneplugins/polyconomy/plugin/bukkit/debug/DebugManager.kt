package io.github.arcaneplugins.polyconomy.plugin.bukkit.debug

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import java.util.*

object DebugManager {

    private val enabledCategories: EnumSet<DebugCategory> = EnumSet.noneOf(DebugCategory::class.java)

    fun isCategoryEnabled(dCat: DebugCategory): Boolean {
        return enabledCategories.contains(dCat)
    }

    fun load() {
        Log.i("Loading debug manager.")

        enabledCategories.clear()

        val categoriesNode = SettingsCfg
            .rootNode!!
            .node("advanced", "debug-categories")

        enabledCategories.addAll(
            categoriesNode.getList(DebugCategory::class.java, emptyList())!!
        )

        if(enabledCategories.contains(DebugCategory.DEBUG_ALL)) {
            enabledCategories.addAll(DebugCategory.values())
        }

        if(!enabledCategories.isEmpty()) {
            Log.w("${enabledCategories.size} debug categories are enabled. These will spam your logs - remove them when you're done.")
        }
    }

}