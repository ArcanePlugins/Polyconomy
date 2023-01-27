package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response

enum class PolyStandardResponseError(
    val desc: String
) : PolyResponseError {

    PLAYER_ACCOUNT_PERMISSION_MODIFICATION_NOT_SUPPORTED(
        """
                Cannot modify the permissions of a player account!
                """.trimIndent()
    );

    override fun id(): String {
        return name
    }

    override fun desc(): String {
        return desc
    }

}