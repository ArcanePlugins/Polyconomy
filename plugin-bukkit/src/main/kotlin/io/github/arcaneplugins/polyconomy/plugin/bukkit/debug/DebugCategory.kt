package io.github.arcaneplugins.polyconomy.plugin.bukkit.debug

enum class DebugCategory {

    /**
     * Reports activity in
     * [io.github.arcaneplugins.polyconomy.plugin.bukkit.hook.impl.treasury.TreasuryHook].
     */
    HOOK_TREASURY,

    /**
     *  If enabled, all debug logs are broadcasted to online
     *  players who are server operators ('*opped*' using `/op`).
     */
    DEBUG_BROADCAST_OPS,

    /**
     * Reports activity in
     * [io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.impl.local.YamlStorageHandler].
     */
    STORAGE_YAML,

    /**
     * Reports activity in
     * [io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.storage.StorageManager]
     */
    STORAGE_MANAGER,

    /**
     * Reports activity in
     * [io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.EconomyManager]
     */
    ECONOMY_MANAGER,

    /**
     * Enables all debug categories
     */
    DEBUG_ALL,

}