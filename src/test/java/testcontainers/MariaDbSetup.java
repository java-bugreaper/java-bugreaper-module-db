package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import net.bugreaper.modules.db.CustomDb;
import net.bugreaper.modules.db.MariaDb;

import org.testcontainers.mariadb.MariaDBContainer;

import java.util.Objects;


public class MariaDbSetup {

    protected static final MariaDBContainer DB_CONTAINER =
            new MariaDBContainer("mariadb:11.2")
                    .withUsername("test_user")
                    .withPassword("secret_pass")
                    .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(3307),
                                    new ExposedPort(3306))))
                    .withInitScript("dump/maria_setup.sql")
                    .withDatabaseName("MARIA_SCHEMA");


    static {
        DB_CONTAINER.start();
    }

    public MariaDb getMariDb() {
        return new MariaDb(

                "jdbc:mariadb://"
                        + DB_CONTAINER.getHost() + ":"
                        + DB_CONTAINER.getMappedPort(3306),
                "test_user",
                "secret_pass");
    }

    public CustomDb getCustomDb() {
        return new CustomDb(

                "jdbc:mariadb://"
                        + DB_CONTAINER.getHost() + ":"
                        + DB_CONTAINER.getMappedPort(3306),
                "test_user",
                "secret_pass");
    }

}
