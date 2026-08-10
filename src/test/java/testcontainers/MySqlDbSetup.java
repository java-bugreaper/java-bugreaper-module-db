package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import net.bugreaper.modules.db.MySQL;
import org.testcontainers.mysql.MySQLContainer;

import java.util.Objects;

import static testcontainers.DockerSetup.printVersion;
import static testcontainers.DockerSetup.resolveDockerImage;


public class MySqlDbSetup {

    private static final String STABLE_VERSION = "mysql:8.4.11";
    private static final String LATEST_VERSION = "mysql:26.7.0";

    private static final String DOCKER_IMAGE = resolveDockerImage(STABLE_VERSION, LATEST_VERSION);

    public static final MySQLContainer DB_CONTAINER = new MySQLContainer(DOCKER_IMAGE)
            .withUsername("mysql_user")
            .withPassword("mysql_pass")
            .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                    new PortBinding(Ports.Binding.bindPort(3308),
                            new ExposedPort(3306))))
            .withInitScript("dump/mysql_setup.sql")
            .withDatabaseName("MYSQL_SCHEMA");


    static {
        printVersion(DOCKER_IMAGE);

        DB_CONTAINER.start();
    }

    public static MySQL getMysqlDb() {
        return new MySQL(

                "jdbc:mysql://"
                        + DB_CONTAINER.getHost() + ":"
                        + DB_CONTAINER.getMappedPort(3306),
                "mysql_user",
                "mysql_pass");
    }

}
