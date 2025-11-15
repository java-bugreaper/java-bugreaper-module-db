package testcontainers;

import io.bugreaper.modules.db.MariaDb;

import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;


public class MariaDbSetup {

    static final DockerImageName MARIA_DB = DockerImageName.parse("mariadb:10.10");
    MariaDBContainer container = new MariaDBContainer(MARIA_DB);
    private static MariaDbSetup instance;


    public MariaDbSetup() {
        container
                .withInitScript("dump/maria_setup.sql")
                .withDatabaseName("MARIA_SCHEMA")
                .start();
    }

    public static MariaDbSetup getInstance() {
        if (instance == null) {
            instance = new MariaDbSetup();
        }

        return instance;
    }

    public MariaDb getMariDb() {
        return new MariaDb(

                "jdbc:mariadb://"
                        + container.getHost() + ":"
                        + container.getMappedPort(3306),
                "test",
                "test");
    }

}
