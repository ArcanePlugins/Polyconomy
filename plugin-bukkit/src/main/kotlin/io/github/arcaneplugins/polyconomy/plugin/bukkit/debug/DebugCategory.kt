package io.github.arcaneplugins.polyconomy.plugin.bukkit.debug

enum class DebugCategory {

    /**
     * Reports activity in `TreasuryHook`
     *
     * @since v0.1.0
     */
    HOOK_TREASURY,

    /**
     *  If enabled, all debug logs are broadcasted to online
     *  players who are server operators ('*opped*' using `/op`).
     *
     *  @since v0.1.0
     */
    DEBUG_BROADCAST_OPS,

    /**
     * Reports activity in `YamlStorageHandler`
     *
     * @since v0.1.0
     */
    STORAGE_YAML,

    /**
     * Reports activity in `StorageManager`
     *
     * @since v0.1.0
     */
    STORAGE_MANAGER,

    /**
     * Reports activity in `EconomyManager`
     *
     * @since v0.1.0
     */
    ECONOMY_MANAGER,

    /**
     * Reports activity in [DebugManager]
     *
     * @since v0.1.0
     */
    DEBUG_MANAGER,

    /**
     * Enables all debug categories
     *
     * @since v0.1.0
     */
    DEBUG_ALL,

    /**
     * Reports activity in `ConfigManager`
     *
     * @since v0.1.0
     */
    CONFIG_MANAGER,

    /**
     * Reports activity in `HookManager`
     *
     * @since v0.1.0
     */
    HOOK_MANAGER,

    /**
     * A generic category for testing purposes
     *
     * @since v0.1.0
     */
    DEBUG_TEST,

    ;

    fun enabled(): Boolean {
        return DebugManager.isCategoryEnabled(this)
    }

}