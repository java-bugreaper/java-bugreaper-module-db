package net.bugreaper.modules.db;

import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.jdbc.JdbcExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * CustomDb helper that provides a common API for operating with SQL-database that is not yet fully supported.
 *
 * <p>It is recommended to use a single instance:
 * {@code CustomDb db = CustomDb.getInstance();}
 * </p>
 *
 * Supports only basic features:
 * <ul>
 *   <li>Get all data for allure on failed not supported</li>
 *   <li>Get data all data by conditions not supported</li>
 *   <li>Get data all data from table not supported</li>
 *   <li>Specific conditions(JSON_*, REGEX) not supported (only basic conditions)</li>
 * </ul>
 *
 * <p>Provides:</p>
 * <ul>
 *     <li>Assertions with await support</li>
 *     <li>Data manipulation operations such as create, update, and clean</li>
 * </ul>
 *
 * <p>For more information, see {@link JdbcExecutor}.</p>
 */
public class CustomDb extends JdbcExecutor<CustomDb> {

    private static CustomDb instance;
    private static final Map<String, CustomDb> INSTANCES = new ConcurrentHashMap<>();

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
                DbTypes.CUSTOM_DB);
    }

    /**
     * Returns the instance of {@link CustomDb} with config builder {@link #CustomDb()}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     *
     * @return the shared instance of {@link CustomDb}
     * @see #CustomDb() config setup
     *
     * @throws IllegalArgumentException if the configuration contains invalid values
     */
    public static synchronized CustomDb getInstance() {
        if (instance == null) {
            instance = new CustomDb();
        }

        return instance;
    }

    /**
     * Returns a unique {@link CustomDb} helper instance for the specified suffix using YAML configuration.
     *
     * <p>This method implements the Multiton pattern. A separate singleton
     * instance is created for each CustomDb. Subsequent calls with the
     * same suffix return the already created instance.</p>
     *
     * @return unique {@link CustomDb} instance associated with the specified
     *         Redis data type
     * @see #CustomDb(String) config setup
     *
     * @throws IllegalArgumentException if the configuration contains invalid values
     */
    public static CustomDb getInstance(String suffix) {
        return INSTANCES.computeIfAbsent(
                suffix,
                CustomDb::new
        );
    }


    /**
     * Constructs a CustomDb client for a database that is not yet fully supported, using YAML configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * Example:
     * <p>{@code CustomDb db = new CustomDb();}</p>
     * <pre>
     * modules:
     *   db:
     *     custom-db: # reserved type
     *       url: 'jdbc:mysql://localhost:3308'
     *       username: user
     *       password: pass
     *       await: 1000 (optional)
     *       enchanted-report: true # (optional)
     * </pre>
     *
     * @see JdbcExecutor#JdbcExecutor(DbTypes dbType, String getColumnsQuery, String suffix) JdbcExecutor
     */
    public CustomDb() {
        super(DbTypes.CUSTOM_DB, GET_COLUMNS, "");
    }

    /**
     * Constructs a CustomDb client for a database that is not yet fully supported, using YAML configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * Example:
     * <p>{@code CustomDb db = new CustomDb("-mydb");}</p>
     * <pre>
     * modules:
     *   db:
     *     custom-db-mydb: # reserved type
     *       url: 'jdbc:mysql://localhost:3308'
     *       username: user
     *       password: pass
     *       await: 1000 (optional)
     *       enchanted-report: true # (optional)
     * </pre>
     *
     * @param suffix concatenation of CustomDb client (example CustomDb("-mydb") keys will be "modules.db.custom-db<b>-mydb</b>")
     * @see JdbcExecutor#JdbcExecutor(DbTypes dbType, String getColumnsQuery, String suffix) JdbcExecutor
     */
    public CustomDb(String suffix) {
        super(DbTypes.CUSTOM_DB, GET_COLUMNS, suffix);
    }
}
