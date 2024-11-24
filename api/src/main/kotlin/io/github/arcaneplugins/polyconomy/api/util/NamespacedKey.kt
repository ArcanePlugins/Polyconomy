package io.github.arcaneplugins.polyconomy.api.util

class NamespacedKey(
    val namespace: String,
    val key: String,
) {

    companion object {
        fun fromString(str: String): NamespacedKey {
            val split = str.split(':')

            if (split.size != 2) {
                throw IllegalArgumentException(
                    "A namespacedkey must have a namespace and key separated by 1 colon character"
                )
            }

            return NamespacedKey(
                split[0],
                split[1]
            )
        }
    }

    override fun toString(): String {
        return "${namespace}:${key}"
    }

}