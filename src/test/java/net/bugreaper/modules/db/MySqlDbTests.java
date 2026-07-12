package net.bugreaper.modules.db;

import net.bugreaper.core.utils.AllureResultLoader;
import net.bugreaper.modules.db.exceptions.QueryBuilderException;
import net.bugreaper.modules.db.exceptions.SqlExecuteException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import testcontainers.MySqlDbSetup;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"squid:S2699", "java:S5778"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //To call connection once!
class MySqlDbTests extends MySqlDbSetup {

    private static final MySQL db = getMysqlDb().setTemplatesDirectory("templates/custom/");
    private static final MySQL dbConf = MySQL.getInstance();

    private static final String TEST_TABLE = "MYSQL_SCHEMA.USERS";
    private static final String TEST_TABLE2 = "MYSQL_SCHEMA.USERS2";
    private static final String TEST_TABLE_FOR_TEMPLATE = "MYSQL_SCHEMA.TEMP";

    private void insertIntoUsers(Object[][] testArray) {
        final Object[][] template = {
                {"ID", 100},
                {"DEPOSIT", null},
                {"USERNAME", "Pupkin"},
                {"PASSWORD", "pupKin1992"},
                {"EMAIL", "pupkin@com.com"},
                {"STATE", 0},
                {"DESCRIPTION", "dummy description"},
                {"ROLES", "manager"},
                {"DATA", "[]"},
                {"IS_ACTIVE", false},
                {"CREATE_DATE", "2021-02-02 10:00:00"},
                {"UPDATE_DATE", "2021-02-02 10:00:00"},
        };

        db.insertIntoTable(TEST_TABLE, template, testArray);
    }

    private void insertIntoUsers2(Object[][] testArray) {
        final Object[][] template = {
                {"ID", "100"},
                {"USERNAME", "Pupkin2"},
        };

        db.insertIntoTable(TEST_TABLE2, template, testArray);
    }

    @BeforeAll
    static void clean() {
        AllureResultLoader.cleanResultsDir();
    }

    @BeforeEach
    void cleanTables() {
        db.truncateTable(TEST_TABLE);
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
    void getDataFromEmptyTable() {
        db.showDataFromTable(TEST_TABLE);
    }

    @Test
    void renameAndRollbackTable() {
        db.showDataFromTable("MYSQL_SCHEMA.TEST");
        db.renameTable("MYSQL_SCHEMA.TEST");
        db.showDataFromTable("MYSQL_SCHEMA.TEST_X_");

        db.rollbackTable("MYSQL_SCHEMA.TEST");
        db.showDataFromTable("MYSQL_SCHEMA.TEST");
    }

    @Test
    void multipleTruncate() {
        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Pupkin1"},
        });

