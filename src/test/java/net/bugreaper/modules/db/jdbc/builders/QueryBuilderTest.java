package net.bugreaper.modules.db.jdbc.builders;

import net.bugreaper.modules.db.enums.DbTypes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryBuilderTest {

    private final DbTypes oracle =DbTypes.ORACLE_DB;
    private final DbTypes maria = DbTypes.MARIA_DB;
    private final DbTypes mssql = DbTypes.MS_SQL;
    private final DbTypes mysql = DbTypes.MY_SQL;

    private final QueryBuilder qbm = new QueryBuilder(maria);


    @Test
    void testBuildConditionString() {
        QueryBuilder qb = new QueryBuilder(mssql);

        assertEquals(
                "where COLUMN='test'",
                qb.sqlConditionBuilder(new Object[][]{{"COLUMN", "test"}}),
                "Create condition with text value"
        );
    }

    @Test
    void testBuildConditionNullable() {
        assertEquals(
                "where COLUMN IS null",
                qbm.sqlConditionBuilder(new Object[][]{{"COLUMN", null}}),
                "Create condition with null value"
        );
    }


    @Test
    void testBuildConditionNoCondition() {
        assertEquals(
                "",
                qbm.sqlConditionBuilder(new Object[][]{}),
                "Create condition with empty array"
        );
    }

    @Test
    void testBuildConditionAll() {
        assertEquals(
                "where COLUMN1 IS null AND COLUMN2='string' AND COLUMN3=3",
                qbm.sqlConditionBuilder(new Object[][]{
                        {"COLUMN1", null},
                        {"COLUMN2", "string"},
                        {"COLUMN3", 3}
                }),
                "Create condition with all types of values"
        );
    }

    @Test
    void testBuildConditionsWithOperators() {
        QueryBuilder qb = new QueryBuilder(mssql);

        assertEquals(
                "where COLUMN1 IS null AND COLUMN2='string' AND COLUMN3 >= '3'",
                qb.sqlConditionBuilder(new Object[][]{
                        {"COLUMN1", null},
                        {"COLUMN2", "string"},
                        {"COLUMN3", ">=", "3"}
                }),
                "Create condition with all types of values and operators"
        );
    }

    @Test
    void testBuildConditionsWithOperatorsAll() {
        assertEquals(
                "where COLUMN1 IS NOT null AND COLUMN2 LIKE 'string' AND COLUMN3 >= '3'",
                qbm.sqlConditionBuilder(new Object[][]{
                        {"COLUMN1", "IS NOT", null},
                        {"COLUMN2", "LIKE", "string"},
                        {"COLUMN3", ">=", "3"}
                }),
                "Create condition with all types of values and operators"
        );
    }

    @Test
    void testBuildConditionsWithObjects() {
        assertEquals(
                "where COLUMN1 IS NOT null AND COLUMN2 LIKE 'string' AND COLUMN3=3 AND COLUMN4 >= 3 AND BOOL=false",
                qbm.sqlConditionBuilder(new Object[][]{
                        {"COLUMN1", "IS NOT", null},
                        {"COLUMN2", "LIKE", "string"},
                        {"COLUMN3", 3},
                        {"COLUMN4", ">=", 3},
                        {"BOOL", false}
                }),
                "Create condition with Objects(int, boolean)"
        );
    }

    @Test
    void testBuildConditionsWithObjectsDecimal() {
        assertEquals(
                "where COLUMN1=3.14159 AND COLUMN2=13.0 AND COLUMN3=4.0 AND COLUMN4=14.14159 AND COLUMN5=5.00 AND COLUMN6=5.14159",
                qbm.sqlConditionBuilder(new Object[][]{
                        {"COLUMN1", 3.14159d},
                        {"COLUMN2", 13.00d},
                        {"COLUMN3", 4.00f},
                        {"COLUMN4", 14.14159},
                        {"COLUMN5",  new BigDecimal("5.00")},
                        {"COLUMN6", new BigDecimal("5.14159")},
                }),
                "Create condition with Objects(double, float, bigDecimal)"
        );
    }

    @Test
    void testBuildConditionsOracleDate() {
        QueryBuilder qb = new QueryBuilder(oracle);

        assertEquals(
                "where COLUMN1 = TO_DATE('2020-01-01', 'YYYY-MM-DD')",
                qb.sqlConditionBuilder(new Object[][]{
                        {"COLUMN1", "=", "DATE", "2020-01-01"},
                }),
                "Create condition with Oracle type DATE"
        );
    }

    @Test
    void testBuildConditionsOracleDateTime() {
        QueryBuilder qb = new QueryBuilder(oracle);

        assertEquals(
                "where COLUMN1 >= TO_DATE('2020-01-01 11-05-51', 'YYYY-MM-DD HH24:MI:SS')",
                qb.sqlConditionBuilder(new Object[][]{
                        {"COLUMN1", ">=", "TIMESTAMP", "2020-01-01 11-05-51"},
                }),
                "Create condition with Oracle type TIMESTAMP"
        );
    }

    @Test
    void testBuildConditionsMariaJsonContains() {
        QueryBuilder qb = new QueryBuilder(maria);

        assertEquals(
                "where JSON_CONTAINS(DATA, '{\"id\": 15}')",
                qb.sqlConditionBuilder(new Object[][]{
                        {"DATA", "JSON_CONTAINS", """
                                                  {"id": 15}"""},
                }),
                "Create condition with mariaDb type JSON_CONTAINS"
        );
    }

    @Test
    void testBuildConditionsMariaRegexContains() {
        QueryBuilder qb = new QueryBuilder(maria);

        assertEquals(
                "where DATA RLIKE '[0-9]{4}'",
                qb.sqlConditionBuilder(new Object[][]{
                        {"DATA", "REGEX_MATCH", "[0-9]{4}"},
                }),
                "Create condition with mariaDb type REGEX_MATCH"
        );
    }


    @Test
    void testBuildConditionsMariaJsonContainsArray() {
        QueryBuilder qb = new QueryBuilder(mysql);

        assertEquals(
                "where JSON_CONTAINS(DATA, '[{\"id\": 1}]')",
                qb.sqlConditionBuilder(new Object[][]{
                        {"DATA", "JSON_CONTAINS", """
                                                [{"id": 1}]"""},
                }),
                "Create condition with mariaDb type JSON_CONTAINS"
        );
    }

    @Test
    void testBuildSqlInsert() {
        assertEquals(
                """
                        INSERT INTO TEST_SCHEMA.TEST_TABLE
                        (COLUMN1, COLUMN2, COLUMN3)
                        VALUES (null, 'data2', null)""",
                qbm.sqlInsertBuilder(
                        "TEST_SCHEMA.TEST_TABLE",
                        new Object[][]{
                                {"COLUMN1", null},
                                {"COLUMN2", "test"},
                                {"COLUMN3", "test"}},
                        new Object[][]{
                                {"COLUMN2", "data2"},
                                {"COLUMN3", null}}),
                "Insert create currently, with null and string test data");
    }

    @Test
    void testBuildSqlInsertDifferentTypes() {
        assertEquals(
                """
                        INSERT INTO TEST_SCHEMA.TEST_TABLE
                        (COLUMN1, COLUMN2, COLUMN3, COLUMN4)
                        VALUES (2, true, false, 'test')""",
                qbm.sqlInsertBuilder(
                        "TEST_SCHEMA.TEST_TABLE",
                        new Object[][]{
                                {"COLUMN1", 1},
                                {"COLUMN2", true},
                                {"COLUMN3", false},
                                {"COLUMN4", "test"}},
                        new Object[][]{
                                {"COLUMN1", 2}}),
                "Insert create currently, with object types");
    }

    @Test
    void testBuildOracleParamSqlInsert() {
        QueryBuilder qb = new QueryBuilder(oracle);

        assertEquals(
                """
                        INSERT INTO TEST_SCHEMA.TEST_TABLE
                        (COLUMN1, COLUMN3, DATE_COLUMN)
                        VALUES (null, null, TIMESTAMP '3033-03-33')""",
                qb.sqlInsertBuilder(
                        "TEST_SCHEMA.TEST_TABLE",
                        new Object[][]{
                                {"COLUMN1", null},
                                {"COLUMN3", "test"},
                                {"DATE_COLUMN", "2022-01-01", "TIMESTAMP"}},
                        new Object[][]{
                                {"DATE_COLUMN", "3033-03-33"},
                                {"COLUMN3", null}}),
                "Insert create currently, with null and DATE with parameter");
    }

    @Test
    void testBuildOracleParamMergeSqlInsert() {
        QueryBuilder qb = new QueryBuilder(oracle);

        assertEquals(
                """
                        INSERT INTO TEST_SCHEMA.TEST_TABLE
                        (COLUMN1, COLUMN3, DATE_COLUMN)
                        VALUES (null, null, DATE '3033-03-33')""",
                qb.sqlInsertBuilder(
                        "TEST_SCHEMA.TEST_TABLE",
                        new Object[][]{
                                {"COLUMN1", null},
                                {"COLUMN3", "test"},
                                {"DATE_COLUMN", "2022-01-01", "TIMESTAMP"}},
                        new Object[][]{
                                {"DATE_COLUMN", "3033-03-33", "DATE"},
                                {"COLUMN3", null}}),
                "Insert create currently, with null and DATE with parameter");
    }

    @Test
    void testBuildOracleNullableParamSqlInsert() {
        assertEquals(
                """
                        INSERT INTO TEST_SCHEMA.TEST_TABLE
                        (COLUMN1, COLUMN3, DATE_COLUMN)
                        VALUES (null, null, null)""",
                qbm.sqlInsertBuilder(
                        "TEST_SCHEMA.TEST_TABLE",
                        new Object[][]{
                                {"COLUMN1", null},
                                {"COLUMN3", "test"},
                                {"DATE_COLUMN", "2022-01-01", "TIMESTAMP"}},
                        new Object[][]{
                                {"DATE_COLUMN", null},
                                {"COLUMN3", null}}),
                "Insert create currently, with nullable DATE, parameter will removed");
    }

    @Test
    void testBuildSetString() {
        assertEquals(
                "SET COLUMN='test'",
                qbm.sqlSetBuilder(new Object[][]{{"COLUMN", "test"}}),
                "Create SET with text value"
        );
    }

    @Test
    void testBuildSetNullable() {
        assertEquals(
                "SET COLUMN IS null",
                qbm.sqlSetBuilder(new Object[][]{{"COLUMN", null}}),
                "Create SET with null value"
        );
    }

    @Test
    void testBuildSetMultipleValues() {
        assertEquals(
                "SET COLUMN1 IS null, COLUMN2='string', COLUMN3='3'",
                qbm.sqlSetBuilder(new Object[][]{
                        {"COLUMN1", null},
                        {"COLUMN2", "string"},
                        {"COLUMN3", "3"}
                }),
                "Create SET with all types of values"
        );
    }

}
