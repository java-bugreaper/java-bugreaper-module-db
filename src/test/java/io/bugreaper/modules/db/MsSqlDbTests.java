package io.bugreaper.modules.db;

import io.bugreaper.modules.db.exceptions.QueryBuilderException;
import io.bugreaper.modules.db.exceptions.SqlExecuteException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import testcontainers.MsSqlDbSetup;

import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("squid:S2699")
class MsSqlDbTests {

    private static final MsSqlDbSetup setup = MsSqlDbSetup.getInstance();
    private static final MsSQL db = setup.getMssqlDb();

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


    @BeforeEach
    void cleanTables() {
        db.truncateTable(TEST_TABLE);
    }

    @Test
    void getDataFromEmptyTable() {
        db.showDataFromTableMethod(TEST_TABLE);
    }

    @Test
    void renameAndRollbackTable() {
        db.showDataFromTableMethod("test_schema.test");
        db.renameTable("test_schema.test");
        db.showDataFromTableMethod("test_schema.test_X_");

        db.rollbackTable("test_schema.test");
        db.showDataFromTableMethod("test_schema.test");
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
                        ERROR, wrong 3 elements: JSON_CONTAINS must be: [=, !=, <>, >, >=, <, <=, IS, IS NOT, LIKE]""",
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

        MatcherAssert.assertThat(
                "Exception on failed show by condition (record exists assert)",
                exception.getMessage(),
                is("No records in test_schema.users by conditions: where ROLES='user'"));
    }

    @Test
    void checkCountExceptionOnEmptyTable() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly(TEST_TABLE, 1));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table)",
                exception.getMessage(),
                is("Failed assert count: ER <1> with AR <0> in test_schema.users within 2 seconds"));
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
                is("Failed assert count: ER <2> with AR <1> in test_schema.users within 2 seconds"));
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
                db.seeRowsCountInTableExactly(TEST_TABLE, 1, 500));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(filled table) with custom timeout",
                exception.getMessage(),
                is("Failed assert count: ER <1> with AR <2> in test_schema.users within 500 milliseconds"));
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
                        Failed assert count: ER <1> with AR <0> in test_schema.users within 2 seconds
                        Expected for:
                        [ID]
                        [5]
                        Actual result(s)"""));
    }

    @Test
    void checkConditionExceptionOnEmptyTableCustomTO() {

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
                        Failed assert count: ER <1> with AR <0> in test_schema.users within 600 milliseconds
                        Expected for:
                        [ID]
                        [4]
                        Actual result(s)"""));
    }

    @Test
    void checkCustomConditionExceptionAssertCustomTime() {
        insertIntoUsers(new Object[][]{
                {"ID", "6"},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactlyCustom(TEST_TABLE, "where ID >7 AND DEPOSIT is null",
                        1, 500));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table) with custom condition & timeout",
                exception.getMessage(),
                is("""
                        Failed assert count: ER <1> with AR <0> in test_schema.users within 500 milliseconds
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
                        Failed assert count: ER <1> with AR <0> in test_schema.users within 2 seconds
                        Expected for:
                        [ID]
                        [(>=)5]
                        Actual result(s)"""));
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

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table)",
                exception.getMessage(),
                is("""
                        Failed assert count: ER <1> with AR <0> in test_schema.users within 2 seconds
                        Expected for:
                        [ID, USERNAME, IS_ACTIVE]
                        [5, Stas, 1]
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

        Throwable exception = assertThrows(AssertionFailedError.class, () ->
                db.seeRecordExistsInTable(TEST_TABLE,
                        new Object[][]{
                                {"ID", 1},
                                {"USERNAME", "Pupkin2"},
                                {"IS_ACTIVE", 0},
                                {"DEPOSIT", 777},
                        }));

        assertEquals("""
                        Failed: record/s not found in <test_schema.users> within 2 seconds
                        Expected for:
                        [ID, USERNAME, IS_ACTIVE, DEPOSIT]
                        [1, Pupkin2, 0, 777]
                        Actual result(s)
                        [222, Pupkin2, false, null]""",
                exception.getMessage(),
                "Exception on seeRecordExistsInTable");
    }

    private static final String CI = System.getenv("CI");
    private static final String PROPERTY = "bugreaperEnv";
    private static final String TEST_TABLE_FOR_TEMPLATE = "test_schema.temp";


    String getCi(){
        if(Objects.equals(CI, "true")){
            return "jdbc:sqlserver://docker:1435;encrypt=false";
        }else {
            return  "jdbc:sqlserver://localhost:1435;encrypt=false";
        }
    }

    @Test
    void configAllFieldsTest() {
        if(Objects.equals(CI, "true")){
            System.setProperty(PROPERTY, "docker");
        }else {
            System.clearProperty(PROPERTY);
        }

        MsSQL dbConf = new MsSQL();

        dbConf.truncateTable(TEST_TABLE_FOR_TEMPLATE);

        dbConf.insertIntoTable(TEST_TABLE_FOR_TEMPLATE,
                new Object[][]{
                        {"ID", 2},
                });


        dbConf.seeRecordExistsInTable
                (TEST_TABLE_FOR_TEMPLATE,
                        new Object[][]{
                                {"ID", 2},
                                {"USERNAME", "Test"},
                                {"STATE", 1},
                        });


        assertEquals(String.format("""
                        MsSQL:
                            dbType=sqlserver
                            url=%s
                            username=sa
                            password=YourStrong(!)Password
                            awaitMs=250
                            templatesPath=templates/sqlserver/
                        """, getCi()),
                dbConf.getConfigSummary());
    }
}



