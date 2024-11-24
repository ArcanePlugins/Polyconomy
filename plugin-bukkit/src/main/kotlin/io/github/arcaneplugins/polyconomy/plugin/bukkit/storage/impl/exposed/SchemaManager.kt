package io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed

import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.Account
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.AccountBalance
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.AccountTransaction
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.Currency
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.CurrencyLocale
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.NonPlayerAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.NonPlayerAccountMember
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.PlayerAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.VaultBankAccount
import io.github.arcaneplugins.polyconomy.plugin.bukkit.storage.impl.exposed.schema.VaultBankAccountNonPlayerMember
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object SchemaManager {

    fun createTables() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Account,
                AccountBalance,
                AccountTransaction,
                Currency,
                CurrencyLocale,
                NonPlayerAccount,
                NonPlayerAccountMember,
                PlayerAccount,
                VaultBankAccount,
                VaultBankAccountNonPlayerMember,
            )

            TODO("Insert default currency and currencylocale values if needed")
        }
    }
}