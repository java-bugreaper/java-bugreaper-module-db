package net.bugreaper.modules.db.jdbc.builders;

import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.exceptions.QueryBuilderException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryBuilderNegativeTest {

    private final DbTypes oracle =DbTypes.ORACLE_DB;
    private final DbTypes maria = DbTypes.MARIA_DB;
    private final DbTypes mssql = DbTypes.MS_SQL;
    private final DbTypes mysql = DbTypes.MY_SQL;

    private final QueryBuilder qbm = new QueryBuilder(maria);



    @Test
    void testBuildConditionWithoutArray() {
        QueryBuilder qb = new QueryBuilder(maria);

        String errorMessage = null;

        try {
            qb.sqlConditionBuilder(null);
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }

        assertEquals(
                "Cannot read the array length because \"conditionsArray\" is null",
                errorMessage,
                "Array not provided will call exception");
    }

    @Test
    void testBuildConditionWithOneElement() {
        QueryBuilder qb = new QueryBuilder(mssql);

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qb.sqlConditionBuilder(new Object[][]{
                        {"COLUMN1", null},
                        {"COLUMN2"},
                        {"COLUMN3", "3"}
                }));

        assertEquals("ERROR, array length not valid: [COLUMN2]",
                exception.getMessage(),
                "Exception with length not valid column");
    }

    @Test
    void testBuildConditionWithFiveElement() {
        QueryBuilder qb = new QueryBuilder(maria);

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qb.sqlConditionBuilder(new Object[][]{
                        {"COLUMN2", "1", "2", "3", "4"},
                }));

        assertEquals("ERROR, array length not valid: [COLUMN2, 1, 2, 3, 4]",
                exception.getMessage(),
                "Exception with wrong array length");
    }

    @Test
    void testBuildConditionMariaFourElements() {
        QueryBuilder qb = new QueryBuilder(maria);

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qb.sqlConditionBuilder(new Object[][]{
                        {"DATA", "JSON_CONTAINS", "1", "[1]"},
                }));

        assertEquals("4 elements not supported for database: mariadb",
                exception.getMessage(),
                "Exception with wrong four elements mariaDB");
    }

    @Test
    void testBuildConditionOracleInvalidEnum() {
        QueryBuilder qb = new QueryBuilder(oracle);

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qb.sqlConditionBuilder(new Object[][]{
                        {"COLUMN2", "=", "DUMMY", "2020-05-05"},
                }));

        assertEquals("ERROR, wrong 4 elements option for Oracle mapper: <DUMMY> supported: DATE or TIMESTAMP",
                exception.getMessage(),
                "Exception with invalid Oracle four elements enum");
    }

    @Test
    void testBuildConditionMssqlNo3SpecificConditionsSupport() {
        QueryBuilder qb = new QueryBuilder(mssql);

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qb.sqlConditionBuilder(new Object[][]{
                        {"COLUMN2", "SOME", "2020-05-05"},
                }));

        assertEquals("ERROR, wrong 3 elements operator: <SOME> database <sqlserver> supported: [=, !=, <>, >, >=, <, <=, IS, IS NOT, LIKE]",
                exception.getMessage(),
                "Exception with 3 elements not basic conditions for not supported DB");
    }

    @Test
    void testBuildConditionMariaWrong3SpecificConditionsSupport() {
        QueryBuilder qb = new QueryBuilder(maria);

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qb.sqlConditionBuilder(new Object[][]{
                        {"COLUMN2", "SOME", "2020-05-05"},
                }));

        assertEquals("ERROR, wrong 3 elements operator for mariadb mapper: <SOME> supported: [=, !=, <>, >, >=, <, <=, IS, IS NOT, LIKE] or [JSON_CONTAINS, JSON_EQUALS], REGEX_MATCH",
                exception.getMessage(),
                "Exception with 3 elements, wrong condition for mariadb");
    }

    @Test
    void testBuildConditionMysqlWrong3SpecificConditionsSupport() {
        QueryBuilder qb = new QueryBuilder(mysql);

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qb.sqlConditionBuilder(new Object[][]{
                        {"COLUMN2", "JSON_EQUALS", "{}"},
                }));

        assertEquals("ERROR, wrong 3 elements operator for mysql mapper: <JSON_EQUALS> supported: [=, !=, <>, >, >=, <, <=, IS, IS NOT, LIKE] or JSON_CONTAINS, REGEX_MATCH",
                exception.getMessage());
    }

    @Test
    void testBuildSqlInsertNonExistentColumn() {
        QueryBuilder qb = new QueryBuilder(oracle);

        Object[][] template = new Object[][]{
                {"COLUMN1", null},
                {"COLUMN2", "test"}};

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qb.sqlInsertBuilder(
                        "TEST_SCHEMA.TEST_TABLE",
                        template,
                        new Object[][]{
                                {"NONEXISTENT", "SOME TEXT"}}
                ));

        assertEquals("ERROR, column not exist in template: NONEXISTENT",
                exception.getMessage(),
                "Exception with non exist column");
    }

    @Test
    void testBuildSqlInsertSameColumnTwice() {
        QueryBuilder qb = new QueryBuilder(mssql);

        Object[][] template = new Object[][]{
                {"COLUMN1", null},
                {"COLUMN2", "test"}};

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qb.sqlInsertBuilder(
                        "TEST_SCHEMA.TEST_TABLE",
                        template,
                        new Object[][]{
                                {"COLUMN2", "some text1"},
                                {"COLUMN2", "some text2"}}
                ));

        assertEquals("ERROR, duplicate column found: COLUMN2",
                exception.getMessage(),
                "Exception with duplicate column");
    }

    @Test
    void testBuildSetWithoutArray() {

        String errorMessage = null;

        try {
            qbm.sqlSetBuilder(null);
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }

        assertEquals(
                "Cannot read the array length because \"setArray\" is null",
                errorMessage,
                "Array not provided will call exception");
    }

    @Test
    void testBuildSetWithOneElement() {

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qbm.sqlSetBuilder(new Object[][]{
                        {"COLUMN1", null},
                        {"COLUMN2"},
                        {"COLUMN3", "3"}
                }));

        assertEquals("ERROR, array length not valid: [COLUMN2]",
                exception.getMessage(),
                "Exception with length not valid column");
    }

    @Test
    void testBuildSetWithThreeElement() {

        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qbm.sqlSetBuilder(new Object[][]{
                        {"COLUMN2", "1", "2"},
                }));

        assertEquals("ERROR, array length not valid: [COLUMN2, 1, 2]",
                exception.getMessage(),
                "Exception with wrong array length");
    }

    @Test
    void testBuildConditionsMariaJsonWrong() {
        QueryBuilder qb = new QueryBuilder(maria);

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                qb.sqlConditionBuilder(new Object[][]{
                        {"DATA", "JSON_CONTAINS", """
                                                "id": 2, "text": "test"}
                                                """},
                }));

        assertEquals("""
                        JSON must be an object or array at the root""",
                exception.getMessage(),
                "Exception with wrong JSON in db assert");

    }

    @Test
    void testBuildConditionsMariaJsonWrongComa() {
        QueryBuilder qb = new QueryBuilder(maria);

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                qb.sqlConditionBuilder(new Object[][]{
                        {"DATA", "JSON_CONTAINS", """
                                                {"id": 2, "text": "test",}"""},
                }));

        assertEquals("""
                        Invalid JSON or JSON array format (strict):
                        {"id": 2, "text": "test",}""",
                exception.getMessage());
    }

    @Test
    void testBuildConditionsMariaJsonEquals() {
        QueryBuilder qb = new QueryBuilder(mysql);


        Throwable exception = assertThrows(QueryBuilderException.class, () ->
                qb.sqlConditionBuilder(new Object[][]{
                        {"DATA", "JSON_EQUALS", """
                                                {"id": 2}"""},
                }));


        MatcherAssert.assertThat(
                "mysql not support JSON_EQUALS",
                exception.getMessage(),
                StringContains.containsString("ERROR, wrong 3 elements operator for mysql mapper: <JSON_EQUALS> supported: [=, !=, <>, >, >=, <, <=, IS, IS NOT, LIKE] or JSON_CONTAINS, REGEX_MATCH"));

    }

}
