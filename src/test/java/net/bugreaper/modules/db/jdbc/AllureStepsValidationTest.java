package net.bugreaper.modules.db.jdbc;

import org.junit.jupiter.api.Test;

import static net.bugreaper.core.utils.AllureStepsValidator.validateAllSteps;

class AllureStepsValidationTest {

    @Test
    void testStepsJdbcExecutor() {
        validateAllSteps("net.bugreaper.modules.db.jdbc.JdbcExecutor");
    }

    @Test
    void testStepsAssertLogic() {
        validateAllSteps("net.bugreaper.modules.db.jdbc.AbstractExecutor");
    }

    @Test
    void testStepsMariaDb() {
        validateAllSteps("net.bugreaper.modules.db.MariaDb");
    }

    @Test
    void testStepsMsSqlDb() {
        validateAllSteps("net.bugreaper.modules.db.MsSQL");
    }

    @Test
    void testStepsMySqlDb() {
        validateAllSteps("net.bugreaper.modules.db.MySQL");
    }

    @Test
    void testStepsOracleDb() {
        validateAllSteps("net.bugreaper.modules.db.OracleDb");
    }

    @Test
    void testStepsPostgreSQL() {
        validateAllSteps("net.bugreaper.modules.db.PostgreSQL");
    }

}
