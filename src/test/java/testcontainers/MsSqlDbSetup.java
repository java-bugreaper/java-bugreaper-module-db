package testcontainers;

import io.bugreaper.modules.db.MsSQL;
import org.testcontainers.mssqlserver.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;


public class MsSqlDbSetup {

    //"mcr.microsoft.com/mssql/server:2022-CU20-ubuntu-22.04" 2025+ support JSON
    static final DockerImageName MS_DB = DockerImageName.parse("mcr.microsoft.com/mssql/server:2025-RC1-ubuntu-24.04");
    MSSQLServerContainer container = new MSSQLServerContainer(MS_DB);
    private static MsSqlDbSetup instance;


    public MsSqlDbSetup() {
        container
                .acceptLicense()
                .withInitScript("dump/mssql_setup.sql")
                .start();
    }

    public static MsSqlDbSetup getInstance() {
        if (instance == null) {
            instance = new MsSqlDbSetup();
        }

        return instance;
    }

    public MsSQL getMssqlDb() {
        return new MsSQL(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword());
    }

}
