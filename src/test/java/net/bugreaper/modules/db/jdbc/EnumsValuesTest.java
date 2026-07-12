package net.bugreaper.modules.db.jdbc;

import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.helpers.Convert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class EnumsValuesTest {


    @Test
    void testMariaDb() {
        Assertions.assertEquals(Convert.convertToDbType(Convert.getMariaDbUrl()), DbTypes.MARIA_DB.value());
    }

    @Test
    void testMySQL() {
        Assertions.assertEquals(Convert.convertToDbType(Convert.getMySqlUrl()), DbTypes.MY_SQL.value());
    }

    @Test
    void testMsSQL() {
        Assertions.assertEquals(Convert.convertToDbType(Convert.getMsSqlUrl()), DbTypes.MS_SQL.value());
    }

    @Test
    void testOracleDb() {
        Assertions.assertEquals(Convert.convertToDbType(Convert.getOracleDbUrl()), DbTypes.ORACLE_DB.value());
    }

    @Test
    void testPostgreSQL() {
        Assertions.assertEquals(Convert.convertToDbType(Convert.getPostgreSqlUrl()), DbTypes.POSTGRE_SQL.value());
    }

}
