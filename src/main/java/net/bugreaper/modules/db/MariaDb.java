package net.bugreaper.modules.db;

import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.jdbc.JdbcExecutor;


/**
 * MariaDb helper that provides a common API for operating with MariaDb.
 *
 * <p>It is recommended to use a single instance:
 * {@code MariaDb db = MariaDb.getInstance();}
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
                DbTypes.MARIA_DB);
    }

    /**
     * Returns the instance of {@link MariaDb} with config builder {@link #MariaDb()}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     *
     * @return the shared instance of {@link MariaDb}
     * @see #MariaDb() config setup
     *
     * @throws IllegalArgumentException if the configuration contains invalid values
     */
    public static synchronized MariaDb getInstance() {
        if (instance == null) {
            instance = new MariaDb();
        }

        return instance;
    }

    /**
     * Constructs a MariaDB client using YAML configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * Example:
     * <p>{@code MariaDb db = new MariaDb();}</p>
     * <pre>
     * modules:
     *   db:
     *     mariadb:
     *       url: 'jdbc:mariadb://localhost:3308'
     *       username: user
     *       password: pass
     *       await: 1000 (optional)
     *       enchanted-report: true # (optional)
     * </pre>
     *
     * @see JdbcExecutor#JdbcExecutor(DbTypes dbType, String getColumnsQuery, String suffix) JdbcExecutor
     */
    public MariaDb() {
        super(DbTypes.MARIA_DB, GET_COLUMNS, "");
    }
}
