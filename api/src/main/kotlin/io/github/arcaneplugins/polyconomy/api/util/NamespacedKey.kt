package io.github.arcaneplugins.polyconomy.api.util

class NamespacedKey(
    val namespace: String,
    val key: String,
) {

    constructor(string: String) : this(string.split(':')[0], string.split(':')[1])

    override fun toString(): String {
        return "${namespace}:${key}"
    }

}