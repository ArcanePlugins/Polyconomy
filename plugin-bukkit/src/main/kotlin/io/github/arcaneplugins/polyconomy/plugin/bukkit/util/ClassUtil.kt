package io.github.arcaneplugins.polyconomy.plugin.bukkit.util

object ClassUtil {

    fun isValidClasspath(classpath: String): Boolean {
        return try {
            Class.forName(classpath)
            true
        } catch (ex: ClassNotFoundException) {
            false
        }
    }
}