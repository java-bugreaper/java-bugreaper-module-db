package testcontainers;

import io.bugreaper.modules.db.MySQL;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;


public class MySqlDbSetup {

    static final DockerImageName MARIA_DB = DockerImageName.parse("mysql:8.0.21");
    MySQLContainer container = new MySQLContainer(MARIA_DB);
    private static MySqlDbSetup instance;


    public MySqlDbSetup() {
        container
                .withInitScript("dump/mysql_setup.sql")
                .withDatabaseName("MYSQL_SCHEMA")
                .start();
    }

    public static MySqlDbSetup getInstance() {
        if (instance == null) {
            instance = new MySqlDbSetup();
        }

        return instance;
    }

    public MySQL getMysqlDb() {
        return new MySQL(

                "jdbc:mysql://"
                        + container.getHost() + ":"
                        + container.getMappedPort(3306),
                "test",
                "test");
    }

}
