package io.bugreaper.modules.db.jdbc;

import io.bugreaper.modules.db.jdbc.interfaces.Setters;
import io.bugreaper.modules.db.jdbc.interfaces.SqlGet;
import io.bugreaper.modules.db.jdbc.interfaces.SqlPost;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static io.bugreaper.core.allurereporter.AllureReporter.attachFromFileNoStep;
import static io.bugreaper.core.filereaders.FileReader.readJsonFromFile;
import static io.bugreaper.core.filereaders.FileReader.readTextFromFile;
import static io.bugreaper.modules.db.mapper.JsonToArrayMapper.jsonToObjectArray;

/**
 * Parent class for interacting with Databases
 *
 * <p><b>Asserts:</b>
 * {@link JdbcExecutor#seeRowsCountInTableExactly(String, Object[][], int)}
 * {@link JdbcExecutor#seeRowsCountInTableExactly(String, String, int)}
 * {@link JdbcExecutor#seeRowsCountInTableExactly(String, int)}
 * {@link JdbcExecutor#seeRowsCountInTableExactly(String, Object[][], int, int)}
 * {@link JdbcExecutor#seeRowsCountInTableExactly(String, String, int, int)}
 * {@link JdbcExecutor#seeRowsCountInTableExactly(String, int, int)}
 * {@link JdbcExecutor#seeTableIsEmpty(String)}
 * {@link JdbcExecutor#seeTableDoesNotEmpty(String)}
 * {@link JdbcExecutor#seeRecordExistsInTable(String, Object[][])}
 * {@link JdbcExecutor#seeRecordExistsInTable(String, String)}
 *
 * <p><b>Operations with data:</b>
 * {@link JdbcExecutor#insertIntoTable(String, Object[][], Object[][])}
 * {@link JdbcExecutor#insertIntoTable(String, Object[][])}
 * {@link JdbcExecutor#runScriptFromFile(String)}
 * {@link JdbcExecutor#runScript(String)}
 * {@link JdbcExecutor#truncateTable(String)}
 * {@link JdbcExecutor#truncateTables(String...)}
 * {@link JdbcExecutor#deleteFromTable(String)}
 * {@link JdbcExecutor#deleteFromTable(String, Object[][])}
 * {@link JdbcExecutor#deleteFromTable(String, String)}
 * {@link JdbcExecutor#updateTable(String, Object[][])}
 * {@link JdbcExecutor#updateTable(String, Object[][], Object[][])}
 * {@link JdbcExecutor#deleteFromTable(String, String)}
 *
 *
 * <p><b>For attachments:</b>
 * {@link JdbcExecutor#showDataFromTable(String)}
 * {@link JdbcExecutor#showDataByCondition(String, Object[][])}
 *
 * <p> Await for asserts default: {@link JdbcExecutor#awaitMs}, can be changed by: {@link JdbcExecutor#withAwaitMs(int)}
 * <p> Path to template files for insert default: {@link JdbcExecutor#templatePath}, can be changed by: {@link JdbcExecutor#setTemplateDirectory(String)}
 */
public abstract class JdbcExecutor<T extends JdbcExecutor<T>> extends AssertLogic implements SqlPost, SqlGet, Setters<T> {


    private static final String DELETE_FROM = "DELETE FROM %s %s";

    /**
     * default ms await in tests
     */
    protected int awaitMs = 2000;

    /**
     * default templates path for insert (dynamically for each database types)
     */
    protected String templatePath = String.format("templates/%s/", dbType);

    private static final Logger LOGGER = LoggerFactory.getLogger("bugreaper-module-db");


