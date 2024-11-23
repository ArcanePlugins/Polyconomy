package io.github.arcaneplugins.polyconomy.api.account

enum class AccountPermission(
    val defaultValue: Boolean
) {

    BALANCE(true),

    WITHDRAW(false),

    DEPOSIT(true),

    MODIFY_PERMISSIONS(false),

}