        insertIntoUsers2(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Pupkin2"},
        });

        db.seeRowsCountInTableExactly(TEST_TABLE, 1);
        db.seeRowsCountInTableExactly(TEST_TABLE2, 1);

        db.truncateTables(TEST_TABLE, TEST_TABLE2);

        db.seeRowsCountInTableExactly(TEST_TABLE, 0);
        db.seeRowsCountInTableExactly(TEST_TABLE2, 0);
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
    void checkUpdateArrayAndAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", "4"},
                {"USERNAME", "Pupkin"},
        });

        db.updateTable(
                TEST_TABLE,
                new Object[][]{
                        {"USERNAME", "Vova_one"},
                }
        );

        db.seeRowsCountInTableExactly(TEST_TABLE, 1);

        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "4"},
                                {"USERNAME", "Vova_one"},
                        },
                        1);
    }

    @Test
    void checkUpdateStringAndAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", "5"},
                {"USERNAME", "Pupkin5"},
        });

        db.updateTable(
                TEST_TABLE,
                "SET USERNAME='Vova_new' where ID=5"
        );

        db.seeRowsCountInTableExactly(TEST_TABLE, 1);

        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "5"},
                                {"USERNAME", "Vova_new"},
                        },
                        1);
    }


    @Test
    void checkDeleteArrayAndAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Pupkin2"},
        });

        insertIntoUsers(new Object[][]{
                {"ID", "3"},
                {"USERNAME", "Pupkin3"},
        });

        db.deleteFromTable(
                TEST_TABLE,
                new Object[][]{
                        {"ID", "2"},
                        {"USERNAME", "Pupkin2"},
                }
        );

        db.seeRowsCountInTableExactly(TEST_TABLE, 1);

        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "3"},
                                {"USERNAME", "Pupkin3"},
                        },
                        1);
    }

    @Test
    void checkDeleteStringAndAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", "5"},
                {"USERNAME", "Pupkin5"},
        });

        insertIntoUsers(new Object[][]{
                {"ID", "6"},
                {"USERNAME", "Pupkin6"},
        });

        db.deleteFromTable(
                TEST_TABLE, "where ID=5"
        );

        db.seeRowsCountInTableExactly(TEST_TABLE, 1);

        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "6"},
                                {"USERNAME", "Pupkin6"},
                        },
                        1);
    }

    @Test
    void checkDeleteAndAssertEmptyTable() {
        insertIntoUsers(new Object[][]{
                {"ID", "6"},
        });
        db.deleteFromTable(TEST_TABLE);
        db.seeRowsCountInTableExactly(TEST_TABLE, 0);
    }

    @Test
    void checkRegexAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", "22"},
        });
        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "REGEX_MATCH", "[0-2]{2}"},
                        },
                        1);
    }

    @Test
    void checkCustomConditionAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", "6"},
        });

        db.seeRowsCountInTableExactlyCustom(TEST_TABLE, "where ID >=1 AND DEPOSIT is null", 1);
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
        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "1"},
                                {"DATA", "JSON_CONTAINS", """
                                                    [{"id":1}]"""},
                                {"ROLES", "manager"},
                        },
                        1);
    }

    @Test
    void checkMariaNotSupportJsonEquals() {
        var data = """
                [{"id":1,"text":"test"}, {"id":2,"text":"test2"}]""";


        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                db.seeRecordExistsInTable
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "1"},
                                        {"DATA", "JSON_EQUALS", data},
                                        {"ROLES", "manager"},
                                }));

        MatcherAssert.assertThat(
                "Exception on JSON_EQUALS for mysql",
                exception.getMessage(),
                StringContains.containsString("ERROR, wrong 3 elements operator for mysql mapper: <JSON_EQUALS> supported: [=, !=, <>, >, >=, <, <=, IS, IS NOT, LIKE] or JSON_CONTAINS, REGEX_MATCH"));
    }

    @Test
    void checkRegex() {
        insertIntoUsers(new Object[][]{
                {"ID", 4145},
                {"USERNAME", "Pupkin"},
                {"IS_ACTIVE", true},
        });

        db.seeRecordExistsInTable
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "REGEX_MATCH", "[0-9]{4}"},
                        });
    }

    @Test
    void runFromFile() {
        db.truncateTable(TEST_TABLE2);

        db.runScriptFromFile("dump/test_script_mysql.sql");

        db.showDataByCondition(TEST_TABLE2, new Object[][]{
                {"USERNAME", "Alex1"},
        });

        db.showDataByCondition(TEST_TABLE2, new Object[][]{
                {"USERNAME", "Anna2"},
        });
    }

    @Test
    void createMultipleRecords() {
        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Alex"},
        });
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Max"},
        });
        db.seeRowsCountInTableExactly(TEST_TABLE, 2);
        db.showDataByCondition(TEST_TABLE, new Object[][]{
                {"ROLES", "manager"},
        });
    }

    @Test
    void checkShowByConditionsException() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.showDataByCondition(TEST_TABLE, new Object[][]{
                        {"ROLES", "user"},
                }));

        MatcherAssert.assertThat(
                "Exception on failed show by condition (record exists assert)",
                exception.getMessage(),
                is("No records in MYSQL_SCHEMA.USERS by conditions: where ROLES='user'"));
    }

    @Test
    void checkCountExceptionOnEmptyTable() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly(TEST_TABLE, 1));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table)",
                exception.getMessage(),
                is("Failed assert count: ER <1> with AR <0> in MYSQL_SCHEMA.USERS within 2 seconds"));
    }

    @Test
    void checkCountExceptionOnNotEmptyTable() {

        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Alex"},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly(TEST_TABLE, 2));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(filled table)",
                exception.getMessage(),
                is("Failed assert count: ER <2> with AR <1> in MYSQL_SCHEMA.USERS within 2 seconds"));
    }

    @Test
    void checkCountExceptionOnNotEmptyTableCustomAwait() {

        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Alex1"},
        });
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Alex2"},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.withAwaitMs(500).seeRowsCountInTableExactly(TEST_TABLE, 1));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(filled table) with custom timeout",
                exception.getMessage(),
                is("Failed assert count: ER <1> with AR <2> in MYSQL_SCHEMA.USERS within 500 milliseconds"));
    }

    @Test
    void checkConditionExceptionOnEmptyTable() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "5"},
                                },
                                1));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table) with custom timeout",
                exception.getMessage(),
                is("""
                        Failed assert count: ER <1> with AR <0> in MYSQL_SCHEMA.USERS within 2 seconds
                        Expected for:
                        [ID]
                        [5]
                        Actual result(s)"""));
    }

    @Test
    void checkConditionExceptionOnEmptyTableCustomAwait() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.withAwaitMs(600).seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "4"},
                                },
                                1));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table) with custom timeout",
                exception.getMessage(),
                is("""
                        Failed assert count: ER <1> with AR <0> in MYSQL_SCHEMA.USERS within 600 milliseconds
                        Expected for:
                        [ID]
                        [4]
                        Actual result(s)"""));
    }

    @Test
    void checkCustomConditionExceptionAssertCustomTO() {
        insertIntoUsers(new Object[][]{
                {"ID", "6"},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.withAwaitMs(500).seeRowsCountInTableExactlyCustom(TEST_TABLE, "where ID >7 AND DEPOSIT is null",
                        1));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table) with custom condition & timeout",
                exception.getMessage(),
                is("""
                        Failed assert count: ER <1> with AR <0> in MYSQL_SCHEMA.USERS within 500 milliseconds
                        where ID >7 AND DEPOSIT is null"""));
    }

    @Test
    void checkCondition3ElementsExceptionOnEmptyTable() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", ">=", "5"},
                                },
                                1));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table)",
                exception.getMessage(),
                is("""
                        Failed assert count: ER <1> with AR <0> in MYSQL_SCHEMA.USERS within 2 seconds
                        Expected for:
                        [ID]
                        [(>=)5]
                        Actual result(s)"""));
    }

    @Test
    void checkConditionExceptionOnFilledTable() {

        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Alex"},
                {"IS_ACTIVE", false},
        });
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Max"},
                {"IS_ACTIVE", true},
        });
        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "5"},
                                        {"USERNAME", "Stas"},
                                        {"IS_ACTIVE", true},
                                },
                                1));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table)",
                exception.getMessage(),
                is("""
                        Failed assert count: ER <1> with AR <0> in MYSQL_SCHEMA.USERS within 2 seconds
                        Expected for:
                        [ID, USERNAME, IS_ACTIVE]
                        [5, Stas, true]
                        Actual result(s)
                        [1, Alex, false]
                        [2, Max, true]"""));
    }

    @Test
    void checkBrokenExecuteException() {

        Throwable exception = assertThrows(SqlExecuteException.class, () ->
                insertIntoUsers(new Object[][]{
                        {"ID", "broken"},
                }));

        MatcherAssert.assertThat(
                "Exception on EXECUTE",
                exception.getMessage(),
                StringContains.containsString("Incorrect integer value: 'broken' for column 'ID' at row 1"));
    }

    @Test
    void checkBrokenSelectException() {

        Throwable exception = assertThrows(SqlExecuteException.class, () ->
                db.seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "IS", "broken"},
                                },
                                1));

        MatcherAssert.assertThat(
                "Exception on SELECT",
                exception.getMessage(),
                StringContains.containsString("You have an error in your SQL syntax"));
    }

    @Test
    void checkBrokenSelectListException() {

        Throwable exception = assertThrows(SqlExecuteException.class, () ->
                db.showDataByCondition(TEST_TABLE, new Object[][]{
                        {"ID", "IS", "broken"},
                }));

        MatcherAssert.assertThat(
                "Exception on SELECT list",
                exception.getMessage(),
                StringContains.containsString("You have an error in your SQL syntax"));
    }


    @Test
    void parallelTestForAwaitRecordExists() {

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> db.seeRowsCountInTableExactly(TEST_TABLE, 1));
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(this::pushWithSleep);
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> db.seeTableIsNotEmpty(TEST_TABLE));

        CompletableFuture.allOf(future1, future2, future3).join();

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

    private static final String CI = System.getenv("CI");
    private static final String PROPERTY = "bugreaperEnv";

    String getCi(){
        if(Objects.equals(CI, "true")){
            return "jdbc:mysql://docker:3308";
        }else {
            return  "jdbc:mysql://localhost:3308";
        }
    }

    @Test
    void configRequiredFieldsTest() {
        if(Objects.equals(CI, "true")){
            System.setProperty(PROPERTY, "docker");
        }else {
            System.clearProperty(PROPERTY);
        }

        dbConf.truncateTable(TEST_TABLE_FOR_TEMPLATE);

        dbConf.insertIntoTable(TEST_TABLE_FOR_TEMPLATE,
                new Object[][]{
                        {"ID", 3},
                });

        //for second call
        MySQL dbConf2 = MySQL.getInstance();

        dbConf2.seeRecordExistsInTable
                (TEST_TABLE_FOR_TEMPLATE,
                        new Object[][]{
                                {"ID", 3},
                                {"NAME", "Alex"},
                                {"AMOUNT", 10.55},
                                {"IS_ACTIVE", false},
                        });


        assertEquals(String.format("""
                        MySQL:
                            dbType=mysql
                            url=%s
                            username=mysql_user
                            password=mysql_pass
                            awaitMs=2000
                            templatesPath=templates/mysql/
                        """, getCi()),
                dbConf2.getConfigSummary());
    }

}



