package io.bugreaper.modules.db.jdbc.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.bugreaper.modules.db.logger.Log;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {

    private final HikariDataSource ds;

    DataSource(String dbUrl,
               String username,
               String passwd) {

        HikariConfig config = getHikariConfig(dbUrl, username, passwd);

        Log.LOGGER.debug("HikariCP pool started: {}", config.getPoolName());

        ds = new HikariDataSource(config);

        // Shutdown hook to close pool automatically when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!ds.isClosed()) {
                ds.close();
                Log.LOGGER.debug("HikariCP pool closed: {}", config.getPoolName());
            }
        }));

    }

    private HikariConfig getHikariConfig(String dbUrl, String username, String passwd) {
        HikariConfig config = new HikariConfig();
        config.setPoolName(username + System.currentTimeMillis());

        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(passwd);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(1000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        config.addDataSourceProperty("oracle.jdbc.timezoneAsRegion", "false");

        return config;
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

}