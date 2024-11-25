package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.typeImpl

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.ExposedStorageHandler
import org.jetbrains.exposed.sql.Database

class MariaDbStorageHandler(
    plugin: Polyconomy,
) : ExposedStorageHandler(
    plugin = plugin,
    id = "mariadb",
) {

    override fun initializeDb(): Database {
        val host = plugin.settings.getDbHost()
        val port = plugin.settings.getDbPort()
        val database = plugin.settings.getDbName()
        val username = plugin.settings.getDbUser()
        val password = plugin.settings.getDbPass()

        return Database.connect(
            url = "jdbc:mariadb://${host}:${port}/${database}",
            driver = "org.mariadb.jdbc.Driver",
            user = username,
            password = password,
        )

    }

}