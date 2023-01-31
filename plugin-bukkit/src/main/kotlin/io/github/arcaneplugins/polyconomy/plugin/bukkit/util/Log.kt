package io.github.arcaneplugins.polyconomy.plugin.bukkit.util

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.function.Supplier
import java.util.logging.Logger

object Log {

    /**
     * Logs the given message with [Logger.info].
     *
     * **Warning:** Logging is not available prior to [Polyconomy.onLoad] being called.
     */
    fun i(msg: Any) {
        logger().info(msg.toString())
    }

    /**
     * Logs the given message with [Logger.warning].
     *
     * **Warning:** Logging is not available prior to [Polyconomy.onLoad] being called.
     */
    fun w(msg: Any) {
        logger().warning(msg.toString())
    }

    /**
     * Logs the given message with [Logger.severe].
     *
     * **Warning:** Logging is not available prior to [Polyconomy.onLoad] being called.
     */
    fun s(msg: Any) {
        logger().severe(msg.toString())
    }

    /**
     * Logs the given message if the given [DebugCategory] is enabled.
     *
     * **Warning:** Logging is not available prior to [Polyconomy.onLoad] being called.
     *
     * **Note:** Under normal circumstances, this method will not log any debug categories until
     * the DebugHandler has loaded the enabled categories from the Settings config. You can
     * forcefully allow debug logs to happen at any point after [Polyconomy.onLoad] is called
     * by programatically modifying [DebugManager.enabledCategories].
     *
     * @param dCat   Debug category associated with the message being supplied
     * @param msgSup Supplier of the message which will be accessed if the category is enabled
     */
    fun d(dCat: DebugCategory, msgSup: Supplier<Any>) {
        if(!DebugManager.isCategoryEnabled(dCat)) return

        val output = "[DEBUG: ${dCat}] ${msgSup.get()}"

        logger().info(output)

        if(DebugManager.isCategoryEnabled(DebugCategory.DEBUG_BROADCAST_OPS)) {
            Bukkit.getOnlinePlayers()
                .filter(Player::isOp)
                .forEach { it.sendMessage("${ChatColor.DARK_GRAY}${output}") }
        }
    }

    /**
     * Retrieves the [Logger] instance from [Polyconomy]. The [Logger] instance is only available
     * after [Polyconomy.onLoad] is called. The behaviour of accessing the logger after
     * [Polyconomy.onDisable] is called (until [Polyconomy.onLoad] is potentially called) is
     * **undefined**.
     */
    private fun logger(): Logger {
        return Polyconomy.instance.logger
    }

}