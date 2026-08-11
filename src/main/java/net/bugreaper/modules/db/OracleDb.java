package net.bugreaper.modules.db;

import io.qameta.allure.Step;
import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.jdbc.JdbcExecutor;
import org.apache.commons.lang3.StringUtils;


/**
 * OracleDb helper that provides a common API for operating with OracleDb.
 *
 * <p>It is recommended to use a single instance:
 * {@code OracleDb db = OracleDb.getInstance();}
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
public final class OracleDb extends JdbcExecutor<OracleDb> {

    private static OracleDb instance;

    private static final String GET_COLUMNS = """
            SELECT
            column_name as COLUMN_NAME
            FROM
            all_tab_columns
            WHERE
            owner = '%s'
            AND
            table_name = '%s'""";


    /**
     * Creates a new Oracle database helper instance with the specified connection parameters.
     *
     * @param dbUrl    the JDBC connection URL for the Oracle database
     * @param username the username used to authenticate with the database
     * @param passwd   the password used to authenticate with the database
     *
     * @throws IllegalArgumentException if any of the required parameters are null or empty
     */
    public OracleDb(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                GET_COLUMNS,
                DbTypes.ORACLE_DB);
    }

    /**
     * Returns the instance of {@link OracleDb} with config builder {@link #OracleDb()}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     *
     * @return the shared instance of {@link OracleDb}
     * @see #OracleDb() config setup
     *
     * @throws IllegalArgumentException if the configuration contains invalid values
     */
    public static synchronized OracleDb getInstance() {
        if (instance == null) {
            instance = new OracleDb();
        }

        return instance;
    }

    /**
     * Constructs an OracleDb client using YAML configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * Example:
     * <p>{@code OracleDb db = new OracleDb();}</p>
     * <pre>
     * oracle:
     *   url: 'jdbc:oracle:thin:@localhost:1521/xepdb1'
     *   username: user
     *   password: pass
     *   await: 1000 (optional)
     * </pre>
     *
     * @see JdbcExecutor#JdbcExecutor(DbTypes dbType, String getColumnsQuery) JdbcExecutor
     */
    public OracleDb() {
        super(DbTypes.ORACLE_DB, GET_COLUMNS);
    }

    @Override
    @Step("(DB) Rename table '{tableName}'")
    public void renameTable(String tableName) {
        jdbc.sqlExecute("ALTER TABLE " + tableName + " RENAME TO " + StringUtils.substringAfter(tableName, ".") + "_X_");
    }

    @Override
    @Step("(DB) Rollback renamed table '{tableName}'")
    public void rollbackTable(String tableName) {
        jdbc.sqlExecute("ALTER TABLE " + tableName + "_X_ RENAME TO " + StringUtils.substringAfter(tableName, "."));
    }

}
