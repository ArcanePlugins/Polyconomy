package io.github.arcaneplugins.polyconomy.plugin.bukkit.util

class PolyResponse<T>(
    val name: String,
    val result: T?,
    val error: PolyResponseError?
) {

    fun successful(): Boolean {
        return error == null
    }

    class PolyResponseError(
        val id: String,
        val desc: String
    ) {
        companion object {
            // TODO: make constants here.
        }
    }

}