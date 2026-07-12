package net.bugreaper.modules.db;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.JsonNode;
import net.bugreaper.core.utils.AllureAssert;
import net.bugreaper.core.utils.AllureResultLoader;
import net.bugreaper.core.utils.LogWatcher;
import net.bugreaper.modules.db.exceptions.SqlExecuteException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.*;
import testcontainers.MariaDbSetup;

import java.util.Objects;

import static net.bugreaper.core.filereaders.FileReader.readTextFromFile;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"squid:S2699", "java:S5778"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //To call connection once!
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MariaDbTests extends MariaDbSetup {

    private static final MariaDb db = getMariDb();
    private static final MariaDb dbConf = MariaDb.getInstance();


    private static final String TEST_TABLE = "MARIA_SCHEMA.USERS";
    private static final String TEST_TABLE2 = "MARIA_SCHEMA.USERS2";
    private static final String TEST_TABLE_FOR_TEMPLATE = "MARIA_SCHEMA.TEMP";

    private LogWatcher logWatcher;

    @BeforeAll
    static void clean() {
        AllureResultLoader.cleanResultsDir();
    }

    @BeforeEach
    void setup() {
        logWatcher = new LogWatcher("bugreaper-module-db", Level.DEBUG);
    }

    @AfterEach
    void teardown() {
        logWatcher.detach();
    }


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


    @BeforeEach
    void cleanTables() {
        db.truncateTable(TEST_TABLE);
    }

    @Test
    @Order(7)
    void getDataFromEmptyTable() {
        db.showDataFromTable(TEST_TABLE);
    }

    @Test
    @Order(8)
    void getDataFromEmptyAllure() {
        JsonNode result = AllureResultLoader.loadByTestName("getDataFromEmptyTable");

        AllureAssert.assertThat(result)
                .hasStep("(DB) Show data from table MARIA_SCHEMA.USERS")
                .hasAttachment("I see in table MARIA_SCHEMA.USERS",  readTextFromFile("testdata/allure/attachments/maria_show_empty.html"));
    }

    @Test
    void checkTableIsEmpty() {
        db.seeTableIsEmpty(TEST_TABLE);
    }


    @Test
    @Order(1)
    void fullJsonFeatureTest() {
        db.truncateTable(TEST_TABLE_FOR_TEMPLATE);

        db.insertIntoTable(TEST_TABLE_FOR_TEMPLATE,
                """
                        {
                            "ID": 1,
                            "AMOUNT": 10.55
                        }""");

        assertEquals(
                """
                        [[DEBUG] Try to execute sql:
                         TRUNCATE TABLE MARIA_SCHEMA.TEMP, [DEBUG] Looking for template file in resources: templates/mariadb/MARIA_SCHEMA.TEMP.json, [DEBUG] Try to execute sql:
                         INSERT INTO MARIA_SCHEMA.TEMP
                        (ID, NAME, AMOUNT, IS_ACTIVE)
                        VALUES (1, 'Maria', 10.55, false)]""",
                logWatcher.getLoggedEvents(Level.DEBUG).toString());

        db.seeRecordExistsInTable(TEST_TABLE_FOR_TEMPLATE,
                """
                        {
                            "ID": 1,
                            "NAME": "Maria",
                            "AMOUNT": 10.55
                        }""");

        db.seeRowsCountInTableExactly(TEST_TABLE_FOR_TEMPLATE,
                """
                        {
                            "ID": 1,
                            "NAME": "Maria",
                            "AMOUNT": 10.55
                        }""", 1);
    }

    @Test
    @Order(2)
    void fullJsonFeatureTestAllure() {
        JsonNode result = AllureResultLoader.loadByTestName("fullJsonFeatureTest");

        AllureAssert.assertThat(result)
                .hasStep("(DB) Truncate table MARIA_SCHEMA.TEMP")
                .hasAttachment("execute sql with", "TRUNCATE TABLE MARIA_SCHEMA.TEMP;")

                .hasStep("(DB) Insert into table MARIA_SCHEMA.TEMP record: [[ID, 1], [AMOUNT, 10.55]]")
                .hasAttachment("Inserted in table MARIA_SCHEMA.TEMP", readTextFromFile("testdata/allure/attachments/insert1.html"))
                .hasAttachment("execute sql with", """
                        INSERT INTO MARIA_SCHEMA.TEMP
                        (ID, NAME, AMOUNT, IS_ACTIVE)
                        VALUES (1, 'Maria', 10.55, false);""")

                .hasStep("(DB)[ASSERT] Record/s exists in MARIA_SCHEMA.TEMP where ID=1 AND NAME='Maria' AND AMOUNT=10.55")
                .hasAttachment("Expected in table MARIA_SCHEMA.TEMP", readTextFromFile("testdata/allure/attachments/assert1.html"))
                .hasAttachment("execute sql with", """
                        SELECT COUNT(*) as COUNT_RES FROM MARIA_SCHEMA.TEMP where ID=1 AND NAME='Maria' AND AMOUNT=10.55;""");
    }

    @Test
    void renameAndRollbackTable() {
        db.showDataFromTable("MARIA_SCHEMA.TEST");
        db.renameTable("MARIA_SCHEMA.TEST");
        db.showDataFromTable("MARIA_SCHEMA.TEST_X_");

        db.rollbackTable("MARIA_SCHEMA.TEST");
        db.showDataFromTable("MARIA_SCHEMA.TEST");
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
    @Order(9)
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

        db.showDataFromTable(TEST_TABLE);
    }

    @Test
    @Order(10)
    void checkRunScriptAndAllureAssert() {
        JsonNode result = AllureResultLoader.loadByTestName("checkRunScriptAndAssert");

        AllureAssert.assertThat(result)
                .hasStep("(DB) Show data from table MARIA_SCHEMA.USERS")
                .hasAttachment("I see in table MARIA_SCHEMA.USERS",  readTextFromFile("testdata/allure/attachments/maria_show_all.html"));
    }

    @Test
    void checkUpdateArraysAndAssert() {
        insertIntoUsers(new Object[][]{
                {"ID", 2},
                {"USERNAME", "Pupkin2"},
        });

        db.updateTable(
                TEST_TABLE,
                new Object[][]{
                        {"USERNAME", "Vova"},
                },
                new Object[][]{
                        {"ID", 2},
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
                {"ID", 4},
                {"USERNAME", "Pupkin"},
        });

        db.updateTable(
                TEST_TABLE,
                new Object[][]{
                        {"ID", 5},
                        {"USERNAME", "Vova_one"},
                }
        );

        db.seeRowsCountInTableExactly(TEST_TABLE, 1);

        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", 5},
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

        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "1"},
                                {"DATA", "JSON_EQUALS", data},
                                {"ROLES", "manager"},
                        },
                        1);
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
    void checkLongType() {
        insertIntoUsers(new Object[][]{
                {"ID", 9223372036854775807L},
                {"USERNAME", "Pupkin"},
                {"IS_ACTIVE", true},
        });

        db.seeRecordExistsInTable
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", 9223372036854775807L },
                        });
    }

    @Test
    void runFromFile() {
        db.truncateTable(TEST_TABLE2);

        db.runScriptFromFile("dump/test_script_maria.sql");

        assertEquals(
                "[[INFO] Script executed from: dump/test_script_maria.sql]",
                logWatcher.getLoggedEvents(Level.INFO).toString());

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
    void showDataByConditionTest() {
        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Alex"},
        });

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
                is("No records in MARIA_SCHEMA.USERS by conditions: where ROLES='user'"));
    }

    @Test
    void checkCountExceptionOnEmptyTable() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly(TEST_TABLE, 1));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table)",
                exception.getMessage(),
                is("Failed assert count: ER <1> with AR <0> in MARIA_SCHEMA.USERS within 2 seconds"));
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
                is("Failed assert count: ER <2> with AR <1> in MARIA_SCHEMA.USERS within 2 seconds"));
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
                is("Failed assert count: ER <1> with AR <2> in MARIA_SCHEMA.USERS within 500 milliseconds"));
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
                        Failed assert count: ER <1> with AR <0> in MARIA_SCHEMA.USERS within 2 seconds
                        Expected for:
                        [ID]
                        [5]
                        Actual result(s)"""));
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

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table) with custom timeout",
                exception.getMessage(),
                is("""
                        Failed assert count: ER <1> with AR <0> in MARIA_SCHEMA.USERS within 600 milliseconds
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
                        Failed assert count: ER <1> with AR <0> in MARIA_SCHEMA.USERS within 500 milliseconds
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
                        Failed assert count: ER <1> with AR <0> in MARIA_SCHEMA.USERS within 2 seconds
                        Expected for:
                        [ID]
                        [(>=)5]
                        Actual result(s)"""));
    }

    @Test
    @Order(3)
    void checkConditionExceptionOnFilledTable() {

        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Alex"},
        });
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Max"},
        });
        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "5"},
                                        {"USERNAME", "Stas"},
                                },
                                1));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table)",
                exception.getMessage(),
                is("""
                        Failed assert count: ER <1> with AR <0> in MARIA_SCHEMA.USERS within 2 seconds
                        Expected for:
                        [ID, USERNAME]
                        [5, Stas]
                        Actual result(s)
                        [1, Alex]
                        [2, Max]"""));
    }

    @Test
    @Order(4)
    void checkConditionExceptionOnFilledTableAllure() {
        JsonNode result = AllureResultLoader.loadByTestName("checkConditionExceptionOnFilledTable");

        AllureAssert.assertThat(result)
                .hasStep("(DB)[ASSERT] Count records in MARIA_SCHEMA.USERS where ID='5' AND USERNAME='Stas' EQUAL 1")
                .hasAttachment("I see in table MARIA_SCHEMA.USERS", readTextFromFile("testdata/allure/attachments/failed1.html"));
    }

    @Test
    @Order(5)
    void checkConditionExceptionExistsOnFilledTable() {

        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Alex"},
        });
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Max"},
        });
        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRecordExistsInTable
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "5"},
                                        {"USERNAME", "Stas"},
                                }));

        MatcherAssert.assertThat(
                "Exception on failed assert count in table(empty table)",
                exception.getMessage(),
                is("""
                        Failed: record/s not found in <MARIA_SCHEMA.USERS> within 2 seconds
                        Expected for:
                        [ID, USERNAME]
                        [5, Stas]
                        Actual result(s)
                        [1, Alex]
                        [2, Max]"""));
    }

    @Test
    @Order(6)
    void checkConditionExceptionExistsOnFilledTableAllure() {
        JsonNode result = AllureResultLoader.loadByTestName("checkConditionExceptionExistsOnFilledTable");

        AllureAssert.assertThat(result)
                .hasStep("(DB)[ASSERT] Record/s exists in MARIA_SCHEMA.USERS where ID='5' AND USERNAME='Stas'")
                .hasAttachment("I see in table MARIA_SCHEMA.USERS", readTextFromFile("testdata/allure/attachments/failed1.html"));
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
                StringContains.containsString("Incorrect integer value: 'broken' for column `MARIA_SCHEMA`.`USERS`.`ID`"));
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

    private static final String CI = System.getenv("CI");
    private static final String PROPERTY = "bugreaperEnv";

    String getCi(){
        if(Objects.equals(CI, "true")){
            return "jdbc:mariadb://docker:3307";
        }else {
            return  "jdbc:mariadb://localhost:3307";
        }
    }

    @Test
    void configAllFieldsTest() {
        if(Objects.equals(CI, "true")){
            System.setProperty(PROPERTY, "docker");
        }else {
            System.clearProperty(PROPERTY);
        }

        dbConf.truncateTable(TEST_TABLE_FOR_TEMPLATE);

        dbConf.insertIntoTable(TEST_TABLE_FOR_TEMPLATE,
            new Object[][]{
                    {"ID", 2},
        });

        //for second call
        MariaDb dbConf2 = MariaDb.getInstance();

        dbConf2.seeRecordExistsInTable
                (TEST_TABLE_FOR_TEMPLATE,
                        new Object[][]{
                                {"ID", 2},
                                {"NAME", "Maria"},
                                {"AMOUNT", 10.55},
                                {"IS_ACTIVE", false},
                        });


        Assertions.assertEquals(String.format("""
                        MariaDb:
                            dbType=mariadb
                            url=%s
                            username=test_user
                            password=secret_pass
                            awaitMs=510
                            templatesPath=templates/mariadb/
                        """, getCi()),
                dbConf2.getConfigSummary());
    }

}