    protected JdbcExecutor(String dbUrl,
                           String username,
                           String passwd,
                           String getColumnsQuery) {

        super(dbUrl, username, passwd, getColumnsQuery);

        LOGGER.debug("Database type: {}", dbType);
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    @Override
    public T withAwaitMs(int awaitMs) {
        if (awaitMs < 200) {
            throw new IllegalArgumentException("awaitMs too small (can`t bee less 200ms)");
        }
        this.awaitMs = awaitMs;
        return self();
    }

    @Override
    public T setTemplateDirectory(String templatePath) {
        if (templatePath == null || templatePath.isBlank()) {
            throw new IllegalArgumentException("templatePath can`t be empty or null");
        }
        this.templatePath = templatePath;
        return self();
    }

    @Override
    public void insertIntoTable(String tableName,
                                Object[][] templateArray,
                                Object[][] providedArray) {

        Allure.step("(DB) Insert into table " + tableName + " record: " + Arrays.deepToString(providedArray),
                (Allure.ThrowableContextRunnableVoid<Allure.StepContext>) step ->
                        jdbc.sqlExecute(qb.sqlInsertBuilder(tableName, templateArray, providedArray))
        );
    }

    @Override
    public void insertIntoTable(String tableName,
                                Object[][] providedArray) {
        LOGGER.debug("Looking for template file in resources: {}", templatePath);

        insertIntoTable(
                tableName,
                jsonToObjectArray(readJsonFromFile(templatePath + tableName + ".json"), true),
                providedArray);
    }

    @Override
    @Step("(DB) run script from file {path}")
    public void runScriptFromFile(String path) {

        attachFromFileNoStep(path, path);

        jdbc.sqlNoAttachExecute(readTextFromFile(path));

        LOGGER.info("Script run completed from: {} ", path);
    }

    @Override
    @Step("(DB) {script}")
    public void runScript(String script) {
        jdbc.sqlExecute(script);
    }

    @Override
    @Step("(DB) Truncate table {tableName}")
    public void truncateTable(String tableName) {
        jdbc.sqlExecute("TRUNCATE TABLE " + tableName);
    }

    @Override
    @Step("(DB) Truncate tables: {tablesNames}")
    public void truncateTables(String... tablesNames) {
        for (String table : tablesNames) {
            jdbc.sqlExecute("TRUNCATE TABLE " + table);
        }

    }

    @Override
    public void updateTable(String tableName, Object[][] setArray) {

        Allure.step("(DB)UPDATE from " + tableName + " " + Arrays.deepToString(setArray), (Allure.ThrowableContextRunnableVoid<Allure.StepContext>) step ->
                jdbc.sqlExecute(String.format("UPDATE %s %s", tableName, qb.sqlSetBuilder(setArray)))
        );
    }

    @Override
    public void updateTable(String tableName, Object[][] setArray, Object[][] conditionsArray) {

        Allure.step("(DB)UPDATE from " + tableName + " " + Arrays.deepToString(setArray),
                (Allure.ThrowableContextRunnableVoid<Allure.StepContext>) step ->
                        jdbc.sqlExecute(String.format("UPDATE %s %s %s", tableName, qb.sqlSetBuilder(setArray), qb.sqlConditionBuilder(conditionsArray)))
        );
    }

    @Override
    @Step("(DB) Delete from {tableName} {conditions}")
    public void updateTable(String tableName, String conditions) {
        jdbc.sqlExecute(String.format("UPDATE %s %s", tableName, conditions));
    }

    @Override
    @Step("(DB) Delete from {tableName}")
    public void deleteFromTable(String tableName) {
        jdbc.sqlExecute("DELETE FROM " + tableName);
    }

    @Override
    public void deleteFromTable(String tableName, Object[][] conditionsArray) {
        Allure.step("(DB) Delete from " + tableName + " " + Arrays.deepToString(conditionsArray),
                (Allure.ThrowableContextRunnableVoid<Allure.StepContext>) step ->
                        jdbc.sqlExecute(String.format(DELETE_FROM,
                                tableName, qb.sqlConditionBuilder(conditionsArray)))
        );
    }

    @Override
    @Step("(DB) Delete from {tableName} {conditions}")
    public void deleteFromTable(String tableName, String conditions) {
        jdbc.sqlExecute(String.format(DELETE_FROM, tableName, conditions));
    }

    @Step("(DB) Rename table {tableName}")
    public void renameTable(String tableName) {
        jdbc.sqlExecute("RENAME TABLE " + tableName + " TO " + tableName + "_X_");
    }

    @Step("(DB) Rollback renamed table {tableName}")
    public void rollbackTable(String tableName) {
        jdbc.sqlExecute("RENAME TABLE " + tableName + "_X_ TO " + tableName);
    }


    @Override
    public void seeRowsCountInTableExactly(String tableName,
                                           Object[][] conditionsArray,
                                           int expectedCount) {
        var conditions = qb.sqlConditionBuilder(conditionsArray);
        assertCountInTableBuilder(tableName, conditions, conditionsArray, Integer.toString(expectedCount), awaitMs);
    }

    @Override
    public void seeRowsCountInTableExactly(String tableName,
                                           Object[][] conditionsArray,
                                           int expectedCount,
                                           int awaitMs) {
        var conditions = qb.sqlConditionBuilder(conditionsArray);
        assertCountInTableBuilder(tableName, conditions, conditionsArray, Integer.toString(expectedCount), awaitMs);
    }

    /**
     * Check count from table by string-conditions
     *
     * @param tableName     name of table with schema
     * @param conditions    converted to String
     * @param expectedCount how many rows expected
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    @Override
    public void seeRowsCountInTableExactly(String tableName, String conditions, int expectedCount) {
        assertCountInTableBuilder(tableName, conditions, null, Integer.toString(expectedCount), awaitMs);
    }

    @Override
    public void seeRowsCountInTableExactly(String tableName, String conditions, int expectedCount, int awaitMs) {
        assertCountInTableBuilder(tableName, conditions, null, Integer.toString(expectedCount), awaitMs);
    }

    /**
     * Check count from table without conditions
     *
     * @param tableName     name of table with schema
     * @param expectedCount how many rows expected
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    @Override
    public void seeRowsCountInTableExactly(String tableName, int expectedCount) {
        assertCountInTableBuilder(tableName, "", null, Integer.toString(expectedCount), awaitMs);
    }

    @Override
    public void seeRowsCountInTableExactly(String tableName, int expectedCount, int awaitMs) {
        assertCountInTableBuilder(tableName, "", null, Integer.toString(expectedCount), awaitMs);
    }

    @Override
    public void seeTableIsEmpty(String tableName) {
        assertCountInTableBuilder(tableName, "", null, "0", awaitMs);
    }

    @Override
    public void seeRecordExistsInTable(String tableName, String conditions) {
        recordExistsBuilder(tableName, conditions, null, awaitMs);
    }

    @Override
    public void seeRecordExistsInTable(String tableName,
                                       Object[][] conditionsArray) {
        var conditions = qb.sqlConditionBuilder(conditionsArray);
        recordExistsBuilder(tableName, conditions, conditionsArray, awaitMs);
    }


    @Override
    public void seeTableDoesNotEmpty(String tableName) {
        recordExistsBuilder(tableName, "", null, awaitMs);
    }

    @Override
    public void showDataByCondition(String tableName, Object[][] conditionsArray) {
        seeInDbByCondMethod(tableName, qb.sqlConditionBuilder(conditionsArray));
    }


    @Step("(DB) Show data from table {tableName}")
    public void showDataFromTable(String tableName) {
        showDataFromTableMethod(tableName);
    }

}
