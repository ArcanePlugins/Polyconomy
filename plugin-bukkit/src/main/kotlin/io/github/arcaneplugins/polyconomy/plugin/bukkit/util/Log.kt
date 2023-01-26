package io.github.arcaneplugins.polyconomy.plugin.bukkit.util

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugCategory
import io.github.arcaneplugins.polyconomy.plugin.bukkit.debug.DebugManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.function.Supplier

object Log {

    fun i(msg: Any) {
        Polyconomy.instance!!.logger.info(msg.toString())
    }

    fun w(msg: Any) {
        Polyconomy.instance!!.logger.warning(msg.toString())
    }

    fun s(msg: Any) {
        Polyconomy.instance!!.logger.severe(msg.toString())
    }

    fun d(dCat: DebugCategory, msgSup: Supplier<Any>) {
        if(dCat == DebugCategory.BROADCAST_TO_OPS)
            throw IllegalArgumentException("Debug category '${dCat}' is not loggable.")

        if(!DebugManager.isCategoryEnabled(dCat)) return

        val output = "[LM Debug : ${dCat}] ${msgSup.get()}"

        Polyconomy.instance!!.logger.info(output)

        if(DebugManager.isCategoryEnabled(DebugCategory.BROADCAST_TO_OPS)) {
            Bukkit.getOnlinePlayers()
                .filter { it.isOp }
                .forEach { it.sendMessage("${ChatColor.DARK_GRAY}${output}") }
        }
    }

}