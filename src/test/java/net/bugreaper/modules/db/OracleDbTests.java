package net.bugreaper.modules.db;

import net.bugreaper.core.utils.AllureResultLoader;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.*;
import testcontainers.OracleDbSetup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static testcontainers.DockerSetup.CI_HOST;

@SuppressWarnings("squid:S2699")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //To call connection once!
class OracleDbTests extends OracleDbSetup {


    private static final OracleDb db = getOracleDb().setAwaitMs(500);
    private static final OracleDb dbConf = OracleDb.getInstance(); //For config

    private static final String TEST_TABLE = "ORACLE_SCHEMA.GOODS";
    private static final String TEST_TABLE2 = "ORACLE_SCHEMA_2.COUNTRIES";
    private static final String TEST_FOR_RENAME = "ORACLE_SCHEMA.USERS";
    private static final String TEST_FOR_TEMPLATE = "ORACLE_SCHEMA.TEMP";


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


    public void insertIntoGoods(Object[][] testArray) {

        final Object[][] template = {
                {"ID", "200"},
                {"CATEGORY_ID", "1"},
                {"NAME", "dummy_category"},
                {"PRICE", "175"},
                {"DATA", "{}"},
                {"CREATE_DATE", "2020-01-01 10:00:00", "TIMESTAMP"},
                {"UPDATE_DATE", "2020-01-01 11:00:00", "TIMESTAMP"},
        };
        db.insertIntoTable(TEST_TABLE, template, testArray);
    }

    public void insertIntoCountries(Object[][] testArray) {

        final Object[][] template = {
                {"ID", "200"},
                {"NAME", "Ukraine"},
                {"DESCRIPTION", "some description"},
                {"IS_EUROPE", "1"},
                {"CREATE_DATE", "2020-01-01 10:00:00", "TIMESTAMP"},
        };
        db.insertIntoTable(TEST_TABLE2, template, testArray);
    }

    @Test
    void templateFileInsertTest() {

        db.truncateTable(TEST_FOR_TEMPLATE);

        db.insertIntoTable(TEST_FOR_TEMPLATE,
                new Object[][]{
                        {"ID", 2},
                });

        db.seeRecordExistsInTable
                (TEST_FOR_TEMPLATE,
                        new Object[][]{
                                {"ID", 2},
                                {"NAME", "Alex"},
                                {"AMOUNT", 10.55},
                                {"CREATE_DATE", "=", "TIMESTAMP", "2020-02-02 12:00:00"},
                        });
    }


    @Test
    void checkRegex() {
        db.insertIntoTable(TEST_FOR_TEMPLATE,
                new Object[][]{
                        {"ID", 7134},
                });

        db.seeRecordExistsInTable
                (TEST_FOR_TEMPLATE,
                        new Object[][]{
                                {"ID", "REGEX_MATCH", "[0-9]{4}"},
                        });
    }


