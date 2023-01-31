package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response

import me.lokka30.treasury.api.common.response.FailureReason

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

                override fun toTreasury(): FailureReason {
                    return FailureReason { desc() }
                }
            }
        }
    }

    fun id(): String

    fun desc(): String

    fun toTreasury(): FailureReason

}