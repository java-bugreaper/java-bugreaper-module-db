package io.bugreaper.modules.db;

import io.bugreaper.modules.db.jdbc.JdbcExecutor;
import io.qameta.allure.Step;
import org.apache.commons.lang3.StringUtils;

/**
 * Class for interacting with MsSql
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 *  * more info {@link JdbcExecutor},
 */
public class MsSQL extends JdbcExecutor<MsSQL> {


    public MsSQL(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                """
                        SELECT COLUMN_NAME
                        FROM INFORMATION_SCHEMA.COLUMNS
                        WHERE TABLE_SCHEMA='%s'
                            AND TABLE_NAME='%s'""");
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
