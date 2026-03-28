package net.bugreaper.modules.db;

public enum DbTypes {

    MARIA_DB("mariadb"),
    MS_SQL("sqlserver"),
    MY_SQL("mysql"),
    ORACLE_DB("oracle"),
    POSTGRE_SQL("postgresql"),
    CUSTOM_DB("custom-db");

    private final String value;

    DbTypes(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
