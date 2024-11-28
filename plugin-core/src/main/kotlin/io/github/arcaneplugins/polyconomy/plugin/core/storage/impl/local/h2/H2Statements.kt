package io.github.arcaneplugins.polyconomy.plugin.core.storage.impl.local.h2

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
                    id          INT             NOT NULL,
                    player_uuid BINARY(16)      NOT NULL UNIQUE,
                    PRIMARY KEY (id),
                    FOREIGN KEY id REFERENCES Account.id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS NonPlayerAccount (
                    id              INT             NOT NULL,
                    namespaced_key  VARCHAR(255)    NOT NULL UNIQUE,
                    PRIMARY KEY (id),
                    FOREIGN KEY id REFERENCES Account.id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS NonPlayerAccountMember (
                    account_id                  INT             NOT NULL,
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
                    account_id      INT             NOT NULL,
                    owner_string    VARCHAR(255)    NOT NULL,
                    owner_uuid      BINARY(16)      NOT NULL,
                    PRIMARY KEY (account_id),
                    FOREIGN KEY account_id REFERENCES NonPlayerAccount.id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS VaultBankAccountNonPlayerMember (
                    account_id      INT             NOT NULL,
                    member_id_str   VARCHAR(255)    NOT NULL,
                    PRIMARY KEY (account_id, member_id_str),
                    FOREIGN KEY account_id REFERENCES VaultBankAccount.account_id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS Currency (
                    id                  IDENTITY        NOT NULL,
                    name                VARCHAR(255)    NOT NULL UNIQUE,
                    enabled             BOOLEAN         NOT NULL,
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
                    id                      INT             NOT NULL,
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
                    account_id              INT             NOT NULL,
                    currency_id             INT             NOT NULL,
                    amount                  DECIMAL(18, 4)  NOT NULL,
                    PRIMARY KEY (account_id, currency_id),
                    FOREIGN KEY account_id REFERENCES Account.id,
                    FOREIGN KEY currency_id REFERENCES Currency.id
                );
            """.trimIndent(),

        """
                CREATE TABLE IF NOT EXISTS AccountTransaction (
                    id                      IDENTITY        NOT NULL,
                    account_id              INT             NOT NULL,
                    amount                  DECIMAL(18, 4)  NOT NULL,
                    currency_id             INT             NOT NULL,
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

}