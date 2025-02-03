/*
 * Unsure if offline-mode servers allow  >16 chars or not. may need to kick those players.
 */
CREATE TABLE IF NOT EXISTS arceco_player_usernames
(
    uuid
    CHAR
(
    36
) NOT NULL PRIMARY KEY,
    username VARCHAR
(
    16
) NOT NULL UNIQUE,
    cache_date DATE NOT NULL
    );

CREATE TABLE IF NOT EXISTS arceco_accounts
(
    id
    INT
    NOT
    NULL
    PRIMARY
    KEY,
    type
    ENUM
(
    'Player',
    'NonPlayer'
) NOT NULL
    )

CREATE TABLE IF NOT EXISTS arceco_player_accounts
(
    account_id
    INT
    NOT
    NULL
    PRIMARY
    KEY,
    uuid
    CHAR
(
    36
) NOT NULL UNIQUE,
    FOREIGN KEY
(
    account_id
) REFERENCES arceco_accounts
(
    id
) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS arceco_non_player_accounts
(
    account_id
    INT
    NOT
    NULL
    PRIMARY
    KEY,

    /* Treasury needs to impose a character limit on these names. */
    name
    VARCHAR
(
    48
) NOT NULL UNIQUE,
    FOREIGN KEY
(
    account_id
) REFERENCES arceco_accounts
(
    id
) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS arcoeco_account_balances
(
    account_id
    INT
    NOT
    NULL,
    currency_id
    INT
    NOT
    NULL,
    balance
    DECIMAL
(
    38,
    4
) NOT NULL,
    FOREIGN KEY
(
    account_id
) REFERENCES arceco_accounts
(
    id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    currency_id
) REFERENCES arceco_currencies
(
    id
)
  ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS arceco_account_members
(
    account_id
    INT
    NOT
    NULL,
    member_uuid
    VARCHAR
(
    36
) NOT NULL,
    permission ENUM
(
    'BALANCE',
    'WITHDRAW',
    'DEPOSIT',
    'MODIFY_PERMISSIONS'
) NOT NULL,
    FOREIGN KEY
(
    account_id
) REFERENCES arceco_accounts
(
    id
) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS arceco_transactions
(
    id
    INT
    NOT
    NULL
    PRIMARY
    KEY,
    account_id
    INT
    NOT
    NULL,
    initiator_type
    ENUM
(
    'PLAYER',
    'SERVER',
    'PLUGIN'
) NOT NULL,
    initiator_data VARCHAR
(
    36
) NOT NULL,
    sent_date DATE NOT NULL,
    reason VARCHAR
(
    72
),
    type ENUM
(
    'DEPOSIT',
    'WITHDRAWAL'
) NOT NULL,
    amount DECIMAL
(
    38,
    4
) NOT NULL,
    currency_id INT NOT NULL,
    importance ENUM
(
    'LOW',
    'NORMAL',
    'HIGH'
) NOT NULL,
    FOREIGN KEY
(
    account_id
) REFERENCES arceco_accounts
(
    id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    currency_id
) REFERENCES arceco_currencies
(
    id
)
  ON DELETE CASCADE
    );

/*
 * This table stores a list of available currencies.
 * The database does not store the details of the currencies, it only stores them as to associate
 * various currencies with their own unique identifiers (which are used by other tables).
 */
CREATE TABLE IF NOT EXISTS arceco_currencies
(

    /* Currency's identifier (ID). */
    currency_id
    INT
    NOT
    NULL
    PRIMARY
    KEY,

    /* Currency's name, e.g. 'Dollars'. */
    currency_name
    VARCHAR
(
    48
) NOT NULL UNIQUE
    );