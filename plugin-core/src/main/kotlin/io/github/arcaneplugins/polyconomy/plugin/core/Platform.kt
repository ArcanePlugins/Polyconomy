package io.github.arcaneplugins.polyconomy.plugin.core

import io.github.arcaneplugins.polyconomy.plugin.core.config.Config
import io.github.arcaneplugins.polyconomy.plugin.core.config.messages.MessagesCfg
import io.github.arcaneplugins.polyconomy.plugin.core.config.settings.SettingsCfg
import io.github.arcaneplugins.polyconomy.plugin.core.debug.DebugCategory
import io.github.arcaneplugins.polyconomy.plugin.core.debug.DebugManager
import io.github.arcaneplugins.polyconomy.plugin.core.storage.StorageManager
import java.nio.file.Path
import java.util.function.Supplier
import java.util.logging.Logger

interface Platform {

    val settings: SettingsCfg
    val messages: MessagesCfg
    val configs: LinkedHashSet<Config>
    val debugManager: DebugManager
    var storageManager: StorageManager
    val nativeLogger: Logger

    fun dataFolder(): Path

    /**
     * Logs the given message if the given [DebugCategory] is enabled.
     *
     * **Note:** Under normal circumstances, this method will not log any debug categories until
     * the DebugHandler has loaded the enabled categories from the Settings config.
     *
     * @param cat Debug category associated with the message being supplied
     * @param msg Supplier of the message which will be accessed if the category is enabled
     */
    @Suppress("unused")
    fun debugLog(cat: DebugCategory, msg: Supplier<Any>) {
        if (!debugManager.enabled(cat)) {
            return
        }

        val output = "[DEBUG: ${cat}] ${msg.get()}"

        nativeLogger.info(output)
    }

}