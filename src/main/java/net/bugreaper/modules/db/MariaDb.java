package net.bugreaper.modules.db;

import net.bugreaper.modules.db.jdbc.JdbcExecutor;


/**
 * Class for interacting with MariaDB
 *
 * <p>For one instance run recommended: {@code MariaDb db = MariaDb.getInstance()}</p>
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 * more info {@link JdbcExecutor}
 */
public class MariaDb extends JdbcExecutor<MariaDb> {

    private static MariaDb instance;

    private static final String GET_COLUMNS = """
            SELECT COLUMN_NAME
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA='%s'
                AND TABLE_NAME='%s'""";

    /**
     * Creates a new MariaDb database helper instance with the specified connection parameters.
     *
     * @param dbUrl    the JDBC connection URL for the MariaDb database
     * @param username the username used to authenticate with the database
     * @param passwd   the password used to authenticate with the database
     *
     * @throws IllegalArgumentException if any of the required parameters are null or empty
     */
    public MariaDb(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                GET_COLUMNS,
                DbTypes.MARIA_DB.value());
    }

    /**
     * Returns the singleton instance of {@link MariaDb}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     * However, it may have performance overhead due to synchronization on every call.
     *
     * @return the singleton instance of {@link MariaDb}
     */
    public static synchronized MariaDb getInstance() {
        if (instance == null) {
            instance = new MariaDb();
        }

        return instance;
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
