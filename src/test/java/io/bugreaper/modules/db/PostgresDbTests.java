package io.bugreaper.modules.db;

import com.zaxxer.hikari.pool.HikariPool;
import io.bugreaper.core.exceptions.FileReaderException;
import io.bugreaper.modules.db.exceptions.JdbcExecutorException;
import io.bugreaper.modules.db.exceptions.QueryBuilderException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import testcontainers.PostgresDbSetup;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("squid:S2699")
class PostgresDbTests extends PostgresDbSetup {

    @BeforeEach
    void cleanTables() {
        db.truncateTable(TEST_TABLE);
    }

    @Test
    void getDataFromEmptyTable() {
        db.showDataFromTableMethod(TEST_TABLE);
    }

    @Test
    void getDataFromEmptyTableWrongUpperCase() {

        String wrongTableName = TEST_TABLE.toUpperCase();

        Throwable exception = assertThrows(JdbcExecutorException.class, () ->
                db.showDataFromTableMethod(wrongTableName));

        MatcherAssert.assertThat(
                "Exception on failed when wrong case (may be in postgres) given to sql request",
                exception.getMessage(),
                StringStartsWith.startsWith("No columns for allure in result after:"));
    }

    @Test
    void getDataFromFilledTable() {
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Pupkin2"},
        });

        db.showDataFromTableMethod(TEST_TABLE);
    }

    @Test
    void renameAndRollbackTable() {
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Pupkin2"},
        });
        db.renameTable(TEST_TABLE_FOR_RENAME);
        db.seeTableIsEmpty(TEST_TABLE_FOR_RENAME + "_X_");

        db.rollbackTable(TEST_TABLE_FOR_RENAME);
        db.showDataFromTableMethod(TEST_TABLE_FOR_RENAME);
    }

    @Test
    void checkRunScriptAndAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Pupkin2"},
        });

        db.runScript(String.format("UPDATE %s SET USERNAME='Alex' where ID=2", TEST_TABLE));

        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "2"},
                                {"USERNAME", "Alex"},
                        },
                        1);
    }

    @Test
    void templateFileInsertTest() {

        db.truncateTable(TEST_TABLE_FOR_TEMPLATE);

        db.insertIntoTable(TEST_TABLE_FOR_TEMPLATE,
                new Object[][]{
                        {"ID", 2},
                });


        db.seeRecordExistsInTable
                (TEST_TABLE_FOR_TEMPLATE,
                        new Object[][]{
                                {"ID", 2},
                                {"NAME", "Alex"},
                                {"AMOUNT", 10.55},
                                {"IS_ACTIVE", false},
                        });
    }

    @Test
    void seeInTableAllTypesTest() {
        insertIntoUsers(new Object[][]{
                {"ID", 2},
                {"USERNAME", "Pupkin2"},
                {"IS_ACTIVE", false},
                {"AMOUNT", "15.00"},
                {"PERCENTAGE", "16.99"},
        });

        db.seeRecordExistsInTable
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", 2},
                                {"USERNAME", "Pupkin2"},
                                {"IS_ACTIVE", false},
                                {"AMOUNT", "15"},
                                {"PERCENTAGE", "16.99"},
                        });
    }

    @Test
    void seeInTableAllTypesTestV2() {
        insertIntoUsers(new Object[][]{
                {"ID", 2},
                {"USERNAME", "Pupkin2"},
                {"IS_ACTIVE", false},
                //not recommended
                {"AMOUNT", 12.55f},
                {"PERCENTAGE", new BigDecimal("15.99")},
        });

        db.seeRecordExistsInTable
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", 2},
                                {"USERNAME", "Pupkin2"},
                                {"IS_ACTIVE", false},
                                {"AMOUNT", 12.55f},
                                {"PERCENTAGE", new BigDecimal("15.99")},
                        });
    }

    @Test
    void checkUpdateArraysAndAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Pupkin2"},
        });

        db.updateTable(
                TEST_TABLE,
                new Object[][]{
                        {"USERNAME", "Vova"},
                },
                new Object[][]{
                        {"ID", "2"},
                }
        );

        db.seeRowsCountInTableExactly(TEST_TABLE, 1);

        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "2"},
                                {"USERNAME", "Vova"},
                        },
                        1);
    }

    @Test
    void checkConditionExceptionOnEmptyTableCustomAwait() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "4"},
                                },
                                1, 600));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table) with custom timeout",
                exception.getMessage(),
                is("""
                        Failed assert count: ER <1> with AR <0> in public.users within 600 milliseconds
                        Expected for:
                        [ID]
                        [4]
                        Actual result(s)"""));
    }

    @Test
    void runFromFile() {
        db.truncateTable(TEST_TABLE2);

        db.runScriptFromFile("dump/test_script_postgres.sql");

        db.showDataByCondition(TEST_TABLE2, new Object[][]{
                {"USERNAME", "Alex1"},
        });

        db.showDataByCondition(TEST_TABLE2, new Object[][]{
                {"USERNAME", "Anna2"},
        });
    }


    @Test
    void createRecordJsonCheckCountAndData() {
        var data = """
                [{"id":1,"text":"test"}, {"id":2,"text":"test2"}]""";

        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"DATA", data},
        });
        db.seeRowsCountInTableExactly(TEST_TABLE, 1);

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                db.seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "1"},
                                        {"DATA", "JSON_CONTAINS", """
                                                    [{"id":1}]"""},
                                        {"ROLES", "manager"},
                                },
                                1));

        assertEquals("""
                        ERROR, wrong 3 elements: JSON_CONTAINS must be: [=, !=, <>, >, >=, <, <=, IS, IS NOT, LIKE]""",
                exception.getMessage(),
                "Exception with JSON in db not supported yet");
    }

    @Test
    void seeTableIsEmptyCatchTest() {
        insertIntoUsers(new Object[][]{
                {"ID", 2},
                {"USERNAME", "Pupkin2"},
                {"IS_ACTIVE", true},
        });

        Throwable exception = assertThrows(AssertionFailedError.class, () ->
                db.seeTableIsEmpty(TEST_TABLE));

        assertEquals("""
                        Failed assert count: ER <0> with AR <1> in public.users within 2 seconds""",
                exception.getMessage(),
                "Exception on seeTableIsEmpty");

    }

    @Test
    void seeRecordExistsInTableWithTimeSetterCatchTest() {
        insertIntoUsers(new Object[][]{
                {"ID", 222},
                {"USERNAME", "Pupkin2"},
                {"IS_ACTIVE", false},
                {"DESCRIPTION", null},
                {"AMOUNT", "15.00"},
                {"PERCENTAGE", "16.00"},
        });

        Throwable exception = assertThrows(AssertionFailedError.class, () ->
                dbAwait.seeRecordExistsInTable(TEST_TABLE,
                        new Object[][]{
                                {"ID", 1},
                                {"USERNAME", "Pupkin2"},
                                {"IS_ACTIVE", false},
                                {"DESCRIPTION", "descr"},
                                {"AMOUNT", "15.55"},
                                {"PERCENTAGE", "16.99"},
                        }));

        assertEquals("""
                        Failed: record/s not found in <public.users> within 1 second
                        Expected for:
                        [ID, USERNAME, IS_ACTIVE, DESCRIPTION, AMOUNT, PERCENTAGE]
                        [1, Pupkin2, false, descr, 15.55, 16.99]
                        Actual result(s)
                        [222, Pupkin2, false, null, 15.00, 16.000]""",
                exception.getMessage(),
                "Exception on seeRecordExistsInTable");
    }

    @Test
    void seeRecordExistsInTableEmptyConditionsTest() {

        Throwable exception = assertThrows(AssertionFailedError.class, () ->
                dbAwait.seeRecordExistsInTable(TEST_TABLE, ""));

        assertEquals("""
                        Failed: record/s not found in public.users within 1 second""",
                exception.getMessage(),
                "Exception on seeRecordExistsInTable with empty conditions");
    }

    @Test
    void seeRecordExistsInTableStringConditionsTest() {
        insertIntoUsers(new Object[][]{
                {"ID", 222},
                {"USERNAME", "Pupkin2"},
        });

        Throwable exception = assertThrows(AssertionFailedError.class, () ->
                dbAwait.seeRecordExistsInTable(TEST_TABLE, "where ID=15"));

        assertEquals("""
                        Failed: record/s not found in public.users within 1 second
                        where ID=15""",
                exception.getMessage(),
                "Exception on seeRecordExistsInTable with string conditions");
    }

    @Test
    void parallelTestForAwaitRecordExists() {

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> db.seeRecordExistsInTable(TEST_TABLE, "where ID >=1 AND USERNAME = 'Pupkin2'"));
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(this::pushWithSleep);

        CompletableFuture.allOf(future1, future2).join();

    }

    @SuppressWarnings("squid:S2925")
    private void pushWithSleep() {
        try {
            sleep(700);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        insertIntoUsers(new Object[][]{
                {"ID", 2},
                {"USERNAME", "Pupkin2"},
        });
    }

    @Test
    void testTruncate() {
        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Pupkin1"},
        });

        db.seeRowsCountInTableExactly(TEST_TABLE, 1);

        db.truncateTableCascade(TEST_TABLE);

        db.seeRowsCountInTableExactly(TEST_TABLE, 0);
    }

    @Test
    void configLowAwaitTest() {

        PostgreSQL dbTest = setup.getPostgresDb();

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                dbTest.withAwaitMs(100));

        MatcherAssert.assertThat(
                "Error on config .withAwaitMs negative validation",
                exception.getMessage(),
                StringContains.containsString("awaitMs too small (can`t bee less 200ms)"));
    }

    @Test
    void configEmptyTemplatePathTest() {

        PostgreSQL dbTest = setup.getPostgresDb();

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                dbTest.setTemplatesDirectory(""));

        MatcherAssert.assertThat(
                "Error on config .setTemplatesDirectory empty",
                exception.getMessage(),
                StringContains.containsString("templatesPath can`t be empty or null"));
    }

    @Test
    void configNullTemplatePathTest() {

        PostgreSQL dbTest = setup.getPostgresDb();

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                dbTest.setTemplatesDirectory(null));

        MatcherAssert.assertThat(
                "Error on config .setTemplatesDirectory empty",
                exception.getMessage(),
                StringContains.containsString("templatesPath can`t be empty or null"));
    }

    @Test
    void templateFileForInsertAbsentTest() {

        Throwable exception = assertThrows(FileReaderException.class, () ->
                db.insertIntoTable("NOT_EXISTS.TABLE",
                        new Object[][]{
                                {"ID", 2},
                        }));

        MatcherAssert.assertThat(
                "No template file in resources",
                exception.getMessage(),
                StringContains.containsString("File not exist in resources: templates/postgresql/NOT_EXISTS.TABLE.json"));

    }

    @Test
    void wrongConfigConnect() {

        Throwable exception = assertThrows(HikariPool.PoolInitializationException.class, PostgreSQL::new);

        MatcherAssert.assertThat(
                exception.getMessage(),
                StringContains.containsString("Connection to localhost:5434 refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections"));

    }

}
