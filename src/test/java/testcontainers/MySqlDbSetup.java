package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import net.bugreaper.modules.db.MySQL;
import org.testcontainers.mysql.MySQLContainer;

import java.util.Objects;


public class MySqlDbSetup {

    private static final MySQLContainer DB_CONTAINER = new MySQLContainer("mysql:8.0.21")
            .withUsername("mysql_user")
            .withPassword("mysql_pass")
            .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                    new PortBinding(Ports.Binding.bindPort(3308),
                            new ExposedPort(3306))))
            .withInitScript("dump/mysql_setup.sql")
            .withDatabaseName("MYSQL_SCHEMA");


    static {
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
