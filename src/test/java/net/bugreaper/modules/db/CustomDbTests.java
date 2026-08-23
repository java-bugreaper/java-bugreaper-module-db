package net.bugreaper.modules.db;


import net.bugreaper.modules.db.exceptions.JdbcExecutorException;
import net.bugreaper.modules.db.exceptions.QueryBuilderException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import testcontainers.ClickhouseDb;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static testcontainers.DockerSetup.CI_HOST;

@SuppressWarnings({"squid:S2699"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //To call connection once!
class CustomDbTests extends ClickhouseDb {

    private static final CustomDb db = getCustomDb();
    private static final CustomDb customDb = CustomDb.getInstance();

    private static final String TEST_TABLE_FOR_TEMPLATE = "MYSCHEMA.TEMP";

    @AfterAll
    static void shutDown() {
        DB_CONTAINER.stop();
    }


    @Test
    void fullJsonFeatureTest() {
        db.truncateTable(TEST_TABLE_FOR_TEMPLATE);

        db.insertIntoTable(TEST_TABLE_FOR_TEMPLATE,
                """
                        {
                            "ID": 1,
                            "AMOUNT": 10.55
                        }""");

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
    void configCustomDbBasicMethodsTest() {

        customDb.truncateTable(TEST_TABLE_FOR_TEMPLATE);

        customDb.insertIntoTable(TEST_TABLE_FOR_TEMPLATE,
                new Object[][]{
                        {"ID", 2},
                });

        customDb.seeRecordExistsInTable
                (TEST_TABLE_FOR_TEMPLATE,
                        new Object[][]{
                                {"ID", 2},
                                {"NAME", "Maria"},
                                {"AMOUNT", 10.55},
                                {"IS_ACTIVE", false},
                        });


        Assertions.assertEquals(String.format("""
                        CustomDb:
                            dbType=custom-db
                            url=jdbc:clickhouse://%s:8123/MYSCHEMA
                            username=default
                            password=password
                            awaitMs=420
                            awaitPollInterval=100
                            templatesPath=templates/custom-db/
                            enableEnchantedReport=true
                        """, CI_HOST),
                customDb.getConfigSummary());
    }

    @Test
    void getDataPassCustomDbTests() {

        customDb.truncateTable(TEST_TABLE_FOR_TEMPLATE);

        //for second call
        CustomDb customDb2 = CustomDb.getInstance();

        customDb2.insertIntoTable(TEST_TABLE_FOR_TEMPLATE,
                new Object[][]{
                        {"ID", 2},
                });

        Throwable exception = assertThrows(AssertionError.class, () ->
                customDb.seeRecordExistsInTable
                        (TEST_TABLE_FOR_TEMPLATE,
                                new Object[][]{
                                        {"ID", 1},
                                }));

        MatcherAssert.assertThat(
                exception.getMessage(),
                is("""
                        Failed: no records found in table 'MYSCHEMA.TEMP' within 420 milliseconds
                        Expected for:
                        [ID]
                        [1]
                        Actual result(s):
                        [2]"""));

        Throwable exception2 = assertThrows(JdbcExecutorException.class, () ->
                customDb.showDataFromTable(TEST_TABLE_FOR_TEMPLATE));

        MatcherAssert.assertThat(
                exception2.getMessage(),
                containsString("Get all data for allure not supported for CustomDb"));
    }

    @Test
    void onlyBasicConditionsCustomDbTests() {

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                customDb.seeRecordExistsInTable
                        (TEST_TABLE_FOR_TEMPLATE,
                                new Object[][]{
                                        {"ID", "JSON_CONTAINS", 1},
                                }));

        MatcherAssert.assertThat(
                exception.getMessage(),
                is("ERROR: wrong 3 elements operator 'JSON_CONTAINS' for database 'custom-db' supported basic: [=, !=, <>, >, >=, <, <=, IS, IS NOT, LIKE]"));
    }

    //  Mutliton

    @Test
    void customDbMultiton2Test() {

        CustomDb customDb2 = CustomDb.getInstance("-2");
        CustomDb customDb2Same = CustomDb.getInstance("-2");

        customDb2.truncateTable(TEST_TABLE_FOR_TEMPLATE);

        customDb2Same.insertIntoTable(TEST_TABLE_FOR_TEMPLATE,
                new Object[][]{
                        {"ID", 2},
                });

        customDb2Same.seeRecordExistsInTable
                (TEST_TABLE_FOR_TEMPLATE,
                        new Object[][]{
                                {"ID", 2},
                                {"NAME", "Maria"},
                                {"AMOUNT", 999.99},
                                {"IS_ACTIVE", false},
                        });


        Assertions.assertEquals(String.format("""
                        CustomDb:
                            dbType=custom-db-2
                            url=jdbc:clickhouse://%s:8123/MYSCHEMA
                            username=default
                            password=password
                            awaitMs=200
                            awaitPollInterval=100
                            templatesPath=templates/custom-db-2/
                            enableEnchantedReport=false
                        """, CI_HOST),
                customDb2Same.getConfigSummary());
    }

    @Test
    void customDbMultiton3Test() {

        CustomDb customDb3 = CustomDb.getInstance("-3");

        Assertions.assertEquals(String.format("""
                        CustomDb:
                            dbType=custom-db-3
                            url=jdbc:clickhouse://%s:8123/MYSCHEMA
                            username=default
                            password=password
                            awaitMs=400
                            awaitPollInterval=100
                            templatesPath=templates/custom-db-3/
                            enableEnchantedReport=true
                        """, CI_HOST),
                customDb3.getConfigSummary());
    }

}
