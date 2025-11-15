package io.bugreaper.modules.db.jdbc;

import io.bugreaper.modules.db.exceptions.JdbcExecutorException;
import io.bugreaper.modules.db.jdbc.builders.QueryBuilder;
import io.bugreaper.modules.db.jdbc.connection.JdbcDirectivesHikari;
import io.qameta.allure.Allure;
import io.qameta.allure.Param;
import io.qameta.allure.Step;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.core.ConditionTimeoutException;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.bugreaper.core.assertions.Asserts.assertGreaterThanExpected;
import static io.bugreaper.core.assertions.Asserts.assertStrings;
import static io.bugreaper.core.mappers.StringMappers.formatMilliseconds;
import static io.bugreaper.modules.db.allurereporter.AllureBuilder.reportHtmlTableAllBuilder;
import static io.bugreaper.modules.db.allurereporter.AllureReporter.attachTableForDb;
import static io.qameta.allure.model.Parameter.Mode.HIDDEN;
import static java.time.Duration.ofMillis;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("squid:S5960")
abstract class AssertLogic {

    protected final QueryBuilder qb;
    protected final JdbcDirectivesHikari jdbc;
    protected final String getColumnsQuery;


    protected static final String SELECT_ALL = "SELECT * FROM ";
    protected static final String COUNT = "COUNT_RES";
    protected final String dbType;

    protected AssertLogic(String dbUrl,
                          String username,
                          String passwd,
                          String getColumnsQuery) {

        this.dbType = StringUtils.substringBefore(URI.create(dbUrl).getSchemeSpecificPart(), ":");

        jdbc = new JdbcDirectivesHikari(dbUrl, username, passwd);

        this.qb = new QueryBuilder(dbType);
        this.getColumnsQuery = getColumnsQuery;
    }

    @Step("(DB)[ASSERT] Count records in {tableName} {conditions} EQUAL {expectedCount}")
    protected void assertCountInTableBuilder(String tableName,
                                             String conditions,
                                             @Param(mode = HIDDEN) Object[][] conditionsArray,
                                             String expectedCount,
                                             @Param(mode = HIDDEN) int awaitMs) {

        var query = MessageFormat.format("SELECT COUNT(*) as {0} FROM {1} {2}",
                COUNT, tableName, conditions);

        attachTableForDb("Expected in table " + tableName, conditionsArray);

        Allure.addAttachment("execute sql with", "text/plain", query + ";");

        try {
            await().with()
                    .pollDelay(ofMillis(0)).atMost(ofMillis(awaitMs)).untilAsserted(() ->
                            assertStrings(
                                    jdbc.getSqlResultForCount(query, COUNT),
                                    expectedCount));
        } catch (ConditionTimeoutException ex) {

            showDataFromTableMethod(tableName);

            if (conditionsArray != null) {

                var extendLog = extendedLogBuilder(tableName, conditionsArray);

                fail(MessageFormat.format("Failed assert count: ER <{0}> with AR <{1}> in {2} within {3}\n{4}",
                        expectedCount, jdbc.getSqlResultForCount(query, COUNT), tableName, formatMilliseconds(awaitMs), extendLog));
            } else if (!Objects.equals(conditions, "")) {
                fail(MessageFormat.format("Failed assert count: ER <{0}> with AR <{1}> in {2} within {3}\n{4}",
                        expectedCount, jdbc.getSqlResultForCount(query, COUNT), tableName, formatMilliseconds(awaitMs), conditions));
            }

            fail(MessageFormat.format("Failed assert count: ER <{0}> with AR <{1}> in {2} within {3}",
                    expectedCount, jdbc.getSqlResultForCount(query, COUNT), tableName, formatMilliseconds(awaitMs)));
        }
    }

