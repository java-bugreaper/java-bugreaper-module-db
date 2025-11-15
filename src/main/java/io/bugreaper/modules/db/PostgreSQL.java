package io.bugreaper.modules.db;

import io.qameta.allure.Step;
import io.bugreaper.modules.db.jdbc.JdbcExecutor;
import org.apache.commons.lang3.StringUtils;

/**
 * Class for interacting with PostgreSQL
 *
 * <p>Contains methods for:</p>
 * asserts with await, change data (create, update, clean)<br>
 *  * more info {@link JdbcExecutor},
 */
public class PostgreSQL extends JdbcExecutor<PostgreSQL> {


    public PostgreSQL(String dbUrl, String username, String passwd) {
        super(dbUrl,
                username,
                passwd,
                """
                        SELECT COLUMN_NAME
                        FROM INFORMATION_SCHEMA.COLUMNS
                        WHERE TABLE_SCHEMA='%s'
                            AND TABLE_NAME='%s'""");
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
