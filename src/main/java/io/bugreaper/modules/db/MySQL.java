package io.bugreaper.modules.db;

import io.bugreaper.modules.db.jdbc.JdbcExecutor;

/**
 * Class for interacting with MySQL
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 *  * more info {@link JdbcExecutor},
 */
public class MySQL extends JdbcExecutor<MySQL> {


    public MySQL(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                """
                        SELECT COLUMN_NAME
                        FROM INFORMATION_SCHEMA.COLUMNS
                        WHERE TABLE_SCHEMA='%s'
                            AND TABLE_NAME='%s'""");
    }

}
