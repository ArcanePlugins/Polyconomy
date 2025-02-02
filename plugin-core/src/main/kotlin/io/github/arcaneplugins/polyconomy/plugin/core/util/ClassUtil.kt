package io.github.arcaneplugins.polyconomy.plugin.core.util

object ClassUtil {

    fun isValidClasspath(classpath: String): Boolean {
        return try {
            Class.forName(classpath, false, javaClass.classLoader)
            true
        } catch (ex: ClassNotFoundException) {
            false
        }
    }
}