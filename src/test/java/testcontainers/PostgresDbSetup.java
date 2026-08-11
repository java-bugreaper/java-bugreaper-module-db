package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import net.bugreaper.modules.db.PostgreSQL;

import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Objects;
import java.util.TimeZone;

import static testcontainers.DockerSetup.printVersion;
import static testcontainers.DockerSetup.resolveDockerImage;


public class PostgresDbSetup {



    private static final String STABLE_VERSION = "postgres:14.23";
    private static final String LATEST_VERSION = "postgres:18.4";

    private static final String DOCKER_IMAGE = resolveDockerImage(STABLE_VERSION, LATEST_VERSION);


    public static final PostgreSQLContainer DB_CONTAINER = new PostgreSQLContainer(DOCKER_IMAGE)
            .withInitScript("dump/postgres_setup.sql")
            .withEnv("TZ", "Europe/Kyiv")
            .withDatabaseName("public")
            .withUsername("postgres")
            .withPassword("postgres")
            .withExposedPorts(5432)
            .withCreateContainerCmdModifier(cmd ->
                    Objects.requireNonNull(cmd.getHostConfig())
                            .withPortBindings(
                                    new PortBinding(
                                            Ports.Binding.bindPort(5432),
                                            new ExposedPort(5432))));

    static {
        printVersion(DOCKER_IMAGE);

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));//if local or runner problem with "Europe/Kiev"
        DB_CONTAINER.start();
    }


    public static PostgreSQL getPostgresDb() {
        return new PostgreSQL(

                "jdbc:postgresql://"
                        + DB_CONTAINER.getHost() + ":"
                        + DB_CONTAINER.getMappedPort(5432)
                        + "/public",
                "postgres",
                "postgres");
    }

}
