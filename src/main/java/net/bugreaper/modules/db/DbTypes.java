package net.bugreaper.modules.db;

/**
 * Enumeration of supported database types.
 * <p>
 * Each enum constant represents a specific database vendor or configuration
 * and is associated with a string identifier used in configuration,
 * connection setup, or query processing.
 */
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

    /**
     * Returns the string representation of the database type.
     *
     * @return the database type value (e.g., "mariadb", "custom-db")
     */
    public String value() {
        return value;
    }

}
