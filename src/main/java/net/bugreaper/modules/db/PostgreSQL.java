package net.bugreaper.modules.db;

import io.qameta.allure.Step;
import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.jdbc.JdbcExecutor;
import org.apache.commons.lang3.StringUtils;

/**
 * Class for interacting with PostgreSQL
 *
 * <p>For one instance run recommended: {@code PostgreSQL db = PostgreSQL.getInstance()}</p>
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 * more info {@link JdbcExecutor}
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
     * Returns the singleton instance of {@link PostgreSQL} with config builder {@link #PostgreSQL()}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     *
     * @return the singleton instance of {@link PostgreSQL}
     * @see #PostgreSQL() config setup
     */
    public static synchronized PostgreSQL getInstance() {
        if (instance == null) {
            instance = new PostgreSQL();
        }

        return instance;
    }

    /**
     * Constructs OracleDb client configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * Example:
     * <p>{@code PostgreSQL db = new PostgreSQL();}</p>
     * <pre>
     * postgresql:
     *   url: 'jdbc:postgresql://localhost:5434/public'
     *   username: user
     *   password: pass
     *   await: 1000 (optional)
     * </pre>
     *
     * @see JdbcExecutor#JdbcExecutor(DbTypes dbType, String getColumnsQuery) JdbcExecutor
     */
    public PostgreSQL() {
        super(DbTypes.POSTGRE_SQL, GET_COLUMNS);
    }

    /**
     * Truncate(clean) table and all connected by foreign keys tables
     *
     * @param tableName schema_name.table_name
     */
    @Step("Truncate table {tableName} CASCADE")
    public void truncateTableCascade(String tableName) {
        jdbc.sqlExecute("TRUNCATE TABLE " + tableName + " CASCADE");
    }

    @Override
    @Step("Rename table {tableName}")
    public void renameTable(String tableName) {
        jdbc.sqlExecute("ALTER TABLE " + StringUtils.substringAfter(tableName, ".") + " RENAME TO " + StringUtils.substringAfter(tableName, ".") + "_X_");
    }

    @Override
    @Step("Rollback table {tableName}")
    public void rollbackTable(String tableName) {
        jdbc.sqlExecute("ALTER TABLE " + StringUtils.substringAfter(tableName, ".") + "_X_ RENAME TO " + StringUtils.substringAfter(tableName, "."));
    }

}
