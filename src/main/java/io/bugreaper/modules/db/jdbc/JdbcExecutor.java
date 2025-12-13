package io.bugreaper.modules.db.jdbc;

import io.bugreaper.core.config.YamlUtils;
import io.bugreaper.modules.db.DbTypes;
import io.bugreaper.modules.db.jdbc.builders.QueryBuilder;
import io.bugreaper.modules.db.jdbc.connection.JdbcDirectivesHikari;
import io.bugreaper.modules.db.jdbc.interfaces.SqlConfig;
import io.bugreaper.modules.db.jdbc.interfaces.SqlGet;
import io.bugreaper.modules.db.jdbc.interfaces.SqlPost;
import io.bugreaper.modules.db.logger.Log;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

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
 * {@link JdbcExecutor#seeRowsCountInTableExactlyCustom(String, String, int)}
 * {@link JdbcExecutor#seeRowsCountInTableExactly(String, int)}
 * {@link JdbcExecutor#seeRowsCountInTableExactly(String, Object[][], int, int)}
 * {@link JdbcExecutor#seeRowsCountInTableExactlyCustom(String, String, int, int)}
 * {@link JdbcExecutor#seeRowsCountInTableExactly(String, int, int)}
 * {@link JdbcExecutor#seeTableIsEmpty(String)}
 * {@link JdbcExecutor#seeTableDoesNotEmpty(String)}
 * {@link JdbcExecutor#seeRecordExistsInTable(String, Object[][])}
 * {@link JdbcExecutor#seeRecordExistsInTable(String, String)}
 *
 * <p><b>Operations with data:</b>
 * {@link JdbcExecutor#insertIntoTable(String, Object[][], Object[][])}
 * {@link JdbcExecutor#insertIntoTable(String, Object[][])}
 * {@link JdbcExecutor#insertIntoTable(String, String)}
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
 * <p>Supported dbTypes: {@link DbTypes}</p>
 *
 * <p> Await for asserts default: {@link JdbcExecutor#awaitMs}, can be changed by: {@link JdbcExecutor#withAwaitMs(int)}
 * <p> Path to template files for insert default: {@link JdbcExecutor#templatesPath}, can be changed by: {@link JdbcExecutor#setTemplatesDirectory(String)}
 */
public abstract class JdbcExecutor<T extends JdbcExecutor<T>> extends AbstractExecutor implements SqlPost, SqlGet, SqlConfig<T> {



    private static final String YML_KEY = "modules.db.";
    private static final String DELETE_FROM = "DELETE FROM %s %s";

    /**
     * default ms await in tests
     */
    private int awaitMs = 2000;

    /**
     * default templates path for insert (dynamically for each database types)
     */
    private String templatesPath;
    private final String dbType;
    private final QueryBuilder qb;

    protected JdbcExecutor(String url,
                           String username,
                           String password,
                           String getColumnsQuery,
                           String dbType) {

        this.qb = new QueryBuilder(dbType);
        this.dbType = dbType;
        this.templatesPath = String.format("templates/%s/", dbType);

        this.url = url;
        this.username = username;
        this.password = password;


        jdbc = new JdbcDirectivesHikari(this.url, this.username, this.password);
        this.getColumnsQuery = getColumnsQuery;
    }


    /**
     * Constructs db client configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     *
     * <p><b>Default file:</b> {@code bugreaper.yml}</p>
     * <p><b>Custom file:</b> using {@code -DbugreaperEnv=test} loads {@code bugreaper-test.yml}</p>
     *
     * <p><b>Required configuration keys:</b></p>
     * <p>Supported dbTypes: {@link DbTypes}</p>
     * <ul>
     *     <li>{@code modules.db.${dbType}.url}</li>
     *     <li>{@code modules.db.${dbType}.port}</li>
     *     <li>{@code modules.db.${dbType}.username}</li>
     *     <li>{@code modules.db.${dbType}.password}</li>
     * </ul>
     *
     * <p><b>Optional configuration keys:</b></p>
     * <ul>
     *     <li>{@code modules.db.${dbType}.await}</li>
     * </ul>
     *
     * <p>Missing required keys will result in configuration errors.
     * Missing optional keys will fall back to predefined defaults.</p>
     */
    protected JdbcExecutor(String dbType, String getColumnsQuery) {

        this.dbType = dbType;
        this.getColumnsQuery = getColumnsQuery;
        this.templatesPath = String.format("templates/%s/", dbType);

        loadFromYaml(dbType);

        jdbc = new JdbcDirectivesHikari(this.url, this.username, this.password);
        this.qb = new QueryBuilder(dbType);
    }


    private void loadFromYaml(String dbType) {

        //required config fields
        this.url = YamlUtils.getStringValueByPath(YML_KEY + dbType + ".url");
        this.username = YamlUtils.getStringValueByPath(YML_KEY + dbType + ".username");
        this.password = YamlUtils.getStringValueByPath(YML_KEY + dbType + ".password");

        //optional config fields
        Object awaitVal = YamlUtils.getValueByPath(YML_KEY + dbType + ".await", true);
        if (awaitVal instanceof Number number) {
            withAwaitMs(number.intValue());
        }
    }


    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    // setters

    @Override
    public T withAwaitMs(int awaitMs) {
        if (awaitMs < 200) {
            throw new IllegalArgumentException("awaitMs too small (can`t bee less 200ms)");
        }
        this.awaitMs = awaitMs;
        return self();
    }

    @Override
    public T setTemplatesDirectory(String templatesPath) {
        if (templatesPath == null || templatesPath.isBlank()) {
            throw new IllegalArgumentException("templatesPath can`t be empty or null");
        }
        this.templatesPath = templatesPath;
        return self();
    }

    // getters
    @Override
    public String getConfigSummary() {
        String info = String.format("""
        %s:
            dbType=%s
            url=%s
            username=%s
            password=%s
            awaitMs=%d
            templatesPath=%s%n""",
                this.getClass().getSimpleName(), dbType, url, username, password,
                awaitMs, templatesPath);

        Log.LOGGER.info(info);
        return info;
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
        Log.LOGGER.debug("Looking for template file in resources: {}", templatesPath);

        insertIntoTable(
                tableName,
                jsonToObjectArray(readJsonFromFile(templatesPath + tableName + ".json"), true),
                providedArray);
    }

   @Override
    public void insertIntoTable(String tableName,
                                String providedJson) {

        insertIntoTable(
                tableName,
                jsonToObjectArray(providedJson, true));
    }

    @Override
    @Step("(DB) run script from file {path}")
    public void runScriptFromFile(String path) {

        attachFromFileNoStep(path, path);

        jdbc.sqlNoAttachExecute(readTextFromFile(path));

        Log.LOGGER.info("Script run completed from: {} ", path);
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

        Allure.step("(DB)UPDATE " + tableName + " " + Arrays.deepToString(setArray), (Allure.ThrowableContextRunnableVoid<Allure.StepContext>) step ->
                jdbc.sqlExecute(String.format("UPDATE %s %s", tableName, qb.sqlSetBuilder(setArray)))
        );
    }

    @Override
    public void updateTable(String tableName, Object[][] setArray, Object[][] conditionsArray) {

        Allure.step("(DB)UPDATE " + tableName + " " + Arrays.deepToString(setArray),
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
                                           String providedJson,
                                           int expectedCount) {
        seeRowsCountInTableExactly(
                tableName,
                jsonToObjectArray(providedJson, false),
                expectedCount);
    }

    @Override
    public void seeRowsCountInTableExactly(String tableName,
                                           Object[][] conditionsArray,
                                           int expectedCount,
                                           int awaitMs) {
        var conditions = qb.sqlConditionBuilder(conditionsArray);
        assertCountInTableBuilder(tableName, conditions, conditionsArray, Integer.toString(expectedCount), awaitMs);
    }

    public void seeRowsCountInTableExactlyCustom(String tableName, String conditions, int expectedCount) {
        assertCountInTableBuilder(tableName, conditions, null, Integer.toString(expectedCount), awaitMs);
    }

    public void seeRowsCountInTableExactlyCustom(String tableName, String conditions, int expectedCount, int awaitMs) {
        assertCountInTableBuilder(tableName, conditions, null, Integer.toString(expectedCount), awaitMs);
    }

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
    public void seeRecordExistsInTableCustom(String tableName, String conditions) {
        recordExistsBuilder(tableName, conditions, null, awaitMs);
    }

    @Override
    public void seeRecordExistsInTable(String tableName, String providedJson) {

        seeRecordExistsInTable(
                tableName,
                jsonToObjectArray(providedJson, false));
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
