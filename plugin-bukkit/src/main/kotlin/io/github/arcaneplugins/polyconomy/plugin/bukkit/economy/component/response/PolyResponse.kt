package io.github.arcaneplugins.polyconomy.plugin.bukkit.economy.component.response

import me.lokka30.treasury.api.common.response.Response

class PolyResponse<T>(
    val name: String,
    val result: T?,
    val error: PolyResponseError?
) {

    companion object {
        fun <T> fromTreasury(
            treasuryResponse: Response<T>
        ): PolyResponse<T> {
            return if(treasuryResponse.isSuccessful) {
                PolyResponse(
                    name = "Treasury Response",
                    result = treasuryResponse.result!!,
                    error = null
                )
            } else {
                PolyResponse(
                    name = "Treasury Response",
                    result = null,
                    error = PolyResponseError.fromTreasury(treasuryResponse.failureReason!!)
                )
            }
        }
    }

    fun successful(): Boolean {
        return error == null
    }

    fun toTreasury(): Response<T> {
        return if(successful()) {
            Response.success(result!!)
        } else {
            Response.failure(error!!.toTreasury())
        }
    }

}