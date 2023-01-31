package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response

import me.lokka30.treasury.api.economy.response.EconomyFailureReason

enum class PolyStandardResponseError(
    val desc: String,
    val toTreasury: EconomyFailureReason?
) : PolyResponseError {

    PLAYER_ACCOUNT_PERMISSION_MODIFICATION_NOT_SUPPORTED(
        desc = "Player account permissions may not be modified as per the Treasury API",
        toTreasury = EconomyFailureReason.PLAYER_ACCOUNT_PERMISSION_MODIFICATION_NOT_SUPPORTED
    ),

    NEGATIVE_AMOUNT_SPECIFIED(
        desc = "Negative amounts are not allowed in deposit or withdrawal amounts",
        toTreasury = EconomyFailureReason.NEGATIVE_AMOUNT_SPECIFIED
    ),

    TRANSACTION_OVERDRAFTS_BALANCE(
        desc = "The new balance of the account after the requested operation would be below the minimum balance on this server",
        toTreasury = null // TODO add to treasury
    ),

    CURRENCY_NOT_FOUND(
        desc = "The currency specified is not a valid registered currency on this server",
        toTreasury = EconomyFailureReason.CURRENCY_NOT_FOUND
    )

    ;

    override fun id(): String {
        return name
    }

    override fun desc(): String {
        return desc
    }

}