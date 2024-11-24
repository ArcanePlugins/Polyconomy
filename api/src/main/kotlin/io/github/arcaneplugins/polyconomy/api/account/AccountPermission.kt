package io.github.arcaneplugins.polyconomy.api.account

enum class AccountPermission(
    val defaultValue: Boolean
) {

    BALANCE(true),

    WITHDRAW(true),

    DEPOSIT(true),

    MODIFY_PERMISSIONS(false),

    OWNER(false),

    TRANSFER_OWNERSHIP(false),

    INVITE_MEMBER(false),

    REMOVE_MEMBER(false),

    DELETE(false),

}