package net.bugreaper.modules.db;

import net.bugreaper.modules.db.jdbc.JdbcExecutor;


/**
 * Class for interacting with custom DB (that not supported by default)
 *
 * <p>For one instance run recommended: {@code CustomDb db = CustomDb.getInstance()}</p>
 *
 * Supports only basic features:
 * <ul>
 *   <li>Get all data for allure on failed not supported</li>
 *   <li>Get data all data by conditions not supported</li>
 *   <li>Get data all data from table not supported</li>
 *   <li>Specific conditions(JSON_*, REGEX) not supported (only basic conditions)</li>
 * </ul>
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 * more info {@link JdbcExecutor}
 */
public class CustomDb extends JdbcExecutor<CustomDb> {

    private static CustomDb instance;

    private static final String GET_COLUMNS = "Get all data for allure not supported for CustomDb";


    /**
     * Creates a new Custom database helper instance with the specified connection parameters.
     *
     * @param dbUrl    the JDBC connection URL for the Custom database
     * @param username the username used to authenticate with the database
     * @param passwd   the password used to authenticate with the database
     *
     * @throws IllegalArgumentException if any of the required parameters are null or empty
     */
    public CustomDb(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                GET_COLUMNS,
                DbTypes.MARIA_DB.value());
    }

    /**
     * Returns the singleton instance of {@link CustomDb}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     * However, it may have performance overhead due to synchronization on every call.
     *
     * @return the singleton instance of {@link CustomDb}
     */
    public static synchronized CustomDb getInstance() {
        if (instance == null) {
            instance = new CustomDb();
        }

        return instance;
    }

    /**
     * Constructs custom database (that not yet supported) client configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * Example:
     * <p>{@code CustomDb db = new CustomDb();}</p>
     * <pre>
     * custom-db: # reserved type
     *   url: jdbc:mysql://localhost:3308
     *   username: mysql_user
     *   password: mysql_pass
     *   await: 600 (optional)
     * </pre>
     * more info {@link JdbcExecutor#JdbcExecutor(String dbType, String getColumnsQuery)}
     */
    public CustomDb() {
        super(DbTypes.CUSTOM_DB.value(), GET_COLUMNS);
    }


}
