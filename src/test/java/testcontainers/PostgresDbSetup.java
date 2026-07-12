package testcontainers;

import net.bugreaper.modules.db.PostgreSQL;

import org.testcontainers.postgresql.PostgreSQLContainer;


public class PostgresDbSetup {

    private static final PostgreSQLContainer DB_CONTAINER = new PostgreSQLContainer("postgres:14.11")
            .withInitScript("dump/postgres_setup.sql")
            .withDatabaseName("public");

    static {
        DB_CONTAINER.start();
    }


    public static PostgreSQL getPostgresDb() {
        return new PostgreSQL(

                "jdbc:postgresql://"
                        + DB_CONTAINER.getHost() + ":"
                        + DB_CONTAINER.getMappedPort(5432)
                        + "/public",
                "test",
                "test");
    }

}
