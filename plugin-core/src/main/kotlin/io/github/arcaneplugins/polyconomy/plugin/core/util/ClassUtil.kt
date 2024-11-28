package io.github.arcaneplugins.polyconomy.plugin.core.util

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