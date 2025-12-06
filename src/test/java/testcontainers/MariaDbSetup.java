package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.bugreaper.modules.db.MariaDb;

import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;


public class MariaDbSetup {

    static final DockerImageName MARIA_DB = DockerImageName.parse("mariadb:10.10");
    MariaDBContainer container = new MariaDBContainer(MARIA_DB);
    private static MariaDbSetup instance;


    public MariaDbSetup() {
        container
                .withUsername("test_user")
                .withPassword("secret_pass")
                .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(3307),
                                new ExposedPort(3306))))
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
                "test_user",
                "secret_pass");
    }

}
