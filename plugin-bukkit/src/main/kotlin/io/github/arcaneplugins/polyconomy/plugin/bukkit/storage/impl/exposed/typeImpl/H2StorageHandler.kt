package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.typeImpl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.ExposedStorageHandler
import org.jetbrains.exposed.sql.Database
import java.io.File

class H2StorageHandler(
    plugin: Polyconomy,
) : ExposedStorageHandler(
    plugin = plugin,
    id = "h2",
) {

    val dbFile = File(plugin.dataFolder, "data${File.separator}h2.db")

    override fun initializeDb(): Database {
        if (!dbFile.exists()) {
            dbFile.parentFile.mkdirs()
        }

        return Database.connect(
            url = "jdbc:h2:${dbFile.absolutePath};DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
        )
    }

}