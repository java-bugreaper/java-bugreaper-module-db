package io.bugreaper.modules.db.helpers;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

public class Convert {

    private static final String ORACLE_URL = "jdbc:oracle:thin:@//localhost/XEPDB1";
    private static final String MARIADB_URL = "jdbc:mariadb://localhost:3306";
    private static final String MSSQL_URL = "jdbc:sqlserver://localhost:1433;databaseName=master";
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306";
    private static final String POSTGE_URL = "jdbc:postgresql://localhost:5432/public";


    public static String convertToDbType(String dbUrl){
        return  StringUtils.substringBefore(URI.create(dbUrl).getSchemeSpecificPart(), ":");
    }

    public static String getMySqlUrl(){
        return MYSQL_URL;
    }

    public static String getMariaDbUrl(){
        return MARIADB_URL;
    }

    public static String getOracleDbUrl(){
        return ORACLE_URL;
    }

    public static String getMsSqlUrl(){
        return MSSQL_URL;
    }

    public static String getPostgreSqlUrl(){
        return POSTGE_URL;
    }
}
