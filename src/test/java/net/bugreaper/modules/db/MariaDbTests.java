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

import static net.bugreaper.core.filereaders.FileReader.readTextFromFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static testcontainers.DockerSetup.CI_HOST;

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

    @AfterAll
    static void shutDown() {
        DB_CONTAINER.stop();
    }

    @BeforeEach
    void setup() {
        db.truncateTable(TEST_TABLE);
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
                .hasStep("(DB) Show data from table 'MARIA_SCHEMA.USERS'")
                .hasAttachment("Table 'MARIA_SCHEMA.USERS' data:", readTextFromFile("testdata/allure/attachments/maria_show_empty.html"));
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
                .hasStep("(DB) Truncate table 'MARIA_SCHEMA.TEMP'")
                .hasAttachment("Execute SQL:", "TRUNCATE TABLE MARIA_SCHEMA.TEMP;")

                .hasStep("(DB) Insert into table 'MARIA_SCHEMA.TEMP' record: [[ID, 1], [AMOUNT, 10.55]]")
                .hasAttachment("Inserted record:", readTextFromFile("testdata/allure/attachments/insert1.html"))
                .hasAttachment("Execute SQL:", """
                        INSERT INTO MARIA_SCHEMA.TEMP
                        (ID, NAME, AMOUNT, IS_ACTIVE)
                        VALUES (1, 'Maria', 10.55, false);""")

                .hasStep("(DB)[ASSERT] Table: 'MARIA_SCHEMA.TEMP' contains EXACTLY <1> record/s where ID=1 AND NAME='Maria' AND AMOUNT=10.55")
                .hasAttachment("Expected record:", readTextFromFile("testdata/allure/attachments/assert1.html"))
                .hasAttachment("Execute SQL:", """
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
                .hasStep("(DB) Show data from table 'MARIA_SCHEMA.USERS'")
                .hasAttachment("Table 'MARIA_SCHEMA.USERS' data:", readTextFromFile("testdata/allure/attachments/maria_show_all.html"));
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
    void createRecordJsonContainsTest() {

        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"DATA", """
                [{"id":1,"text":"test"}, {"id":2,"text":"test2"}]"""},
        });
        db.seeRowsCountInTableExactly(TEST_TABLE, 1);
        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "1"},
                                {"DATA", "JSON_CONTAINS", """
                                                    [{"id":1}]"""},
                        },
                        1);
    }

    @Test
    void createRecordJsonEqualsTest() {

        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"DATA", """
                [{"id":1,"text":"test"}, {"id":2,"text":"test2"}]"""},
        });
        db.seeRowsCountInTableExactly(TEST_TABLE, 1);

        db.seeRowsCountInTableExactly
                (TEST_TABLE,
                        new Object[][]{
                                {"ID", "1"},
                                {"DATA", "JSON_EQUALS", """
                                    [{"id":1,"text":"test"}, {"text":"test2", "id":2}]"""},
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
                                {"ID", 9223372036854775807L},
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

        assertEquals("Failed: no records found in table 'MARIA_SCHEMA.USERS' where ROLES='user'",
                exception.getMessage());
    }

    @Test
    void checkCountExceptionOnEmptyTable() {

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRowsCountInTableExactly(TEST_TABLE, 1));

        assertEquals("Expected EXACTLY <1> records in table 'MARIA_SCHEMA.USERS', but got <0> within 2 seconds",
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

        assertEquals("Expected EXACTLY <2> records in table 'MARIA_SCHEMA.USERS', but got <1> within 2 seconds",
                exception.getMessage());
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

        assertEquals("Expected EXACTLY <1> records in table 'MARIA_SCHEMA.USERS', but got <2> within 500 milliseconds",
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
                        Expected EXACTLY <1> records in table 'MARIA_SCHEMA.USERS', but got <0> within 2 seconds
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
                        Expected EXACTLY <1> records in table 'MARIA_SCHEMA.USERS', but got <0> within 600 milliseconds
                        Expected for:
                        [ID]
                        [4]
                        Actual result(s):""",
                exception.getMessage(),
                "Exception on failed assert count in table(empty table) with custom timeout");
    }

    @Test
    void checkCustomConditionExceptionAssertCustomTO() {
        insertIntoUsers(new Object[][]{
                {"ID", "6"},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.withAwaitMs(500).seeRowsCountInTableExactlyCustom(TEST_TABLE, "where ID >7 AND DEPOSIT is null",
                        1));

        assertEquals("""
                        Expected EXACTLY <1> records in table 'MARIA_SCHEMA.USERS', but got <0> within 500 milliseconds
                        where ID >7 AND DEPOSIT is null""",
                exception.getMessage());
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
                        Expected EXACTLY <1> records in table 'MARIA_SCHEMA.USERS', but got <0> within 2 seconds
                        Expected for:
                        [ID]
                        [(>=)5]
                        Actual result(s):""",
                exception.getMessage(),
                "Exception on failed assert count in table(empty table)");
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


        assertEquals("""
                        Expected EXACTLY <1> records in table 'MARIA_SCHEMA.USERS', but got <0> within 2 seconds
                        Expected for:
                        [ID, USERNAME]
                        [5, Stas]
                        Actual result(s):
                        [1, Alex]
                        [2, Max]""",
                exception.getMessage(),
                "Exception on failed assert count in table(empty table)");
    }

    @Test
    @Order(4)
    void checkConditionExceptionOnFilledTableAllure() {
        JsonNode result = AllureResultLoader.loadByTestName("checkConditionExceptionOnFilledTable");

        AllureAssert.assertThat(result)
                .hasStep("(DB)[ASSERT] Table: 'MARIA_SCHEMA.USERS' contains EXACTLY <1> record/s where ID='5' AND USERNAME='Stas'")
                .hasAttachment("Table 'MARIA_SCHEMA.USERS' data:", readTextFromFile("testdata/allure/attachments/failed1.html"));
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

        assertEquals("""
                        Failed: no records found in table 'MARIA_SCHEMA.USERS' within 2 seconds
                        Expected for:
                        [ID, USERNAME]
                        [5, Stas]
                        Actual result(s):
                        [1, Alex]
                        [2, Max]""",
                exception.getMessage(),
                "Exception on failed assert exists in table(empty table)");
    }

    @Test
    @Order(6)
    void checkConditionExceptionExistsOnFilledTableAllure() {
        JsonNode result = AllureResultLoader.loadByTestName("checkConditionExceptionExistsOnFilledTable");

        AllureAssert.assertThat(result)
                .hasStep("(DB)[ASSERT] Table 'MARIA_SCHEMA.USERS' contains records where ID='5' AND USERNAME='Stas'")
                .hasAttachment("Table 'MARIA_SCHEMA.USERS' data:", readTextFromFile("testdata/allure/attachments/failed1.html"));
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


    @Test
    void configAllFieldsTest() {


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
                            url=jdbc:mariadb://%s:3307?connectTimeout=5001
                            username=test_user
                            password=secret_pass
                            awaitMs=510
                            awaitPollInterval=200
                            templatesPath=templates/mariadb/
                            enableEnchantedReport=true
                        """, CI_HOST),
                dbConf2.getConfigSummary());
    }

    @Test
    void configDataSourcePropertiesTest() {
        String dataSourceConfig = dbConf.getDataSourceProperties();

        MatcherAssert.assertThat(
                "provided properties",
                dataSourceConfig,
                StringContains.containsString("connectTimeout=3001"));

        MatcherAssert.assertThat(
                "provided properties",
                dataSourceConfig,
                StringContains.containsString("socketTimeout=3002"));
    }

    @Test
    void configHikariSettingsTest() {
        String hikariSettings = dbConf.getHikariSettings();

        MatcherAssert.assertThat(
                hikariSettings,
                StringContains.containsString("poolName=mariadb-pool"));

        MatcherAssert.assertThat(
                hikariSettings,
                StringContains.containsString("minimumIdle=2"));

        MatcherAssert.assertThat(
                hikariSettings,
                StringContains.containsString("maximumPoolSize=4"));

        MatcherAssert.assertThat(
                hikariSettings,
                StringContains.containsString("maxLifetime=600000"));

        MatcherAssert.assertThat(
                hikariSettings,
                StringContains.containsString("keepaliveTime=110000"));

        MatcherAssert.assertThat(
                hikariSettings,
                StringContains.containsString("connectionTimeout=10000"));

        MatcherAssert.assertThat(
                hikariSettings,
                StringContains.containsString("validationTimeout=9000"));

        MatcherAssert.assertThat(
                hikariSettings,
                StringContains.containsString("idleTimeout=10555"));

        MatcherAssert.assertThat(
                hikariSettings,
                StringContains.containsString("leakDetectionThreshold=0"));

        MatcherAssert.assertThat(
                hikariSettings,
                StringContains.containsString("readOnly=false"));
    }


    // off enchanted report
    @Test
    @Order(11)
    void checkConditionExceptionWithDisabledEnchantedReport() {

        MariaDb dbNoEnch = getMariDb().setEnchantedReport(false).setAwaitMs(200);

        insertIntoUsers(new Object[][]{
                {"ID", "1"},
                {"USERNAME", "Alex"},
        });
        insertIntoUsers(new Object[][]{
                {"ID", "2"},
                {"USERNAME", "Max"},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                dbNoEnch.seeRowsCountInTableExactly
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "5"},
                                        {"USERNAME", "Stas"},
                                },
                                1));


        assertEquals("""
                        Expected EXACTLY <1> records in table 'MARIA_SCHEMA.USERS', but got <0> within 200 milliseconds
                        Expected for:
                        [ID, USERNAME]
                        [5, Stas]
                        Actual result(s):
                        enableEnchantedReport disabled: actual result was not requested.""",
                exception.getMessage());


        Throwable exception2 = assertThrows(AssertionError.class, () ->
                dbNoEnch.seeRecordExistsInTable
                        (TEST_TABLE,
                                new Object[][]{
                                        {"ID", "5"},
                                        {"USERNAME", "Stas"},
                                }));

        assertEquals("""
                        Failed: no records found in table 'MARIA_SCHEMA.USERS' within 200 milliseconds
                        Expected for:
                        [ID, USERNAME]
                        [5, Stas]
                        Actual result(s):
                        enableEnchantedReport disabled: actual result was not requested.""",
                exception2.getMessage());
    }

    @Test
    @Order(12)
    void checkConditionExceptionWithDisabledEnchantedReportAllureAssert() {
        JsonNode result = AllureResultLoader.loadByTestName("checkConditionExceptionWithDisabledEnchantedReport");

        Throwable exception = assertThrows(AssertionError.class, () ->
                AllureAssert.assertThat(result)
                        .hasStep("(DB)[ASSERT] Table: 'MARIA_SCHEMA.USERS' contains EXACTLY <1> record/s where ID='5' AND USERNAME='Stas'")
                        .hasAttachment("Table 'MARIA_SCHEMA.USERS' data:"));

        assertEquals("Attachment not found: Table 'MARIA_SCHEMA.USERS' data: ==> expected: <true> but was: <false>",
                exception.getMessage());


        Throwable exception2 = assertThrows(AssertionError.class, () ->
                AllureAssert.assertThat(result)
                        .hasStep("(DB)[ASSERT] Table 'MARIA_SCHEMA.USERS' contains records where ID='5' AND USERNAME='Stas'")
                        .hasAttachment("Table 'MARIA_SCHEMA.USERS' data:"));

        assertEquals("Attachment not found: Table 'MARIA_SCHEMA.USERS' data: ==> expected: <true> but was: <false>",
                exception2.getMessage());
    }

}
