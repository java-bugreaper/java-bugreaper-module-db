package io.bugreaper.modules.db.jdbc.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {

    private final HikariDataSource ds;

    DataSource(String dbUrl,
               String username,
               String passwd) {

        HikariConfig config = new HikariConfig();
        config.setPoolName(username);

        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(passwd);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(1000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("oracle.jdbc.timezoneAsRegion", "false");
        ds = new HikariDataSource(config);

    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}