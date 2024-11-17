package io.github.arcaneplugins.polyconomy.plugin.bukkit.debug

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory.DEBUG_MANAGER
import java.util.*

class DebugManager(
    val plugin: Polyconomy,
) {

    private val enabledCategories: EnumSet<DebugCategory> = EnumSet.noneOf(DebugCategory::class.java)

    fun enabled(dCat: DebugCategory): Boolean {
        return enabledCategories.contains(dCat)
    }

    fun load() {
        enabledCategories.clear()

        val categoriesNode = plugin.configManager.settings
            .rootNode
            .node("advanced", "debug-categories")

        enabledCategories.addAll(
            categoriesNode.getList(DebugCategory::class.java, emptyList())!!
        )

        plugin.debugLog(DEBUG_MANAGER) { "Loaded debug manager." }
        plugin.debugLog(DEBUG_MANAGER) {
            "Current Categories: ${
                enabledCategories.joinToString(separator = ", ") { it.name }
            }"
        }

        if (enabledCategories.contains(DebugCategory.DEBUG_ALL)) {
            plugin.debugLog(DEBUG_MANAGER) { "'DEBUG_ALL' category detected: adding remaining categories." }
            enabledCategories.addAll(
                DebugCategory.entries.filter {
                    // We don't want special flags to be added by debug_all
                    !it.name.startsWith("DEBUG")
                }
            )
        }

        if (!enabledCategories.isEmpty()) {
            plugin.logger.warning("${enabledCategories.size} debug categories are enabled. These will spam your logs - remove them when you're done.")
        }

        plugin.debugLog(DEBUG_MANAGER) { "Loaded debug manager." }
    }

}