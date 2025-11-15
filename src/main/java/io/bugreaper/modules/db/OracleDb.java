package io.bugreaper.modules.db;

import io.qameta.allure.Step;
import io.bugreaper.modules.db.jdbc.JdbcExecutor;
import org.apache.commons.lang3.StringUtils;


/**
 * Class for interacting with OracleDb
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 * more info {@link JdbcExecutor},
 */
public final class OracleDb extends JdbcExecutor<OracleDb> {

    public OracleDb(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                """
                        SELECT
                        column_name as COLUMN_NAME
                        FROM
                        all_tab_columns
                        WHERE
                        owner = '%s'
                        AND
                        table_name = '%s'""");
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
