package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage

/*
This object uses Caffeine to improve performance and memory consumption of storing database details.

The internal cache is NOT suitable for configurations where multiple Polyconomy instances are
running and using a shared storage solution, i.e., server networks and
MultiPaper-like configurations. They are strongly recommended to use Redis as an external
centralised cache, and if not, unfortunately they should disable the cache completely.
 */
object InternalCache {
    //TODO Caffeine cache system

    var enabled: Boolean = false
        private set
}