package io.bugreaper.modules.db.jdbc;

import org.junit.jupiter.api.Test;

import static io.bugreaper.core.utils.AllureStepsValidator.validateAllSteps;

class AllureStepsValidationTest {

    @Test
    void testStepsJdbcExecutor() {
        validateAllSteps("io.bugreaper.modules.db.jdbc.JdbcExecutor");
    }

    @Test
    void testStepsAssertLogic() {
        validateAllSteps("io.bugreaper.modules.db.jdbc.AbstractExecutor");
    }

    @Test
    void testStepsMariaDb() {
        validateAllSteps("io.bugreaper.modules.db.MariaDb");
    }

    @Test
    void testStepsMsSqlDb() {
        validateAllSteps("io.bugreaper.modules.db.MsSQL");
    }

    @Test
    void testStepsMySqlDb() {
        validateAllSteps("io.bugreaper.modules.db.MySQL");
    }

    @Test
    void testStepsOracleDb() {
        validateAllSteps("io.bugreaper.modules.db.OracleDb");
    }

    @Test
    void testStepsPostgreSQL() {
        validateAllSteps("io.bugreaper.modules.db.PostgreSQL");
    }

}
