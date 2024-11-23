package io.github.arcaneplugins.polyconomy.plugin.bukkit.util.throwable

import io.github.arcaneplugins.polyconomy.plugin.bukkit.Polyconomy
import java.io.PrintWriter
import java.io.StringWriter

object ThrowableUtil {

    private const val DIVIDER_LEN = 35
    private val bigDivider = "+${"=".repeat(DIVIDER_LEN)}+"
    private val divider = "+${"-".repeat(DIVIDER_LEN)}+"

    fun explainHelpfully(
        plugin: Polyconomy,
        throwable: Throwable,
        otherInfo: String? = null,
        otherContext: String? = null,
        action: String = "running",
        printTrace: Boolean = true,
    ): DescribedThrowable {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        val stackTrace = stringWriter.toString()

        plugin.logger.severe(
            """
                
                ${bigDivider} <readme>
                ${divider}
                ATTENTION: Please take a moment to read the error log below.
                ${divider}
                
                An unexpected error occurred whilst ${action} Polyconomy (Is it up to date?).
                
                Sometimes, errors are caused not by the plugin itself, but by the user misconfiguring it.
                Please make sure Polyconomy is up to date and your server setup is compatible (see 'Requirements' Wiki page).
                Please double check the error message to see if you can spot any potential causes of the issue which you may be able to fix.
                
                Other information:
                ${otherInfo ?: "N/A"}
                
                Error message:
                ${throwable.message ?: "N/A"}
                
                Other context:
                ${otherContext ?: "N/A"}
                
                Stack trace:
                ${if (printTrace) stackTrace else "Omitted"}
                
                ${divider}
                ATTENTION: Please take a moment to read the error log above.
                ${divider}
                ${bigDivider} </readme>
            """.trimIndent()
        )

        return DescribedThrowable(throwable)
    }

}