package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook

enum class HookType {

    /**
     * The [Hook] references an Economy API such as Treasury or Vault.
     */
    ECONOMY_API,

    /**
     * The [Hook] has a miscellaneous purpose.
     */
    @Suppress("unused")
    OTHER

}