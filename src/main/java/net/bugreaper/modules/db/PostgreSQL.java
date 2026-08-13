package net.bugreaper.modules.db;

import io.qameta.allure.Step;
import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.jdbc.JdbcExecutor;
import org.apache.commons.lang3.StringUtils;

/**
 * PostgreSQL helper that provides a common API for operating with PostgreSQL.
 *
 * <p>It is recommended to use a single instance:
 * {@code PostgreSQL db = PostgreSQL.getInstance();}
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
public class PostgreSQL extends JdbcExecutor<PostgreSQL> {

    private static PostgreSQL instance;

    private static final String GET_COLUMNS = """
            SELECT COLUMN_NAME
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA='%s'
                AND TABLE_NAME='%s'""";

    /**
     * Creates a new PostgreSQL database helper instance with the specified connection parameters.
     *
     * @param dbUrl    the JDBC connection URL for the PostgreSQL database
     * @param username the username used to authenticate with the database
     * @param passwd   the password used to authenticate with the database
     *
     * @throws IllegalArgumentException if any of the required parameters are null or empty
     */
    public PostgreSQL(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                GET_COLUMNS,
                DbTypes.POSTGRE_SQL);
    }

    /**
     * Returns the instance of {@link PostgreSQL} with config builder {@link #PostgreSQL()}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     *
     * @return the shared instance of {@link PostgreSQL}
     * @see #PostgreSQL() config setup
     *
     * @throws IllegalArgumentException if the configuration contains invalid values
     */
    public static synchronized PostgreSQL getInstance() {
        if (instance == null) {
            instance = new PostgreSQL();
        }

        return instance;
    }

    /**
     * Constructs a PostgreSQL client using YAML configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * Example:
     * <p>{@code PostgreSQL db = new PostgreSQL();}</p>
     * <pre>
     * modules:
     *   db:
     *     postgresql:
     *       url: 'jdbc:postgresql://localhost:5434/public'
     *       username: user
     *       password: pass
     *       await: 1000 (optional)
     *       enchanted-report: true # (optional)
     * </pre>
     *
     * @see JdbcExecutor#JdbcExecutor(DbTypes dbType, String getColumnsQuery, String suffix) JdbcExecutor
     */
    public PostgreSQL() {
        super(DbTypes.POSTGRE_SQL, GET_COLUMNS, "");
    }

    /**
     * Truncate(clean) table and all connected by foreign keys tables
     *
     * @param tableName schema_name.table_name
     */
    @Step("(DB) Truncate table '{tableName}' CASCADE")
    public void truncateTableCascade(String tableName) {
        jdbc.sqlExecute("TRUNCATE TABLE " + tableName + " CASCADE");
    }

    @Override
    @Step("(DB) Rename table '{tableName}'")
    public void renameTable(String tableName) {
        jdbc.sqlExecute("ALTER TABLE " + StringUtils.substringAfter(tableName, ".") + " RENAME TO " + StringUtils.substringAfter(tableName, ".") + "_X_");
    }

    @Override
    @Step("(DB) Rollback renamed table '{tableName}'")
    public void rollbackTable(String tableName) {
        jdbc.sqlExecute("ALTER TABLE " + StringUtils.substringAfter(tableName, ".") + "_X_ RENAME TO " + StringUtils.substringAfter(tableName, "."));
    }

}
