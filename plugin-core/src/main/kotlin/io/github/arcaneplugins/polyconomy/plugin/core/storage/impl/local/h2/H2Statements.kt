package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

import io.github.arcaneplugins.polyconomy.plugin.core.util.StdKey

object H2Statements {

    val createTablesStatements = listOf(
        """
                CREATE TABLE IF NOT EXISTS Account (
                    id      IDENTITY        NOT NULL,
                    name    VARCHAR(255)    NOT NULL,
                    PRIMARY KEY (id)
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS PlayerAccount (
                    id          BIGINT          NOT NULL,
                    player_uuid BINARY(16)      NOT NULL UNIQUE,
                    PRIMARY KEY (id),
                    FOREIGN KEY id REFERENCES Account.id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS NonPlayerAccount (
                    id              BIGINT          NOT NULL,
                    namespaced_key  VARCHAR(255)    NOT NULL UNIQUE,
                    PRIMARY KEY (id),
                    FOREIGN KEY id REFERENCES Account.id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS NonPlayerAccountMember (
                    account_id                  BIGINT          NOT NULL,
                    member_id                   BINARY(16)      NOT NULL,
                    perm_balance                BOOLEAN         NULL,
                    perm_withdraw               BOOLEAN         NULL,
                    perm_deposit                BOOLEAN         NULL,
                    perm_modify_perms           BOOLEAN         NULL,
                    perm_owner                  BOOLEAN         NULL,
                    perm_transfer_ownership     BOOLEAN         NULL,
                    perm_invite_member          BOOLEAN         NULL,
                    perm_remove_member          BOOLEAN         NULL,
                    perm_delete                 BOOLEAN         NULL,
                    PRIMARY KEY (account_id, member_id),
                    FOREIGN KEY account_id REFERENCES NonPlayerAccount.id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS VaultBankAccount (
                    account_id      BIGINT          NOT NULL,
                    owner_string    VARCHAR(255)    NOT NULL,
                    owner_uuid      BINARY(16)      NOT NULL,
                    PRIMARY KEY (account_id),
                    FOREIGN KEY account_id REFERENCES NonPlayerAccount.id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS VaultBankAccountNonPlayerMember (
                    account_id      BIGINT          NOT NULL,
                    member_id_str   VARCHAR(255)    NOT NULL,
                    PRIMARY KEY (account_id, member_id_str),
                    FOREIGN KEY account_id REFERENCES VaultBankAccount.account_id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS Currency (
                    id                  IDENTITY        NOT NULL,
                    name                VARCHAR(255)    NOT NULL UNIQUE,
                    starting_balance    DECIMAL(18, 4)  NOT NULL,
                    symbol              VARCHAR(32)     NOT NULL,
                    amount_format       VARCHAR(255)    NOT NULL,
                    presentation_format VARCHAR(1023)   NOT NULL,
                    conversion_rate     DECIMAL(18, 4)  NOT NULL,
                    PRIMARY KEY (id)
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS CurrencyLocale (
                    id                      BIGINT          NOT NULL,
                    locale                  VARCHAR(255)    NOT NULL,
                    display_name_singular   VARCHAR(255)    NOT NULL,
                    display_name_plural     VARCHAR(255)    NOT NULL,
                    decimal                 VARCHAR(32)     NOT NULL,
                    PRIMARY KEY (id, locale),
                    FOREIGN KEY id REFERENCES Currency.id
                );
        """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS AccountBalance (
                    account_id              BIGINT          NOT NULL,
                    currency_id             BIGINT          NOT NULL,
                    amount                  DECIMAL(18, 4)  NOT NULL,
                    PRIMARY KEY (account_id, currency_id),
                    FOREIGN KEY account_id REFERENCES Account.id,
                    FOREIGN KEY currency_id REFERENCES Currency.id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS AccountTransaction (
                    id                      IDENTITY        NOT NULL,
                    account_id              BIGINT          NOT NULL,
                    amount                  DECIMAL(18, 4)  NOT NULL,
                    currency_id             BIGINT          NOT NULL,
                    cause                   SMALLINT        NOT NULL,
                    reason                  VARCHAR(1023)   NOT NULL,
                    importance              SMALLINT        NOT NULL,
                    type                    SMALLINT        NOT NULL,
                    timestamp               BIGINT          NOT NULL,
                    PRIMARY KEY (id),
                    FOREIGN KEY account_id REFERENCES Account.id,
                    FOREIGN KEY currency_id REFERENCES Currency.id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS PlayerUsernameCache (
                    uuid                    BINARY(16)      NOT NULL,
                    username                VARCHAR(32)     NOT NULL,
                    last_updated            BIGINT          NOT NULL,
                    PRIMARY KEY (uuid)
                );
            """.trimIndent(),
    )

    val getUsernameByUuid = """
        SELECT username
        FROM PlayerUsernameCache
        WHERE uuid = ?;
    """.trimIndent()

    val setUsernameForUuid = """
        MERGE INTO PlayerUsernameCache
        VALUES (?, ?, ?);
    """.trimIndent()

    val isPlayerCached = """
        SELECT COUNT(*)
        FROM PlayerUsernameCache
        WHERE uuid = ?;
    """.trimIndent()

    val getPlayerAccountName = """
        SELECT name
        FROM PlayerAccount
        INNER JOIN Account ON Account.id = PlayerAccount.id
        WHERE PlayerAccount.player_uuid = ?;
    """.trimIndent()

    val getNonPlayerAccountName = """
        SELECT name
        FROM NonPlayerAccount
        INNER JOIN Account ON Account.id = NonPlayerAccount.id
        WHERE NonPlayerAccount.namespaced_key = ?;
    """.trimIndent()

    val createAccount = """
        INSERT INTO Account (name)
        VALUES (?);
    """.trimIndent()

    val createPlayerAccount = """
        INSERT INTO PlayerAccount (id, player_uuid)
        VALUES (?, ?);
    """.trimIndent()

    val createNonPlayerAccount = """
        INSERT INTO NonPlayerAccount (id, namespaced_key)
        VALUES (?, ?);
    """.trimIndent()

    val getPlayerAccountIds = """
        SELECT player_uuid
        FROM PlayerAccount;
    """.trimIndent()

    val getNonPlayerAccountIds = """
        SELECT namespaced_key
        FROM NonPlayerAccount;
    """.trimIndent()

    val getNonPlayerAccountsPlayerIsMemberOf = """
        SELECT namespaced_key
        FROM NonPlayerAccount
        INNER JOIN NonPlayerAccountMember ON NonPlayerAccountMember.member_id = ?;
    """.trimIndent()

    val getCurrencyByName = """
        SELECT id
        FROM Currency
        WHERE name = ?;
    """.trimIndent()

    val getCurrencyNames = """
        SELECT name
        FROM Currency;
    """.trimIndent()

    val insertCurrency = """
        INSERT INTO Currency (name, starting_balance, symbol, amount_format, presentation_format, conversion_rate)
        VALUES               (?,    ?,                ?,      ?,             ?,                   ?);
    """.trimIndent()

    val insertCurrencyLocale = """
        INSERT INTO CurrencyLocale (id, locale, display_name_singular, display_name_plural, decimal)
        VALUES                     (?,  ?,      ?,                     ?,                   ?);
    """.trimIndent()

    val deleteCurrency = """
        DELETE FROM Currency
        WHERE name = ?;
    """.trimIndent()

    val getVaultBankAccountIds = """
        SELECT namespaced_key
        FROM NonPlayerAccount
        INNER JOIN VaultBankAccount on VaultBankAccount.account_id = NonPlayerAccount.id;
    """.trimIndent()

    val getPlayerAccountUuidAndNames = """
        SELECT PlayerAccount.player_uuid, Account.name
        FROM PlayerAccount
        INNER JOIN Account on Account.id = PlayerAccount.id;
    """.trimIndent()

    val getVaultUnlockedNonPlayerAccounts = """
        SELECT NonPlayerAccount.namespaced_key, Account.name
        FROM NonPlayerAccount
        INNER JOIN Account on Account.id = NonPlayerAccount.id
        WHERE Account.name LIKE '${StdKey.VU_NAMESPACE_FOR_STANDARD_ACCOUNTS}:%';
    """.trimIndent()

    val getSymbolForCurrency = """
        SELECT symbol
        FROM Currency
        WHERE name = ?;
    """.trimIndent()

    val getDecimalForCurrencyWithLocale = """
        SELECT decimal
        FROM CurrencyLocale
        INNER JOIN Currency ON Currency.id = CurrencyLocale.id
        WHERE Currency.name = ?;
    """.trimIndent()

    val getLocaleDecimalPairsForCurrency = """
        SELECT locale, decimal
        FROM CurrencyLocale
        INNER JOIN Currency ON Currency.id = CurrencyLocale.id
        WHERE Currency.name = ?;
    """.trimIndent()

    val getDisplayNamesForCurrencyWithLocale = """
        SELECT display_name_singular, display_name_plural
        FROM CurrencyLocale
        INNER JOIN Currency ON Currency.id = CurrencyLocale.id
        WHERE Currency.name = ?;
    """.trimIndent()

    val getStartingBalanceForCurrency = """
        SELECT starting_balance
        FROM Currency
        WHERE name = ?;
    """.trimIndent()

    val getConversionRateForCurrency = """
        SELECT conversion_rate
        FROM Currency
        WHERE name = ?;
    """.trimIndent()

    val getStringFormatsForCurrency = """
        SELECT amount_format, presentation_format
        FROM Currency
        WHERE name = ?;
    """.trimIndent()

}