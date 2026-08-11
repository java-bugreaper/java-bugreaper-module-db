package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import net.bugreaper.modules.db.MsSQL;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

import java.util.Optional;

import static testcontainers.DockerSetup.printVersion;
import static testcontainers.DockerSetup.resolveDockerImage;


public class MsSqlDbSetup {

    private static final String STABLE_VERSION = "mcr.microsoft.com/mssql/server:2022-CU26-ubuntu-22.04";
    private static final String LATEST_VERSION = "mcr.microsoft.com/mssql/server:2025-CU7-ubuntu-22.04"; // 2025+ support JSON

    private static final String DOCKER_IMAGE = resolveDockerImage(STABLE_VERSION, LATEST_VERSION);

    public static final MSSQLServerContainer DB_CONTAINER = new MSSQLServerContainer(DOCKER_IMAGE)
            .acceptLicense()
            .withPassword("YourStrong(!)Password")
            .withExposedPorts(1433)
            .withCreateContainerCmdModifier(cmd -> {
                HostConfig hc = Optional.ofNullable(cmd.getHostConfig())
                        .orElseGet(HostConfig::new);

                hc.withPortBindings(
                        new PortBinding(
                                Ports.Binding.bindPort(1435),
                                new ExposedPort(1433)
                        )
                );

                cmd.withHostConfig(hc);
            })
            .withInitScript(getDumpVersion());

    static {
        printVersion(DOCKER_IMAGE);
        DB_CONTAINER.start();
    }

    static String getDumpVersion() {
        String dockerVersion = System.getProperty("dockerTestVersion");

        if ("latest".equalsIgnoreCase(dockerVersion)) {
            return "dump/mssql_setup.sql"; //2025+ support JSONS
        }
        return "dump/mssql_setup_nojson.sql";
    }

    public static MsSQL getMssqlDb() {

        return new MsSQL(
                DB_CONTAINER.getJdbcUrl(),
                DB_CONTAINER.getUsername(),
                DB_CONTAINER.getPassword());
    }

}
