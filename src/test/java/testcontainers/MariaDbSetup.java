package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import net.bugreaper.modules.db.MariaDb;

import org.testcontainers.mariadb.MariaDBContainer;

import java.util.Objects;

import static testcontainers.DockerSetup.printVersion;
import static testcontainers.DockerSetup.resolveDockerImage;


public class MariaDbSetup {

    private static final String STABLE_VERSION = "mariadb:10.11.18";
    private static final String LATEST_VERSION = "mariadb:12.3.2"; //13.01-rc

    private static final String DOCKER_IMAGE = resolveDockerImage(STABLE_VERSION, LATEST_VERSION);

    public static final MariaDBContainer DB_CONTAINER =
            new MariaDBContainer(DOCKER_IMAGE)
                    .withUsername("test_user")
                    .withPassword("secret_pass")
                    .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(3307),
                                    new ExposedPort(3306))))
                    .withInitScript("dump/maria_setup.sql")
                    .withDatabaseName("MARIA_SCHEMA");


    static {
        printVersion(DOCKER_IMAGE);

        DB_CONTAINER.start();
    }

    public static MariaDb getMariDb() {
        return new MariaDb(

                "jdbc:mariadb://"
                        + DB_CONTAINER.getHost() + ":"
                        + DB_CONTAINER.getMappedPort(3306),
                "test_user",
                "secret_pass");
    }



}
