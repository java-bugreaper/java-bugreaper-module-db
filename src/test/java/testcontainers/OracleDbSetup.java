package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import net.bugreaper.modules.db.OracleDb;
import org.testcontainers.containers.OracleContainer;

import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.Objects;

import static testcontainers.DockerSetup.printVersion;
import static testcontainers.DockerSetup.resolveDockerImage;


public class OracleDbSetup {

    private static final String STABLE_VERSION = "gvenzl/oracle-xe:21-slim-faststart";
    private static final String LATEST_VERSION = "gvenzl/oracle-free:23.26.2-slim-faststart"; //ghcr.io/oracle/adb-free:26.2.4.2-26ai

    private static final String DOCKER_IMAGE = resolveDockerImage(STABLE_VERSION, LATEST_VERSION);


    public static final OracleContainer DB_CONTAINER =  new OracleContainer(DockerImageName.parse(DOCKER_IMAGE)
            .asCompatibleSubstituteFor("gvenzl/oracle-xe"))
            .withEnv("TZ", "Europe/Kiev")
            .withExposedPorts(1521)
            .withUsername("ADMIN")
            .withPassword("ADMIN")
            .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                    new PortBinding(Ports.Binding.bindPort(1521),
                            new ExposedPort(1521))))
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource(getDumpVersion()),
                    "/container-entrypoint-initdb.d/01-dump.sql");

    static String getDumpVersion() {
        String dockerVersion = System.getProperty("dockerTestVersion");

        if ("latest".equalsIgnoreCase(dockerVersion)) {
            return "dump/oracle_setup_new.sql";
        }
        return "dump/oracle_setup.sql";
    }

    static {
        printVersion(DOCKER_IMAGE);

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
