package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook

abstract class Hook(
    val id: String,
    val type: HookType
) {

    var registered: Boolean = false
        protected set

    abstract fun canRegister(): Boolean

    abstract fun register()

    abstract fun unregister()

}