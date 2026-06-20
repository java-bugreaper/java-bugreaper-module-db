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
     * Returns the singleton instance of {@link MySQL} with config builder {@link #MySQL()}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     *
     * @return the singleton instance of {@link MySQL}
     * @see #MySQL() config setup
     */
    public static synchronized MySQL getInstance() {
        if (instance == null) {
            instance = new MySQL();
        }

        return instance;
    }

    /**
     * Constructs MySQL client configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * Example:
     * <p>{@code MySQL db = new MySQL();}</p>
     * <pre>
     * mysql:
     *   url: 'jdbc:mysql://localhost:3308'
     *   username: user
     *   password: pass
     *   await: 1000 (optional)
     * </pre>
     *
     * @see JdbcExecutor#JdbcExecutor(String dbType, String getColumnsQuery) JdbcExecutor
     */
    public MySQL() {
        super(DbTypes.MY_SQL.value(), GET_COLUMNS);
    }

}
