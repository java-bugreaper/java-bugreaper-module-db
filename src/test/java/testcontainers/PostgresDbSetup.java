package testcontainers;

import io.bugreaper.modules.db.PostgreSQL;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.TimeZone;


public class PostgresDbSetup {

    static final DockerImageName POSTGRES_DB = DockerImageName.parse("postgres:14.11");
    PostgreSQLContainer container = new PostgreSQLContainer(POSTGRES_DB);
    private static PostgresDbSetup instance;

    public PostgresDbSetup() {
        //for problem if local zone Europe/Kyiv
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kiev"));
        container
                .withInitScript("dump/postgres_setup.sql")
                .withDatabaseName("public")
                .start();
    }

    public static PostgresDbSetup getInstance() {
        if (instance == null) {
            instance = new PostgresDbSetup();
        }

        return instance;
    }

    public PostgreSQL getPostgresDb() {
        return new PostgreSQL(

                "jdbc:postgresql://"
                        + container.getHost() + ":"
                        + container.getMappedPort(5432)
                        + "/public",
                "test",
                "test");
    }

    protected static final PostgresDbSetup setup = PostgresDbSetup.getInstance();
    protected static final PostgreSQL db = setup.getPostgresDb();
    protected static final PostgreSQL dbAwait = setup.getPostgresDb().withAwaitMs(1000);

    protected static final String TEST_TABLE = "public.users";
    protected static final String TEST_TABLE2 = "public.users2";
    protected static final String TEST_TABLE_FOR_RENAME = "public.users3";
    protected static final String TEST_TABLE_FOR_TEMPLATE = "public.temp";


    protected void insertIntoUsers(Object[][] testArray) {
        final Object[][] template = {
                {"ID", 100},
                {"USERNAME", "Pupkin"},
                {"PASSWORD", "pupKin1992"},
                {"EMAIL", "pupkin@com.com"},
                {"STATE", 0},
                {"DESCRIPTION", "dummy description"},
                {"ROLES", "manager"},
                {"DATA", "[]"},
                {"IS_ACTIVE", false},
                {"AMOUNT", null},
                {"PERCENTAGE", null},
                {"CREATE_DATE", "2021-02-02 10:00:00"},
                {"UPDATE_DATE", "2021-02-02 10:00:00"},
        };

        db.insertIntoTable(TEST_TABLE, template, testArray);
    }

}
