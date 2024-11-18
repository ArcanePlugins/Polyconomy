package io.github.arcaneplugins.polyconomy.api.util

class NamespacedKey(
    val namespace: String,
    val key: String
) {

    override fun toString(): String {
        return "${namespace}:${key}"
    }

}