    @Test
    void createRecordJsonEqualsTest() {

        insertIntoGoods(
                new Object[][]{
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
                                    [{"text":"test", "id":1}, {"text":"test2", "id":2}]"""},
                        },
                        1);
    }

    @Test
    void getConfigSummaryTest() {

        assertEquals(String.format("""
                        OracleDb:
                            dbType=oracle
                            url=jdbc:oracle:thin:@%s:1521/xepdb1
                            username=ADMIN
                            password=ADMIN
                            awaitMs=500
                            awaitPollInterval=100
                            templatesPath=templates/oracle/
                            enableEnchantedReport=true
                        """, CI_HOST),
                db.getConfigSummary());
    }

    @Test
    void fullJsonFeatureTest() {
        db.truncateTable(TEST_FOR_TEMPLATE);

        db.insertIntoTable(TEST_FOR_TEMPLATE,
                """
                        {
                          "ID": 2,
                          "CREATE_DATE": "2020-02-02 12:00:00"
                        }""");

        db.seeRecordExistsInTable(TEST_FOR_TEMPLATE,
                """
                        {
                            "ID": 2,
                            "NAME": "Alex",
                            "AMOUNT": 10.55,
                            "CREATE_DATE:=:TIMESTAMP": "2020-02-02 12:00:00"
                        }""");

        db.seeRowsCountInTableExactly(TEST_FOR_TEMPLATE,
                """
                        {
                            "ID": 2,
                            "NAME": "Alex"
                        }""", 1);
    }

    @Test
    void fullJsonFeature2Test() {
        db.truncateTable(TEST_FOR_TEMPLATE);

        db.insertIntoTable(TEST_FOR_TEMPLATE,
                """
                        {
                          "ID": 2,
                          "CREATE_DATE:DATE": "2020-02-02"
                        }""");

        db.seeRecordExistsInTable(TEST_FOR_TEMPLATE,
                """
                        {
                            "ID": 2,
                            "NAME": "Alex",
                            "AMOUNT": 10.55,
                            "CREATE_DATE:=:TIMESTAMP": "2020-02-02 00:00:00"
                        }""");

        db.seeRowsCountInTableExactly(TEST_FOR_TEMPLATE,
                """
                        {
                            "ID": 2,
                            "NAME": "Alex",
                            "CREATE_DATE:=:DATE": "2020-02-02"
                        }""", 1);
    }

    @Test
    void configAllFieldsTest() {
        db.getDataSourceProperties();
        db.getHikariSettings();
        dbConf.truncateTable(TEST_FOR_TEMPLATE);

        dbConf.insertIntoTable(TEST_FOR_TEMPLATE,
                """
                        {
                          "ID": 2,
                          "CREATE_DATE:TIMESTAMP": "2020-02-02 12:00:00"
                        }""");

        dbConf.withAwaitMs(1000).seeRecordExistsInTable
                (TEST_FOR_TEMPLATE,
                        new Object[][]{
                                {"ID", 2},
                                {"NAME", "Alex"},
                                {"AMOUNT", 10.55},
                                {"CREATE_DATE", "=", "TIMESTAMP", "2020-02-02 12:00:00"},
                        });

        //for second call
        OracleDb dbConf2 = OracleDb.getInstance();

        assertEquals(String.format("""
                        OracleDb:
                            dbType=oracle
                            url=jdbc:oracle:thin:@%s:1521/xepdb1
                            username=ADMIN
                            password=ADMIN
                            awaitMs=300
                            awaitPollInterval=100
                            templatesPath=templates/oracle/
                            enableEnchantedReport=true
                        """, CI_HOST),
                dbConf2.getConfigSummary());
    }

    @Test
    void getDataFromEmptyTable() {
        db.showDataFromTable(TEST_TABLE);
    }


    @Test
    void renameAndRollbackTable() {
        db.truncateTable(TEST_FOR_RENAME);
        db.renameTable(TEST_FOR_RENAME);
        db.showDataFromTable(TEST_FOR_RENAME + "_X_");

        db.rollbackTable(TEST_FOR_RENAME);
        db.showDataFromTable(TEST_FOR_RENAME);
    }

    @Test
    void insertIntoGoods() {

        db.truncateTables(TEST_TABLE, TEST_TABLE2);

        insertIntoGoods(new Object[][]{
                {"ID", "1"},
                {"NAME", "good1"},
        });

        insertIntoCountries(new Object[][]{
                {"ID", "2"},
                {"NAME", "country"},
        });

        db.seeTableIsNotEmpty(TEST_TABLE);
        db.seeTableIsNotEmpty(TEST_TABLE2);

        db.truncateTables(TEST_TABLE, TEST_TABLE2);

        db.seeRowsCountInTableExactly(TEST_TABLE, 0);
        db.seeRowsCountInTableExactly(TEST_TABLE2, 0);
    }

    @Test
    void seeRecordExistsInTableAllTypesCatchTest() {
        insertIntoGoods(new Object[][]{
                {"ID", 1},
                {"NAME", "good1"},
                {"PRICE", 175.99},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRecordExistsInTable(TEST_TABLE,
                        new Object[][]{
                                {"ID", 2},
                                {"NAME", "good2"},
                                {"PRICE", "175"},
                        }));

        assertEquals("""
                        Failed: no records found in table 'ORACLE_SCHEMA.GOODS' within 500 milliseconds
                        Expected for:
                        [ID, NAME, PRICE]
                        [2, good2, 175]
                        Actual result(s):
                        [1, good1, 175.99]""",
                exception.getMessage(),
                "Exception on seeRecordExistsInTable");
    }

    @Test
    void seeRecordExistsInTableDateCatchTest() {
        insertIntoGoods(new Object[][]{
                {"ID", 2},
                {"CREATE_DATE", "2020-01-01 10:00:00", "TIMESTAMP"},
        });

        Throwable exception = assertThrows(AssertionError.class, () ->
                db.seeRecordExistsInTable(TEST_TABLE,
                        new Object[][]{
                                {"ID", 2},
                                {"CREATE_DATE", "=", "TIMESTAMP", "2020-02-02 15:15:15"},
                        }));

        assertEquals("""
                        Failed: no records found in table 'ORACLE_SCHEMA.GOODS' within 500 milliseconds
                        Expected for:
                        [ID, CREATE_DATE]
                        [2, (=)2020-02-02 15:15:15]
                        Actual result(s):
                        [2, 2020-01-01 10:00:00.0]""",
                exception.getMessage(),
                "Exception on seeRecordExistsInTable");
    }

    @Test
    void configDataSourcePropertiesTest() {
        String dataSourceConfig = dbConf.getDataSourceProperties();

        MatcherAssert.assertThat(
                "provided properties",
                dataSourceConfig,
                StringContains.containsString("connectTimeout=3002"));

        MatcherAssert.assertThat(
                "default properties",
                dataSourceConfig,
                StringContains.containsString("oracle.jdbc.jsonDefaultGetObjectType=java.lang.String"));

        MatcherAssert.assertThat(
                "default properties",
                dataSourceConfig,
                StringContains.containsString("oracle.jdbc.timezoneAsRegion=false"));
    }

}
