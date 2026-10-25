package net.bugreaper.modules.db.jdbc.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.bugreaper.core.config.ConfigMap;
import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.logger.Log;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.bugreaper.core.config.ConfigurationReader.formatConfiguration;


/**
 * Configuration and client factory for a DataSource client.
 *
 * <p>DataSource client settings are created with the following priority, from highest
 * to lowest:
 * <ol>
 *     <li>Base YAML configuration options</li>
 *     <li>Optional properties/hikari YAML configuration options</li>
 *     <li>JdbcUrl string options</li>
 *     <li>Default bugreaper settings</li>
 *     <li>DataSource driver defaults</li>
 * </ol>
 *
 * <p>Supported configuration options:
 * <pre>
 * modules:
 *   db:
 *     mariadb: # for example MariaDb
 *       url: 'jdbc:mariadb://localhost:3307?connectTimeout=5001'
 *       username: test_user
 *       password: secret_pass
 *       await: 510 # optional
 *       await-poll-interval: 200 # optional
 *       enchanted-report: true # optional
 *
 *       # JDBC driver configuration
 *       properties: # optional
 *         connectTimeout: 3001
 *         socketTimeout: 3002
 *
 *       # Hikari configuration
 *       hikari: # optional and validated
 *         poolName: mariadb-pool
 *         minimumIdle: 2
 *         maximumPoolSize: 4
 *         maxLifetime: 600000
 *         keepAliveTime: 110000
 *         connectionTimeout: 10000
 *         validationTimeout: 9000
 *         idleTimeout: 10555
 *         leakDetectionThreshold: 0
 *         readOnly: false
 * </pre>
 * <p>HikariCP configuration options are validated and supported only from example</p>
 * <p>JDBC driver configuration is specific to each database type.</p>
 *
 * <p>The created {@link HikariDataSource} is automatically closed by a JVM shutdown
 * hook.
 */
public class DataSource {

    private final HikariDataSource ds;
    private HikariConfig config;

    private static final int MAX_POOL_SIZE = 5;
    private static final int CONNECTION_TIMEOUT = 2000;


    /**
     * Creates a DataSource client configured with JDBC connection settings,
     * HikariCP configuration, and custom data source properties.
     *
     * @param dbUrl JDBC connection URL
     * @param username database username
     * @param passwd database password
     * @param dbType database type
     * @param hikariConfig HikariCP configuration options
     * @param dataSourceProperties custom data source properties
     */
    DataSource(String dbUrl,
               String username,
               String passwd,
               String dbType,
               ConfigMap hikariConfig,
               ConfigMap dataSourceProperties) {

        applyConfig(dbUrl, username, passwd, dbType, hikariConfig, dataSourceProperties);

        this.ds = new HikariDataSource(config);

        Log.LOGGER.debug("HikariCP pool started: {}", ds.getPoolName());



        // Shutdown hook to close pool automatically when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!ds.isClosed()) {
                ds.close();
                Log.LOGGER.debug("HikariCP pool closed: {}", config.getPoolName());
            }
        }));

    }

    private void applyConfig(
            String dbUrl,
            String username,
            String passwd,
            String dbType,
            ConfigMap hikariConfig,
            ConfigMap dataSourceProperties) {

        config = new HikariConfig();

        config.setJdbcUrl(dbUrl);

        applyDefaultSettings(dbType);

        // provide hikari settings if exists
        if (hikariConfig != null && !hikariConfig.isEmpty()) {
            applyHikariSettings(hikariConfig);
            //check unsupported hikari configurations
            hikariConfig.checkUnknown();
        }

        // provide dataSourceProperties if exists
        if (dataSourceProperties != null && !dataSourceProperties.isEmpty()) {
            applyDataSourceProperties(dataSourceProperties);
        }

        // provide credentials
        config.setUsername(username);
        config.setPassword(passwd);
    }

    private void applyDefaultSettings(String dbType) {

        config.setPoolName(dbType + "_" + System.currentTimeMillis());
        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setConnectionTimeout(CONNECTION_TIMEOUT);

        //default properties only for oracle
        if (Objects.equals(dbType, DbTypes.ORACLE_DB.value())) {

            config.addDataSourceProperty("oracle.jdbc.timezoneAsRegion", "false");

            //to fix ORA-18722 (on select * cant parse json data)
            config.addDataSourceProperty(
                    "oracle.jdbc.jsonDefaultGetObjectType",
                    "java.lang.String"
            );

        }
    }


    private void applyHikariSettings(ConfigMap hikariConfig) {

        for (String key : hikariConfig.copyKeySet()) {
            switch (key) {
                case "poolName" ->
                        config.setPoolName(
                                hikariConfig.getString(key));

                case "minimumIdle" ->
                        config.setMinimumIdle(
                                hikariConfig.getInt(key));

                case "maximumPoolSize" ->
                        config.setMaximumPoolSize(
                                hikariConfig.getInt(key));

                case "maxLifetime" ->
                        config.setMaxLifetime(
                                hikariConfig.getLong(key));

                case "keepAliveTime" ->
                        config.setKeepaliveTime(
                                hikariConfig.getLong(key));

                case "connectionTimeout" ->
                        config.setConnectionTimeout(
                                hikariConfig.getLong(key));

                case "validationTimeout" ->
                        config.setValidationTimeout(
                                hikariConfig.getLong(key));

                case "idleTimeout" ->
                        config.setIdleTimeout(
                                hikariConfig.getLong(key));

                case "leakDetectionThreshold" ->
                        config.setLeakDetectionThreshold(
                                hikariConfig.getLong(key));

                case "readOnly" ->
                        config.setReadOnly(
                                hikariConfig.getBoolean(key));
                default -> {
                    // skip
                }

            }

        }

    }

    private void applyDataSourceProperties(ConfigMap dataSourceProperties) {
        dataSourceProperties.forEach(config::addDataSourceProperty);
    }

    /**
     * Returns a connection from the HikariCP connection pool.
     *
     * @return a database connection
     * @throws SQLException if a database connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    /**
     * Returns the current data source properties.
     *
     * @return formatted string containing the data source properties
     */
    public String getDataSourceProperties() {

        String properties = config.getDataSourceProperties().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(System.lineSeparator()));

        return "%nData Source Properties:%n%s".formatted(properties);
    }

    /**
     * Returns current HikariCP settings.
     *
     * @return formatted string containing the current HikariCP settings
     */
    public String getHikariSettings() {
        return "%nHikari Settings:%n%s".formatted(formatConfiguration(config));
    }

}