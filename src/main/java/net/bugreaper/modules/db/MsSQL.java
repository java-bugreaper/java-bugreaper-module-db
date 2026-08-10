package net.bugreaper.modules.db;

import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.jdbc.JdbcExecutor;
import io.qameta.allure.Step;
import org.apache.commons.lang3.StringUtils;

/**
 * MsSQL helper that provides a common API for operating with MsSQL.
 *
 * <p>It is recommended to use a single instance:
 * {@code MsSQL db = MsSQL.getInstance();}
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
                DbTypes.MS_SQL);
    }

    /**
     * Returns the instance of {@link MsSQL} with config builder {@link #MsSQL()}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     *
     * @return the shared instance of {@link MsSQL}
     * @see #MsSQL() config setup
     *
     * @throws IllegalArgumentException if the configuration contains invalid values
     */
    public static synchronized MsSQL getInstance() {
        if (instance == null) {
            instance = new MsSQL();
        }

        return instance;
    }

    /**
     * Constructs a MsSQL client using YAML configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * Example:
     * <p>{@code MsSQL db = new MsSQL();}</p>
     * <pre>
     * sqlserver:
     *   url: 'jdbc:sqlserver://localhost:1435;encrypt=false'
     *   username: user
     *   password: pass
     *   await: 1000 (optional)
     * </pre>
     *
     * @see JdbcExecutor#JdbcExecutor(DbTypes dbType, String getColumnsQuery) JdbcExecutor
     */
    public MsSQL() {
        super(DbTypes.MS_SQL, GET_COLUMNS);
    }

    @Override
    @Step("(DB) Rename table '{tableName}'")
    public void renameTable(String tableName) {
        jdbc.sqlExecute("EXEC sp_rename '" + tableName + "', '" + StringUtils.substringAfter(tableName, ".") + "_X_'");
    }

    @Override
    @Step("(DB) Rollback renamed table '{tableName}'")
    public void rollbackTable(String tableName) {
        jdbc.sqlExecute("EXEC sp_rename '" + tableName + "_X_', '" + StringUtils.substringAfter(tableName, ".") + "'");
    }
}
