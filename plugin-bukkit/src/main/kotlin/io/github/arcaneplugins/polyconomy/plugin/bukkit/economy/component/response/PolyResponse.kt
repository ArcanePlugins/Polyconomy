package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response

class PolyResponse<T>(
    val name: String,
    val result: T?,
    val error: PolyResponseError?
) {

    fun successful(): Boolean {
        return error == null
    }

}