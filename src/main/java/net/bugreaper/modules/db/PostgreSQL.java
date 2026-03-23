package net.bugreaper.modules.db;

import io.qameta.allure.Step;
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

    public PostgreSQL(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                GET_COLUMNS,
                DbTypes.POSTGRE_SQL.value());
    }

    /**
     * Run {@link #PostgreSQL()} from config in one instance
     */
    public static PostgreSQL getInstance() {
        if (instance == null) {
            instance = new PostgreSQL();
        }

        return instance;
    }

    /**
     * Constructs OracleDb client configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * more info {@link JdbcExecutor#JdbcExecutor(String dbType, String getColumnsQuery)}
     */
    public PostgreSQL() {
        super(DbTypes.POSTGRE_SQL.value(), GET_COLUMNS);
    }

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
