package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response

import me.lokka30.treasury.api.common.response.FailureReason

enum class PolyStandardResponseError(
    val desc: String
) : PolyResponseError {

    PLAYER_ACCOUNT_PERMISSION_MODIFICATION_NOT_SUPPORTED(
        desc = "Player account permissions may not be modified as per the Treasury API"
    ),

    NEGATIVE_AMOUNT_SPECIFIED(
        desc = "Negative amounts are not allowed in deposit or withdrawal amounts"
    ),

    TRANSACTION_OVERDRAFTS_BALANCE(
        desc = "The new balance of the account after the requested operation would be below the minimum balance on this server"
    ),

    CURRENCY_NOT_FOUND(
        desc = "The currency specified is not a valid registered currency on this server"
    )

    ;

    override fun id(): String {
        return name
    }

    override fun desc(): String {
        return desc
    }

    override fun toTreasury(): FailureReason {
        return FailureReason { desc }
    }

}