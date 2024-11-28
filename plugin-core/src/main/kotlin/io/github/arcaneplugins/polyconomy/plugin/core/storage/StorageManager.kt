package io.github.arcaneplugins.polyconomy.plugin.core.storage

import io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.configurate.impl.JsonStorageHandler
import io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.configurate.impl.YamlStorageHandler
import io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2.H2StorageHandler
import java.io.File
import java.math.BigDecimal
import kotlin.io.path.Path

class StorageManager(
    val dataFolder: File,
    val primaryCurrencyId: String,
    val minimumBalance: BigDecimal,
) {

    val availableHandlers = mutableSetOf(
        JsonStorageHandler(
            absolutePath = Path(dataFolder.absolutePath, "data", "data.json"),
            manager = this,
        ),
        YamlStorageHandler(
            absolutePath = Path(dataFolder.absolutePath, "data", "data.yml"),
            manager = this,
        ),
        H2StorageHandler(
            absolutePath = Path(dataFolder.absolutePath, "data", "h2.db"),
            manager = this,
        )
    )

    lateinit var handler: StorageHandler

    fun startup(
        handlerImplId: String
    ) {
        handler = availableHandlers.firstOrNull { it.id.equals(handlerImplId, ignoreCase = true) }
            ?: throw IllegalArgumentException("There is no available storage handler matching an ID of ${handlerImplId}. (Did you make a typo?)")

        handler.startup()
    }

    fun shutdown() {
        if (::handler.isInitialized) {
            handler.shutdown()
        }
    }

}