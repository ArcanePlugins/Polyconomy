package io.github.arcaneplugins.polyconomy.plugin.core.debug

enum class DebugCategory {

    /**
     * Reports activity in `TreasuryHook`
     *
     * @since v0.1.0
     */
    HOOK_TREASURY,

    /**
     * Reports activity in `ConfigurateStorageHandler`
     *
     * @since v0.1.0
     */
    STORAGE_CONFIGURATE,

    /**
     * Reports activity in `H2StorageHandler` and related H2 implementation classes.
     *
     * @since v0.4.6
     */
    STORAGE_H2,

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

}