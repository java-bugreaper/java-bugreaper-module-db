package io.bugreaper.modules.db.jdbc;

import io.bugreaper.modules.db.DbTypes;
import org.junit.jupiter.api.Test;

import static io.bugreaper.modules.db.helpers.Convert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumsValuesTest {


    @Test
    void testMariaDb() {
        assertEquals(convertToDbType(getMariaDbUrl()), DbTypes.MARIA_DB.value());
    }

    @Test
    void testMySQL() {
        assertEquals(convertToDbType(getMySqlUrl()), DbTypes.MY_SQL.value());
    }

    @Test
    void testMsSQL() {
        assertEquals(convertToDbType(getMsSqlUrl()), DbTypes.MS_SQL.value());
    }

    @Test
    void testOracleDb() {
        assertEquals(convertToDbType(getOracleDbUrl()), DbTypes.ORACLE_DB.value());
    }

    @Test
    void testPostgreSQL() {
        assertEquals(convertToDbType(getPostgreSqlUrl()), DbTypes.POSTGRE_SQL.value());
    }

}
