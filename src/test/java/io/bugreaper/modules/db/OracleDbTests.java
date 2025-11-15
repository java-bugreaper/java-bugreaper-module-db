package io.bugreaper.modules.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import testcontainers.OracleDbSetup;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("squid:S2699")
class OracleDbTests {

    private static final OracleDbSetup setup = OracleDbSetup.getInstance();
    private static final OracleDb db = setup.getOracleDb().withAwaitMs(500);

    private static final String TEST_TABLE = "ORACLE_SCHEMA.GOODS";
    private static final String TEST_TABLE2 = "ORACLE_SCHEMA_2.COUNTRIES";
    private static final String TEST_FOR_RENAME = "ORACLE_SCHEMA.USERS";
    private static final String TEST_FOR_TEMPLATE = "ORACLE_SCHEMA.TEMP";




    public void insertIntoGoods(Object[][] testArray) {

        final Object[][] template = {
                {"ID", "200"},
                {"CATEGORY_ID", "1"},
                {"NAME", "dummy_category"},
                {"PRICE", "175"},
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
        db.truncateTable(TEST_FOR_RENAME);
        db.renameTable(TEST_FOR_RENAME);
        db.showDataFromTableMethod(TEST_FOR_RENAME + "_X_");

        db.rollbackTable(TEST_FOR_RENAME);
        db.showDataFromTableMethod(TEST_FOR_RENAME);
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

        db.seeTableDoesNotEmpty(TEST_TABLE);
        db.seeTableDoesNotEmpty(TEST_TABLE2);

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

        Throwable exception = assertThrows(AssertionFailedError.class, () ->
                db.seeRecordExistsInTable(TEST_TABLE,
                        new Object[][]{
                                {"ID", 2},
                                {"NAME", "good2"},
                                {"PRICE", "175"},
                        }));

        assertEquals("""
                        Failed: record/s not found in <ORACLE_SCHEMA.GOODS> within 500 milliseconds
                        Expected for:
                        [ID, NAME, PRICE]
                        [2, good2, 175]
                        Actual result(s)
                        [1, good1, 175.99]""",
                exception.getMessage(),
                "Exception on seeRecordExistsInTable");
    }

}



