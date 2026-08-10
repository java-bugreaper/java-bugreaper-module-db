package net.bugreaper.modules.db.jdbc.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.logger.Log;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class DataSource {

    private final HikariDataSource ds;

    DataSource(String dbUrl,
               String username,
               String passwd,
               String dbType) {

        HikariConfig config = getHikariConfig(dbUrl, username, passwd, dbType);

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

    private HikariConfig getHikariConfig(String dbUrl, String username, String passwd, String dbType) {
        HikariConfig config = new HikariConfig();
        config.setPoolName(dbType + "_" + System.currentTimeMillis());

        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(passwd);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(1000);

        if(Objects.equals(dbType, DbTypes.ORACLE_DB.value())) {
            config.addDataSourceProperty("oracle.jdbc.timezoneAsRegion", "false");
        }

        return config;
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

}