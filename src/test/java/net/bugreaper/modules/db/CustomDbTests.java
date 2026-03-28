package net.bugreaper.modules.db;


import net.bugreaper.modules.db.exceptions.JdbcExecutorException;
import net.bugreaper.modules.db.exceptions.QueryBuilderException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import testcontainers.MariaDbSetup;

import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"squid:S2699"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //To call connection once!
class CustomDbTests {

    private static final CustomDb db = new MariaDbSetup().getCustomDb();
    private static final CustomDb customDb = CustomDb.getInstance();

    private static final String CI = System.getenv("CI");
    private static final String PROPERTY = "bugreaperEnv";
    private static final String TEST_TABLE_FOR_TEMPLATE = "MARIA_SCHEMA.TEMP";

    String getCi() {
        if (Objects.equals(CI, "true")) {
            return "jdbc:mariadb://docker:3307";
        } else {
            return "jdbc:mariadb://localhost:3307";
        }
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
        if (Objects.equals(CI, "true")) {
            System.setProperty(PROPERTY, "docker");
        } else {
            System.clearProperty(PROPERTY);
        }

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
                            url=%s
                            username=test_user
                            password=secret_pass
                            awaitMs=420
                            templatesPath=templates/custom-db/
                        """, getCi()),
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
                        Failed: record/s not found in <MARIA_SCHEMA.TEMP> within 420 milliseconds
                        Expected for:
                        [ID]
                        [1]
                        Actual result(s)
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
                is("ERROR, wrong 3 elements operator: <JSON_CONTAINS> supported: [=, !=, <>, >, >=, <, <=, IS, IS NOT, LIKE]"));
    }
}
