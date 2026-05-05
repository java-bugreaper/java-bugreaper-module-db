package net.bugreaper.modules.db;

import net.bugreaper.modules.db.jdbc.JdbcExecutor;

/**
 * Class for interacting with MySQL
 *
 * <p>For one instance run recommended: {@code MySQL db = MySQL.getInstance()}</p>
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 * more info {@link JdbcExecutor}
 */
public class MySQL extends JdbcExecutor<MySQL> {

    private static MySQL instance;

    private static final String GET_COLUMNS = """
            SELECT COLUMN_NAME
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA='%s'
                AND TABLE_NAME='%s'""";

    /**
     * Creates a new MySQL database helper instance with the specified connection parameters.
     *
     * @param dbUrl    the JDBC connection URL for the MySQL database
     * @param username the username used to authenticate with the database
     * @param passwd   the password used to authenticate with the database
     *
     * @throws IllegalArgumentException if any of the required parameters are null or empty
     */
    public MySQL(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                GET_COLUMNS,
                DbTypes.MY_SQL.value());
    }

    /**
     * Returns the singleton instance of {@link MySQL}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     * However, it may have performance overhead due to synchronization on every call.
     *
     * @return the singleton instance of {@link MySQL}
     */
    public static synchronized MySQL getInstance() {
        if (instance == null) {
            instance = new MySQL();
        }

        return instance;
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
