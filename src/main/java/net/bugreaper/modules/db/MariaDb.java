package net.bugreaper.modules.db;

import net.bugreaper.modules.db.jdbc.JdbcExecutor;


/**
 * Class for interacting with MariaDB
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 * more info {@link JdbcExecutor}
 */
public class MariaDb extends JdbcExecutor<MariaDb> {

    private static final String GET_COLUMNS = """
            SELECT COLUMN_NAME
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA='%s'
                AND TABLE_NAME='%s'""";

    public MariaDb(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                GET_COLUMNS,
                DbTypes.MARIA_DB.value());
    }

    /**
     * Constructs MariaDB client configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * more info {@link JdbcExecutor#JdbcExecutor(String dbType, String getColumnsQuery)}
     */
    public MariaDb() {
        super(DbTypes.MARIA_DB.value(), GET_COLUMNS);
    }
}
