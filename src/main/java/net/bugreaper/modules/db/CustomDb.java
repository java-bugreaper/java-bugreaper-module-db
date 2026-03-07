package net.bugreaper.modules.db;

import net.bugreaper.modules.db.jdbc.JdbcExecutor;


/**
 * Class for interacting with custom DB (that not supported by default)
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

    private static final String GET_COLUMNS = "Get all data for allure not supported for CustomDb";


    public CustomDb(String dbUrl, String username, String passwd) {
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
     * Example:
     * <p>CustomDb db = new CustomDb("custom-db");</p>
     * <pre>
     * custom-db:
     *   url: jdbc:mysql://localhost:3308
     *   username: mysql_user
     *   password: mysql_pass
     *   await: 600 (optional)
     * </pre>
     * more info {@link JdbcExecutor#JdbcExecutor(String dbType, String getColumnsQuery)}
     * @param dbType key for config
     */
    public CustomDb(String dbType) {
        super(dbType, GET_COLUMNS);
    }


}
