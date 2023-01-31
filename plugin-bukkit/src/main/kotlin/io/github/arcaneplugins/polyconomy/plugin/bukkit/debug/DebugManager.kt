package io.github.arcaneplugins.polyconomy.plugin.bukkit.debug

import io.github.arcaneplugins.polyconomy.plugin.bukkit.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.DEBUG_MANAGER
import io.github.arcaneplugins.polyconomy.plugin.bukkit.util.Log
import java.util.*

object DebugManager {

    private val enabledCategories: EnumSet<DebugCategory> = EnumSet.noneOf(DebugCategory::class.java)

    fun isCategoryEnabled(dCat: DebugCategory): Boolean {
        return enabledCategories.contains(dCat)
    }

    fun load() {
        enabledCategories.clear()

        val categoriesNode = SettingsCfg
            .rootNode
            .node("advanced", "debug-categories")

        enabledCategories.addAll(
            categoriesNode.getList(DebugCategory::class.java, emptyList())!!
        )

        Log.d(DEBUG_MANAGER) { "Loaded debug manager." }
        Log.d(DEBUG_MANAGER) { "Current Categories: ${
            enabledCategories.joinToString(separator = ", ") { it.name }
        }" }

        if(enabledCategories.contains(DebugCategory.DEBUG_ALL)) {
            Log.d(DEBUG_MANAGER) { "'DEBUG_ALL' category detected: adding remaining categories." }
            enabledCategories.addAll(
                DebugCategory.values().filter {
                    // We don't want special flags to be added by debug_all
                    !it.name.startsWith("DEBUG")
                }
            )
        }

        if(!enabledCategories.isEmpty()) {
            Log.w("${enabledCategories.size} debug categories are enabled. These will spam your logs - remove them when you're done.")
        }

        Log.d(DEBUG_MANAGER) { "Loaded debug manager." }
    }

}