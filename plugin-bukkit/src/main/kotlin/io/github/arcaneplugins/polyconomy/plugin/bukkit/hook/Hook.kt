package io.github.arcaneplugins.polyconomy.plugin.bukkit.hook

@Suppress("EmptyMethod")
abstract class Hook(
    val id: String,

    @Suppress("unused")
    val type: HookType //TODO use
) {

    var registered: Boolean = false
        protected set

    abstract fun canRegister(): Boolean

    abstract fun register()

    abstract fun unregister()

}