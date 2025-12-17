package net.bugreaper.modules.db;

import net.bugreaper.modules.db.jdbc.JdbcExecutor;

/**
 * Class for interacting with MySQL
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 * more info {@link JdbcExecutor}
 */
public class MySQL extends JdbcExecutor<MySQL> {

    private static final String GET_COLUMNS = """
            SELECT COLUMN_NAME
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA='%s'
                AND TABLE_NAME='%s'""";

    public MySQL(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                GET_COLUMNS,
                DbTypes.MY_SQL.value());
    }

    /**
     * Constructs MysSQL client configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * more info {@link JdbcExecutor#JdbcExecutor(String dbType, String getColumnsQuery)}
     */
    public MySQL() {
        super(DbTypes.MY_SQL.value(), GET_COLUMNS);
    }

}
