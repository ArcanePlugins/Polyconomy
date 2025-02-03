/*
Available DBMSs:
    Flat-file:
        - SQLite
        - JSON
        - Yaml
    Remote:
        - MySQL/MariaDB
        - Postgres

SQLite will be available for ArcaneEconomy. However, SQLite has the least amount of features
of any of the supported database management systems. Therefore we need to base all of the
commands upon which features SQLite provides.
See: <https://www.sqlite.org/datatype3.html>
*/

/*
The player cache stores a map of each player's uuid and their last known username.
Each row has a timestamp to provide an insight into when this data was last verified.
E.g. the row will be invalidated when a week has passed.
Every time a player joins, it updates their username here.
*/
CREATE TABLE IF NOT EXISTS ArcEco_PlayerCache
(
    /* PlayerUUID, PlayerUsername, Timestamp */
    player_uuid
    VARCHAR
(
    32
) PRIMARY KEY,
    player_username VARCHAR
(
    48
) NOT NULL UNIQUE,
    cache_timestamp DATE NOT NULL
    );

/*
The currencies table stores a map of currency names against the
internal ID used in the database. This saves space because an INT
can be used instead of a VARCHAR(48) in the other tables referencing
a currency which in turn saves storage space.
*/
CREATE TABLE IF NOT EXISTS ArcEco_Currencies
(
    /* CurrencyName, CurrencyId */
    currency_id
    INT
    PRIMARY
    KEY,
    currency_name
    VARCHAR
(
    48
) NOT NULL UNIQUE
    )

/*
This stores a map of player UUIDs to their internal database ID.
For the same reason as currencies being mapped to IDs - saves storage space.
Also gives an easy reference to the existence of an account.
*/
CREATE TABLE IF NOT EXISTS ArcEco_PlayerAccounts
(
    /* AccountId, PlayerUUID */
    account_id
    INT
    PRIMARY
    KEY,
    player_uuid
    VARCHAR
(
    32
) NOT NULL UNIQUE
    );

/*
This stores all of the player account balances.
There are no primary or unique keys because each
row denotes a balance for a specific currency
for a specific account.
*/
CREATE TABLE IF NOT EXISTS ArcEco_PlayerAccountBalances
(
    /* AccountId, CurrencyId, Balance */
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
    5
) NOT NULL,
    FOREIGN KEY
(
    account_id
) REFERENCES ArcEco_PlayerAccounts
(
    account_id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    currency_id
) REFERENCES ArcEco_Currencies
(
    currency_id
)
  ON DELETE CASCADE
    );

/*
This stores a map of non-player account names to their internal database ID.
*/
CREATE TABLE IF NOT EXISTS ArcEco_NonPlayerAccounts
(
    /* AccountId, AccountName */
    account_id
    INT
    PRIMARY
    KEY,
    account_name
    VARCHAR
(
    48
) NOT NULL UNIQUE
    );

/*
This stores all of the non-player account balances.
There are no primary or unique keys because each
row denotes a balance for a specific currency
for a specific account.
*/
CREATE TABLE IF NOT EXISTS ArcEco_NonPlayerAccountBalances
(
    /* AccountId, CurrencyId, Balance */
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
    5
) NOT NULL,
    FOREIGN KEY
(
    account_id
) REFERENCES ArcEco_NonPlayerAccounts
(
    account_id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    currency_id
) REFERENCES ArcEco_Currencies
(
    currency_id
)
  ON DELETE CASCADE
    );


/*
Each account member of a NonPlayerAccount has a row in this table.
No keys are primary/unique due to this.
The member_uuid is the uuid of the player that is the member of the account.
permissions is a String array in String format, like so:
BALANCE,WITHDRAW,DEPOSIT,MODIFY_PERMISSIONS
*/
CREATE TABLE IF NOT EXISTS ArcEco_NonPlayerAccountMembers
(
    /* AccountId, MemberId, Permissions */
    account_id
    INT
    NOT
    NULL,
    member_uuid
    VARCHAR
(
    32
) NOT NULL,
    permissions VARCHAR
(
    256
) NOT NULL,
    FOREIGN KEY
(
    account_id
) REFERENCES ArcEco_NonPlayerAccounts
(
    account_id
) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS ArcEco_PlayerAccountTransactions
(
    /* TransactionId, AccountId, Initiator, Timestamp, Type, Reason, Amount, Importance */
);

CREATE TABLE IF NOT EXISTS ArcEco_NonPlayerAccountTransactions
(
    /* TransactionId, AccountId, Initiator, Timestamp, Type, Reason, Amount, Importance */
);

CREATE TABLE IF NOT EXISTS ArcEco_CurrentIds
(
    /* TableNamesEnum, Value */
    table_name
    VARCHAR
(
    64
) PRIMARY KEY,
    current_id INT NOT NULL
    );

/* Example Commands  */

REPLACE
INTO PlayerCache VALUES('abc-def-ghi-jkl', 'Notch', 'no clue');
INSERT INTO PlayerCache
VALUES ('abc-def-ghi-jkl', 'lokka30', 'no clue');

/* Update row(s) */
UPDATE PlayerCache
SET player_username = 'Notch'
WHERE player_uuid = 'abc-def-ghi-jkl';

/* Select specific columns */
SELECT player_uuid, player_username, cache_timestamp
FROM PlayerCache;

/* Delete account */
DELETE
FROM NonPlayerAccounts
WHERE account_name = 'TownyAccount';

/* Baltop */
SELECT *
FROM PlayerAccountBalances
WHERE currency_id = 'dollars'
ORDER BY balance;

/* Server balance */
SELECT SUM(balance)
FROM ArcEco_PlayerAccountBalances
WHERE currency_id = 'dollars';

/*
Nested Queries
Get all clients from the branch where the branch manager's id is 102
*/
SELECT client.client_name
FROM client
WHERE client.branch_id = (SELECT branch.branch_id
                          FROM branch
                          WHERE branch.mgr_id = 102);