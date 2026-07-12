package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import net.bugreaper.modules.db.OracleDb;
import org.testcontainers.containers.OracleContainer;

import org.testcontainers.utility.MountableFile;

import java.util.Objects;


public class OracleDbSetup {

    private static final OracleContainer DB_CONTAINER =  new OracleContainer("gvenzl/oracle-xe:21-slim")
            .withEnv("TZ", "Europe/Kiev")
            .withExposedPorts(1521)
            .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                    new PortBinding(Ports.Binding.bindPort(1521),
                            new ExposedPort(1521))))
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("dump/oracle_setup.sql"),
                    "/container-entrypoint-initdb.d/01-dump.sql");

    static {
        DB_CONTAINER.start();
    }

    public static  OracleDb getOracleDb() {
        DB_CONTAINER.isHostAccessible();
        return new OracleDb(
                DB_CONTAINER.getJdbcUrl(),
                "ADMIN",
                "ADMIN");
    }

}
