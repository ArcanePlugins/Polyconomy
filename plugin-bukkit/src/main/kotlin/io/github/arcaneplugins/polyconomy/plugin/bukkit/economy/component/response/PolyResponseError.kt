package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response

interface PolyResponseError {

    companion object {
        fun fromException(
            ex: Exception
        ): PolyResponseError {
            return object : PolyResponseError {
                override fun id(): String {
                    return ex::class.simpleName ?: ex::class.java.name
                }

                override fun desc(): String {
                    return """
                    Exception message: ${ex.message}
                    Stack trace:
                    ${ex.stackTraceToString()}
                    """.trimIndent()
                }
            }
        }
    }

    fun id(): String

    fun desc(): String

}