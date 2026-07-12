package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import net.bugreaper.modules.db.MsSQL;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

import java.util.Optional;


public class MsSqlDbSetup {

    //"mcr.microsoft.com/mssql/server:2022-CU20-ubuntu-22.04" 2025+ support JSON
    private static final MSSQLServerContainer DB_CONTAINER = new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2025-RC1-ubuntu-24.04")
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
            .withInitScript("dump/mssql_setup.sql");

    static {
        DB_CONTAINER.start();
    }

    public static MsSQL getMssqlDb() {

        return new MsSQL(
                DB_CONTAINER.getJdbcUrl(),
                DB_CONTAINER.getUsername(),
                DB_CONTAINER.getPassword());
    }

}
