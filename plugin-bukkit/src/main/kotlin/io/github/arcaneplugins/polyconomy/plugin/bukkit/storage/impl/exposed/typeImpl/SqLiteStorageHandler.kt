package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.typeImpl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.ExposedStorageHandler
import org.jetbrains.exposed.sql.Database
import java.io.File

class SqLiteStorageHandler(
    plugin: Polyconomy
) : ExposedStorageHandler(
    plugin = plugin,
    id = "sqlite",
) {

    val dbFile = File(plugin.dataFolder, "data${File.separator}sqlite.db")

    override fun initializeDb(): Database {
        if (!dbFile.exists()) {
            dbFile.mkdirs()
            dbFile.createNewFile()
        }

        return Database.connect("jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC")
    }

}