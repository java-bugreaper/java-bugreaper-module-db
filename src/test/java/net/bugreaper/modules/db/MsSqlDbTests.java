package net.bugreaper.modules.db;

import net.bugreaper.core.utils.AllureResultLoader;
import net.bugreaper.modules.db.exceptions.QueryBuilderException;
import net.bugreaper.modules.db.exceptions.SqlExecuteException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import testcontainers.MsSqlDbSetup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static testcontainers.DockerSetup.CI_HOST;

@SuppressWarnings({"squid:S2699", "java:S5778"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //To call connection once!
class MsSqlDbTests extends MsSqlDbSetup {

    private static final MsSQL db = getMssqlDb();
    private static final MsSQL dbConf = MsSQL.getInstance();

    private static final String TEST_TABLE = "test_schema.users";
    private static final String TEST_TABLE2 = "test_schema.USERS2";


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
                {"IS_ACTIVE", 0},
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

    @AfterAll
    static void shutDown() {
        DB_CONTAINER.stop();
    }

    @BeforeEach
    void cleanTables() {
        db.truncateTable(TEST_TABLE);
    }

    @Test
    void getDataFromEmptyTable() {
        db.showDataFromTable(TEST_TABLE);
    }

    @Test
    void renameAndRollbackTable() {
        db.showDataFromTable("test_schema.test");
        db.renameTable("test_schema.test");
        db.showDataFromTable("test_schema.test_X_");

        db.rollbackTable("test_schema.test");
        db.showDataFromTable("test_schema.test");
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
                {"ID", 2},
                {"USERNAME", "Pupkin2"},
        });

        insertIntoUsers(new Object[][]{
                {"ID", "3"},
                {"USERNAME", "Pupkin3"},
                {"IS_ACTIVE", 1},
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
                                {"ID", 3},
                                {"USERNAME", "Pupkin3"},
                                {"IS_ACTIVE", 1},
                        },
                        1);
    }

    @Test
    void checkDeleteStringAndAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", 5},
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
    void checkCustomConditionAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", "6"},
        });

        db.seeRowsCountInTableExactlyCustom(TEST_TABLE, "where ID >=1 AND DEPOSIT is null", 1);
    }

    @Test
    @EnabledIfSystemProperty(named = "dockerTestVersion", matches = "latest")
    void checkRegex() {
        insertIntoUsers(
                new Object[][]{
                        {"ID", 7677},
                        {"PASSWORD", "7134"},
                });

        db.seeRecordExistsInTable
                (TEST_TABLE,
                        new Object[][]{
                                {"PASSWORD", "REGEX_MATCH", "[0-9]{4}"},
                        });
    }

    @Test
    void runFromFile() {
        db.truncateTable(TEST_TABLE2);

        db.runScriptFromFile("dump/test_script_mssql.sql");

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
                        ERROR: wrong 3 elements operator 'JSON_CONTAINS' for database 'sqlserver' supported basic [=, !=, <>, >, >=, <, <=, IS, IS NOT, LIKE] or specific REGEX_MATCH""",
                exception.getMessage(),
                "Exception with JSON in db not supported yet");
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

        assertEquals("Failed: no records found in table 'test_schema.users' where ROLES='user'",
                exception.getMessage());
    }

    @Test
    void checkCountExceptionOnEmptyTable() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly(TEST_TABLE, 1));

        assertEquals("Expected EXACTLY <1> records in table 'test_schema.users', but got <0> within 2 seconds",
                exception.getMessage());

    }

    @Test
    void checkCountExceptionOnNotEmptyTable() {

        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Alex"},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly(TEST_TABLE, 2));

        assertEquals("Expected EXACTLY <2> records in table 'test_schema.users', but got <1> within 2 seconds",
                exception.getMessage());
    }

    @Test
    void checkCountExceptionOnNotEmptyTableCustomTime() {

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

        assertEquals("""
                        Expected EXACTLY <1> records in table 'test_schema.users', but got <2> within 500 milliseconds""",
                exception.getMessage());
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

        assertEquals("""
                        Expected EXACTLY <1> records in table 'test_schema.users', but got <0> within 2 seconds
                        Expected for:
                        [ID]
                        [5]
                        Actual result(s):""",
                exception.getMessage());
    }

    @Test
    void checkConditionExceptionOnEmptyTableCustomTO() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.withAwaitMs(600).seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "4"},
                                },
                                1));

        assertEquals("""
                        Expected EXACTLY <1> records in table 'test_schema.users', but got <0> within 600 milliseconds
                        Expected for:
                        [ID]
                        [4]
                        Actual result(s):""",
                exception.getMessage());
    }

    @Test
    void checkCustomConditionExceptionAssertCustomTimeOnce() {
        insertIntoUsers(new Object[][]{
                {"ID", "6"},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.withAwaitMs(500).seeRowsCountInTableExactlyCustom(TEST_TABLE, "where ID >7 AND DEPOSIT is null",
                        1));

        assertEquals("""
                        Expected EXACTLY <1> records in table 'test_schema.users', but got <0> within 500 milliseconds
                        where ID >7 AND DEPOSIT is null""",
                exception.getMessage());

        Throwable exception2 = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactlyCustom(TEST_TABLE, "where ID >7 AND DEPOSIT is null",
                        1));

        assertEquals("""
                        Expected EXACTLY <1> records in table 'test_schema.users', but got <0> within 2 seconds
                        where ID >7 AND DEPOSIT is null""",
                exception2.getMessage());
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

        assertEquals("""
                        Expected EXACTLY <1> records in table 'test_schema.users', but got <0> within 2 seconds
                        Expected for:
                        [ID]
                        [(>=)5]
                        Actual result(s):""",
                exception.getMessage());
    }

    @Test
    void checkConditionExceptionOnFilledTable() {

        insertIntoUsers(new Object[][]{
                {"ID", 1},
                {"USERNAME", "Alex"},
                {"IS_ACTIVE", 0},
        });
        insertIntoUsers(new Object[][]{
                {"ID", 2},
                {"USERNAME", "Max"},
                {"IS_ACTIVE", 1},
        });
        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", 5},
                                        {"USERNAME", "Stas"},
                                        {"IS_ACTIVE", 1},
                                },
                                1));

        assertEquals("""
                        Expected EXACTLY <1> records in table 'test_schema.users', but got <0> within 2 seconds
                        Expected for:
                        [ID, USERNAME, IS_ACTIVE]
                        [5, Stas, 1]
                        Actual result(s):
                        [1, Alex, false]
                        [2, Max, true]""",
                exception.getMessage());
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
                StringContains.containsString("Conversion failed when converting the varchar value 'broken'"));
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
                StringContains.containsString("Incorrect syntax"));
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
                StringContains.containsString("Incorrect syntax near 'broken'"));
    }

    @Test
    void seeRecordExistsInTableTest() {
        insertIntoUsers(new Object[][]{
                {"ID", 222},
                {"USERNAME", "Pupkin2"},
                {"IS_ACTIVE", 0},
                {"DEPOSIT", null},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRecordExistsInTable(TEST_TABLE,
                        new Object[][]{
                                {"ID", 1},
                                {"USERNAME", "Pupkin2"},
                                {"IS_ACTIVE", 0},
                                {"DEPOSIT", 777},
                        }));

        assertEquals("""
                        Failed: no records found in table 'test_schema.users' within 2 seconds
                        Expected for:
                        [ID, USERNAME, IS_ACTIVE, DEPOSIT]
                        [1, Pupkin2, 0, 777]
                        Actual result(s):
                        [222, Pupkin2, false, null]""",
                exception.getMessage(),
                "Exception on seeRecordExistsInTable");
    }


    private static final String TEST_TABLE_FOR_TEMPLATE = "test_schema.temp";

    @Test
    void configAllFieldsTest() {

        dbConf.truncateTable(TEST_TABLE_FOR_TEMPLATE);

        dbConf.insertIntoTable(TEST_TABLE_FOR_TEMPLATE,
                new Object[][]{
                        {"ID", 2},
                });

        //for second call
        MsSQL dbConf2 = MsSQL.getInstance();

        dbConf2.seeRecordExistsInTable
                (TEST_TABLE_FOR_TEMPLATE,
                        new Object[][]{
                                {"ID", 2},
                                {"USERNAME", "Test"},
                                {"STATE", 1},
                        });


        assertEquals(String.format("""
                        MsSQL:
                            dbType=sqlserver
                            url=jdbc:sqlserver://%s:1435;encrypt=false
                            username=sa
                            password=YourStrong(!)Password
                            awaitMs=250
                            awaitPollInterval=70
                            templatesPath=templates/sqlserver/
                            enableEnchantedReport=true
                        """, CI_HOST),
                dbConf2.getConfigSummary());
    }
}



