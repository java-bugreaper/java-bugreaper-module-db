package net.bugreaper.modules.db.jdbc;

import net.bugreaper.modules.db.exceptions.JdbcExecutorException;
import net.bugreaper.modules.db.jdbc.connection.JdbcDirectivesHikari;
import io.qameta.allure.Allure;
import io.qameta.allure.Param;
import io.qameta.allure.Step;
import net.bugreaper.modules.db.allurereporter.AllureReporter;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.bugreaper.core.assertions.Asserts.assertStrings;
import static net.bugreaper.core.mappers.StringMappers.formatMilliseconds;
import static net.bugreaper.core.utils.AwaitUtils.awaitCustom;
import static net.bugreaper.modules.db.allurereporter.AllureBuilder.reportHtmlTableAllBuilder;
import static io.qameta.allure.model.Parameter.Mode.HIDDEN;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SuppressWarnings("squid:S5960")
abstract class AbstractExecutor {

    private static final Logger log = LoggerFactory.getLogger(AbstractExecutor.class);
    protected JdbcDirectivesHikari jdbc;
    protected String getColumnsQuery;
    protected String url;
    protected String username;
    protected String password;


    protected static final String SELECT_ALL = "SELECT * FROM ";
    protected static final String COUNT = "COUNT_RES";


    /**
     * Default await polling interval in milliseconds for tests.
     */
    protected volatile int awaitPollInterval = 100;

    /**
     * Enables enhanced reporting with full table data attached to Allure
     * and actual column values included in assertion exceptions.
     */
    protected volatile boolean enableEnchantedReport = true;

    protected AbstractExecutor() {
    }

    @Step("(DB)[ASSERT] Table: '{tableName}' contains EXACTLY <{expectedCount}> record/s {conditions}")
    protected void assertCountInTableBuilder(String tableName,
                                             String conditions,
                                             @Param(mode = HIDDEN) Object[][] conditionsArray,
                                             String expectedCount,
                                             @Param(mode = HIDDEN) int providedAwaitMs) {

        var query = MessageFormat.format("SELECT COUNT(*) as {0} FROM {1} {2}",
                COUNT, tableName, conditions);

        AllureReporter.attachTableForDb("Expected record:", conditionsArray);

        Allure.addAttachment("Execute SQL:", "text/plain", query + ";");

        try {
            awaitCustom(providedAwaitMs, awaitPollInterval).untilAsserted(() ->
                    assertStrings(
                            jdbc.getSqlResultForCount(query, COUNT),
                            expectedCount));
        } catch (ConditionTimeoutException ex) {

            reportAllDataInTable(tableName, enableEnchantedReport);

            if (conditionsArray != null) {

                var extendLog = extendedLogBuilder(tableName, conditionsArray, enableEnchantedReport);

                throw new AssertionError(
                        "Expected EXACTLY <%s> records in table '%s', but got <%s> within %s%n%s"
                                .formatted(expectedCount, tableName, jdbc.getSqlResultForCount(query, COUNT), formatMilliseconds(providedAwaitMs), extendLog));
            } else if (!Objects.equals(conditions, "")) {
                throw new AssertionError(
                        "Expected EXACTLY <%s> records in table '%s', but got <%s> within %s%n%s"
                                .formatted(expectedCount, tableName, jdbc.getSqlResultForCount(query, COUNT), formatMilliseconds(providedAwaitMs), conditions));
            }
            throw new AssertionError(
                    "Expected EXACTLY <%s> records in table '%s', but got <%s> within %s"
                            .formatted(expectedCount, tableName, jdbc.getSqlResultForCount(query, COUNT), formatMilliseconds(providedAwaitMs)));
        }
    }

    @Step("(DB)[ASSERT] Table '{tableName}' contains records {conditions}")
    protected void recordExistsBuilder(String tableName,
                                       String conditions,
                                       @Param(mode = HIDDEN) Object[][] conditionsArray,
                                       @Param(mode = HIDDEN) int providedAwaitMs) {

        var query = MessageFormat.format("SELECT COUNT(*) as {0} FROM {1} {2}",
                COUNT, tableName, conditions);

        AllureReporter.attachTableForDb("Expected record:", conditionsArray);

        Allure.addAttachment("Execute SQL:", "text/plain", query + ";");

        try {
            awaitCustom(providedAwaitMs, awaitPollInterval).untilAsserted(() ->
                    assertNotEquals(0, Integer.parseInt(jdbc.getSqlResultForCount(query, COUNT))));
        } catch (ConditionTimeoutException ex) {

            reportAllDataInTable(tableName, enableEnchantedReport);

            if (conditionsArray != null) {

                var extendLog = extendedLogBuilder(tableName, conditionsArray, enableEnchantedReport);

                throw new AssertionError(
                        "Failed: no records found in table '%s' within %s%n%s"
                                .formatted(tableName, formatMilliseconds(providedAwaitMs), extendLog));
            } else if (!Objects.equals(conditions, "")) {
                throw new AssertionError(
                        "Failed: no records found in table '%s' within %s%n%s"
                                .formatted(tableName, formatMilliseconds(providedAwaitMs), conditions));
            }
            throw new AssertionError(
                    "Failed: no records found in in table '%s' within %s"
                            .formatted(tableName, formatMilliseconds(providedAwaitMs)));
        }
    }


