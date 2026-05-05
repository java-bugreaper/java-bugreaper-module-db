package net.bugreaper.modules.db;

import net.bugreaper.modules.db.jdbc.JdbcExecutor;
import io.qameta.allure.Step;
import org.apache.commons.lang3.StringUtils;

/**
 * Class for interacting with MsSql
 *
 * <p>For one instance run recommended: {@code MsSQL db = MsSQL.getInstance()}</p>
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 * more info {@link JdbcExecutor}
 */
public class MsSQL extends JdbcExecutor<MsSQL> {

    private static MsSQL instance;

    private static final String GET_COLUMNS = """
            SELECT COLUMN_NAME
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA='%s'
                AND TABLE_NAME='%s'""";

    /**
     * Creates a new MsSQL database helper instance with the specified connection parameters.
     *
     * @param dbUrl    the JDBC connection URL for the MsSQL database
     * @param username the username used to authenticate with the database
     * @param passwd   the password used to authenticate with the database
     *
     * @throws IllegalArgumentException if any of the required parameters are null or empty
     */
    public MsSQL(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                GET_COLUMNS,
                DbTypes.MS_SQL.value());
    }

    /**
     * Returns the singleton instance of {@link MsSQL}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     * However, it may have performance overhead due to synchronization on every call.
     *
     * @return the singleton instance of {@link MsSQL}
     */
    public static synchronized MsSQL getInstance() {
        if (instance == null) {
            instance = new MsSQL();
        }

        return instance;
    }

    /**
     * Constructs MsSQL client configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * more info {@link JdbcExecutor#JdbcExecutor(String dbType, String getColumnsQuery)}
     */
    public MsSQL() {
        super(DbTypes.MS_SQL.value(), GET_COLUMNS);
    }

    @Override
    @Step("Rename table {tableName}")
    public void renameTable(String tableName) {
        jdbc.sqlExecute("EXEC sp_rename '" + tableName + "', '" + StringUtils.substringAfter(tableName, ".") + "_X_'");
    }

    @Override
    @Step("Rollback table {tableName}")
    public void rollbackTable(String tableName) {
        jdbc.sqlExecute("EXEC sp_rename '" + tableName + "_X_', '" + StringUtils.substringAfter(tableName, ".") + "'");
    }
}
