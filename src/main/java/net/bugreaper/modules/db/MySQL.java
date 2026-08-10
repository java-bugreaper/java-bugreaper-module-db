package net.bugreaper.modules.db;

import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.jdbc.JdbcExecutor;

/**
 * MySQL helper that provides a common API for operating with MySQL.
 *
 * <p>It is recommended to use a single instance:
 * {@code MySQL db = MySQL.getInstance();}
 * </p>
 *
 * <p>Provides:</p>
 * <ul>
 *     <li>Assertions with await support</li>
 *     <li>Data manipulation operations such as create, update, and clean</li>
 * </ul>
 *
 * <p>For more information, see {@link JdbcExecutor}.</p>
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
                DbTypes.MY_SQL);
    }

    /**
     * Returns the singleton instance of {@link MySQL} with config builder {@link #MySQL()}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     *
     * @return the singleton instance of {@link MySQL}
     * @see #MySQL() config setup
     *
     * @throws IllegalArgumentException if the configuration contains invalid values
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
     * @see JdbcExecutor#JdbcExecutor(DbTypes dbType, String getColumnsQuery) JdbcExecutor
     */
    public MySQL() {
        super(DbTypes.MY_SQL, GET_COLUMNS);
    }

}
