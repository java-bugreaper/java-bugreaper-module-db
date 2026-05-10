package net.bugreaper.modules.db;

import io.qameta.allure.Step;
import net.bugreaper.modules.db.jdbc.JdbcExecutor;
import org.apache.commons.lang3.StringUtils;


/**
 * Class for interacting with OracleDb
 *
 * <p>For one instance run recommended: {@code Kafka OracleDb db = OracleDb.getInstance()}</p>
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 * more info {@link JdbcExecutor}
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
                DbTypes.ORACLE_DB.value());
    }

    /**
     * Returns the singleton instance of {@link OracleDb} with config builder {@link #OracleDb()}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     *
     * @return the singleton instance of {@link OracleDb}
     */
    public static synchronized OracleDb getInstance() {
        if (instance == null) {
            instance = new OracleDb();
        }

        return instance;
    }

    /**
     * Constructs OracleDb client configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     * more info {@link JdbcExecutor#JdbcExecutor(String dbType, String getColumnsQuery)}
     */
    public OracleDb() {
        super(DbTypes.ORACLE_DB.value(), GET_COLUMNS);
    }

    @Override
    @Step("Rename table {tableName}")
    public void renameTable(String tableName) {
        jdbc.sqlExecute("ALTER TABLE " + tableName + " RENAME TO " + StringUtils.substringAfter(tableName, ".") + "_X_");
    }

    @Override
    @Step("Rollback table {tableName}")
    public void rollbackTable(String tableName) {
        jdbc.sqlExecute("ALTER TABLE " + tableName + "_X_ RENAME TO " + StringUtils.substringAfter(tableName, "."));
    }

}