    @Step("(DB)[ASSERT] Record/s exists in {tableName} {conditions}")
    protected void recordExistsBuilder(String tableName,
                                       String conditions,
                                       @Param(mode = HIDDEN) Object[][] conditionsArray,
                                       @Param(mode = HIDDEN) int awaitMs) {

        var query = MessageFormat.format("SELECT COUNT(*) as {0} FROM {1} {2}",
                COUNT, tableName, conditions);

        attachTableForDb("Expected in table " + tableName, conditionsArray);

        Allure.addAttachment("execute sql with", "text/plain", query + ";");

        try {
            await().with()
                    .pollDelay(ofMillis(0)).atMost(ofMillis(awaitMs)).untilAsserted(() ->
                            assertGreaterThanExpected(0, Integer.parseInt(jdbc.getSqlResultForCount(query, COUNT))));
        } catch (ConditionTimeoutException ex) {

            showDataFromTableMethod(tableName);

            if (conditionsArray != null) {

                var extendLog = extendedLogBuilder(tableName, conditionsArray);

                fail(MessageFormat.format("Failed: record/s not found in <{0}> within {1}\n{2}",
                        tableName, formatMilliseconds(awaitMs), extendLog));
            } else if (!Objects.equals(conditions, "")) {
                fail(MessageFormat.format("Failed: record/s not found in {0} within {1}\n{2}",
                        tableName, formatMilliseconds(awaitMs), conditions));
            }

            fail(MessageFormat.format("Failed: record/s not found in {0} within {1}",
                    tableName, formatMilliseconds(awaitMs)));
        }
    }

    /**
     * Create console log for visualise difference
     * between AR and ER
     *
     * @param tableName       name of table with schema
     * @param conditionsArray Object[][] with conditions {{"ID", "1"}, {"NAME", "Alex"}}
     * @return string with exception (columns from test with ER and all rows from table ordered by columns)
     */
    private String extendedLogBuilder(String tableName, Object[][] conditionsArray) {

        //  Create lists with columns names & expected columns data
        String[] columnsName = new String[conditionsArray.length];
        ArrayList<Object> expectFor = new ArrayList<>();

        for (int i = 0; i < conditionsArray.length; i++) {
            columnsName[i] = conditionsArray[i][0].toString();

            if (conditionsArray[i].length > 2) {
                expectFor.add("(" + conditionsArray[i][1] + ")" + conditionsArray[i][2]);
            } else {
                expectFor.add(conditionsArray[i][1]);
            }

        }

        //  Get result for columns in test
        List<Object> resultArray = jdbc.getSqlResultList(SELECT_ALL + tableName, columnsName);

        //  Separate result by columns & rows
        final int COLUMN_NUMBER = expectFor.size();

        StringBuilder dataRows = new StringBuilder();
        final int ROWS = resultArray.size() / COLUMN_NUMBER;
        for (int r = 0; r < ROWS; r++) {
            dataRows.append("\n[");
            for (int i = COLUMN_NUMBER * r; i < COLUMN_NUMBER * (r + 1); i++) {
                dataRows.append(resultArray.get(i)).append(", ");
            }
            dataRows.delete(dataRows.length() - 2, dataRows.length());
            dataRows.append("]");
        }

        //  Create Log
        return "Expected for:\n" + Arrays.toString(columnsName) + "\n" + expectFor +
                "\nActual result(s)" + dataRows;
    }


    public void showDataFromTableMethod(String tableName) {
        String[] columnsArray = getTableColumns(tableName);

        if (columnsArray.length == 0) {
            throw new JdbcExecutorException(
                    MessageFormat.format("No columns for allure in result after:\n{0}", columnRequest(tableName)
                    ));
        }

        Object[] resultArray = jdbc.getSqlResultList(SELECT_ALL + tableName, columnsArray).toArray(Object[]::new);

        reportHtmlTableAllBuilder(tableName, columnsArray, resultArray);
    }

    @Step("(DB)[ASSERT] Records exists in {tableName}: {conditions}")
    protected void seeInDbByCondMethod(String tableName, String conditions) {

        String[] columnsArray = getTableColumns(tableName);
        Object[] resultArray = jdbc.getSqlResultList(SELECT_ALL + tableName + " " + conditions, columnsArray).toArray(Object[]::new);

        if (resultArray.length == 0) {
            showDataFromTableMethod(tableName);
            fail("No records in " + tableName + " by conditions: " + conditions);
        }

        reportHtmlTableAllBuilder(tableName, columnsArray, resultArray);
    }

    /**
     * Get array with table column list
     */
    protected String[] getTableColumns(String tableName) {

        return jdbc.getSqlResultList(
                        columnRequest(tableName),
                        "COLUMN_NAME")
                .toArray(String[]::new);
    }

    private String columnRequest(String tableName) {
        return String.format(getColumnsQuery,
                StringUtils.substringBefore(tableName, "."),
                StringUtils.substringAfter(tableName, "."));
    }
}
