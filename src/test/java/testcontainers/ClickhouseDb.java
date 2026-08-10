package testcontainers;

import net.bugreaper.modules.db.CustomDb;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.utility.MountableFile;

import static testcontainers.DockerSetup.printVersion;
import static testcontainers.DockerSetup.resolveDockerImage;


public class ClickhouseDb {

    private static final String STABLE_VERSION = "clickhouse/clickhouse-server:25.8";
    private static final String LATEST_VERSION = "clickhouse/clickhouse-server:26.3.17.56";

    private static final String DOCKER_IMAGE = resolveDockerImage(STABLE_VERSION, LATEST_VERSION);


    public static final ClickHouseContainer DB_CONTAINER =
            new ClickHouseContainer(DOCKER_IMAGE)
                    .withUsername("default")
                    .withPassword("password")
                    .withCopyFileToContainer(MountableFile.forClasspathResource("dump/clickhouse_setup.sql"), "/docker-entrypoint-initdb.d/01_schema.sql")
                    .withDatabaseName("MYSCHEMA");


    static {
        printVersion(DOCKER_IMAGE);

        DB_CONTAINER.setPortBindings(java.util.List.of("8123:8123", "9000:9000"));
        DB_CONTAINER.start();
    }

    public static CustomDb getCustomDb() {
        return new CustomDb(
                "jdbc:clickhouse://"
                        + DB_CONTAINER.getHost() + ":"
                        + DB_CONTAINER.getMappedPort(8123)
                + "/MYSCHEMA",
                "default",
                "password");
    }

}