    private void reportAllDataInTable(String tableName, boolean enable) {
        if (enable) {
            showDataFromTableNoExceptionMethod(tableName);
        }
    }

    /**
     * Creates a console log to visualize the difference between AR and ER.
     *
     * @param tableName       table name in the format {@code schema_name.table_name}
     * @param conditionsArray conditions in Object[][] format {@code {{"ID", "1"}, {"NAME", "Alex"}} }
     * @param actualEnable    whether to include the actual result
     * @return string containing the exception details, including all table records ordered by the testing columns
     */
    private String extendedLogBuilder(String tableName, Object[][] conditionsArray, boolean actualEnable) {

        //  Create lists with columns names & expected columns data
        String[] columnsName = new String[conditionsArray.length];
        ArrayList<Object> expectFor = new ArrayList<>();

        for (int i = 0; i < conditionsArray.length; i++) {
            columnsName[i] = conditionsArray[i][0].toString();

            switch (conditionsArray[i].length) {
                //for oracle dates
                case 4:
                    expectFor.add("(" + conditionsArray[i][1] + ")" + conditionsArray[i][3]);
                    break;
                //for custom conditions
                case 3:
                    expectFor.add("(" + conditionsArray[i][1] + ")" + conditionsArray[i][2]);
                    break;
                //base two elements or else
                default:
                    expectFor.add(conditionsArray[i][1]);
                    break;
            }

        }

        StringBuilder dataRows = new StringBuilder();

        if (actualEnable) {

            //  Get result for columns in test
            List<Object> resultArray = jdbc.getSqlResultList(SELECT_ALL + tableName, columnsName);

            //  Separate result by columns & rows
            final int COLUMN_NUMBER = expectFor.size();


            final int ROWS = resultArray.size() / COLUMN_NUMBER;

            for (int r = 0; r < ROWS; r++) {
                dataRows.append("\n[");
                for (int i = COLUMN_NUMBER * r; i < COLUMN_NUMBER * (r + 1); i++) {
                    dataRows.append(resultArray.get(i)).append(", ");
                }
                dataRows.delete(dataRows.length() - 2, dataRows.length());
                dataRows.append("]");
            }
        } else {
            dataRows.append("\nenableEnchantedReport disabled: actual result was not requested.");
        }

        //  Create Log
        return "Expected for:\n" + Arrays.toString(columnsName) + "\n" + expectFor +
                "\nActual result(s):" + dataRows;
    }


    protected void showDataFromTableMethod(String tableName) {
        String[] columnsArray = getTableColumns(tableName);

        if (columnsArray.length == 0) {
            throw new JdbcExecutorException(
                    MessageFormat.format("No columns for allure attachment in result after:\n{0}", columnRequest(tableName)
                    ));
        }

        Object[] resultArray = jdbc.getSqlResultList(SELECT_ALL + tableName, columnsArray).toArray(Object[]::new);

        reportHtmlTableAllBuilder(tableName, columnsArray, resultArray);
    }

    private void showDataFromTableNoExceptionMethod(String tableName) {
        String[] columnsArray = getTableColumns(tableName);

        if (columnsArray.length == 0 && log.isWarnEnabled()) {
            log.warn("No columns for allure attachment in result after:\n{}", columnRequest(tableName));
        } else {

            Object[] resultArray = jdbc.getSqlResultList(SELECT_ALL + tableName, columnsArray).toArray(Object[]::new);
            reportHtmlTableAllBuilder(tableName, columnsArray, resultArray);
        }
    }

    @Step("(DB) Show data from table '{tableName}' {conditions}")
    protected void seeInDbByCondMethod(String tableName, String conditions) {

        String[] columnsArray = getTableColumns(tableName);
        Object[] resultArray = jdbc.getSqlResultList(SELECT_ALL + tableName + " " + conditions, columnsArray).toArray(Object[]::new);

        if (resultArray.length == 0) {
            showDataFromTableMethod(tableName);
            throw new AssertionError("Failed: no records found in table '%s' %s".formatted(tableName, conditions));
        }

        reportHtmlTableAllBuilder(tableName, columnsArray, resultArray);
    }

    /**
     * Returns the list of columns in the table.
     *
     * @param tableName table name in the format {@code schema_name.table_name}
     * @return array containing column names
     */
    protected String[] getTableColumns(String tableName) {

        try {
            return jdbc.getSqlResultList(
                            columnRequest(tableName),
                            "COLUMN_NAME")
                    .toArray(String[]::new);
        } catch (Exception e) {
            return new String[0];
        }
    }

    private String columnRequest(String tableName) {
        return String.format(getColumnsQuery,
                StringUtils.substringBefore(tableName, "."),
                StringUtils.substringAfter(tableName, "."));
    }
}
