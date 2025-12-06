package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import io.bugreaper.modules.db.OracleDb;
import org.testcontainers.containers.OracleContainer;


import org.testcontainers.utility.MountableFile;

import java.util.Objects;


public class OracleDbSetup {

    OracleContainer container = new OracleContainer("gvenzl/oracle-xe:21-slim");

    private static OracleDbSetup instance;


    public OracleDbSetup() {
        container
                .withEnv("TZ", "Europe/Kiev")
                .withExposedPorts(1521)
                .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(1521),
                                new ExposedPort(1521))))
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource("dump/oracle_setup.sql"),
                        "/container-entrypoint-initdb.d/01-dump.sql")

                .start();
    }

    public static OracleDbSetup getInstance() {
        if (instance == null) {
            instance = new OracleDbSetup();
        }

        return instance;
    }

    public OracleDb getOracleDb() {
        return new OracleDb(

                "jdbc:oracle:thin:@//"
                        + container.getHost()
                        + "/XEPDB1",
                "ADMIN",
                "ADMIN");
    }

}
