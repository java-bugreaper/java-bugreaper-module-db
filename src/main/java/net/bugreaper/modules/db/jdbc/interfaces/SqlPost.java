package net.bugreaper.modules.db.jdbc.interfaces;


import net.bugreaper.modules.db.enums.DbTypes;


/**
 * Defines operations for executing SQL data-modifying queries.
 * <p>
 * Implementations of this interface are responsible for performing
 * state-changing SQL operations
 * </p>
 *
 * <p>
 * This interface must not include any read-only operations.
 * Use {@link SqlGet} for SELECT queries.
 * </p>
 */
public interface SqlPost {

    /**
     * Merge arrays with data for insert into table
     * <p><b>Wrap this method with table name and template to provide only providedArray</b>
     * 
     * <p>example:
     * <pre>
     * tableName: "schema.table1"
     * templateArray {{"id", 2}, {"name", "Alex"}, {"age", 21}, {"fee", "33.05"}}
     * providedArray {{"name", "Nikita"}, {"fee", null}}
     *
     * result:
     * INSERT INTO schema.table1
     * (id, name, age, fee)
     * VALUES(2, 'Nikita', 21, null)
     * </pre>
     *
     * @param tableName     table name in the format {@code schema_name.table_name}
     * @param templateArray Object[][] with template {{"column1", "value1"},...}
     * @param providedArray Object[][] with test data for merge {{"column1", "test_value1"},...}<br>
     * <b>NOTE:</b> for oracleDb dateTime insert supported 3 elements {"CREATE_DATE", "2020-01-01 10:00:00", "TIMESTAMP"}<br>
     *                      CREATE_DATE = TO_DATE('2020-01-01 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
     */
    void insertIntoTable(String tableName,
                         Object[][] templateArray,
                         Object[][] providedArray);


    /**
     * Merge arrays with template data
     *
     * <p>
     * example:<br>
     * tableName: "schema.table1"<br>
     * (will search in resources file templates/{database_type}/schema.table1.json)<br>
     * database_types : {@link DbTypes}<br>
     * default path can be changed by {@link SqlConfig#setTemplatesDirectory(String)}<br>
     * <pre>
     *{
     *     "id": 2,
     *     "name": "Alex",
     *     "age": 21,
     *     "fee": 33.05
     * }
     * </pre>
     * providedArray {{"name", "Nikita"}, {"fee", null}}<br>
     *
     * result:<br>
     * INSERT INTO schema.table1<br>
     * (id, name, age, fee)<br>
     * VALUES(2, 'Nikita', 21, null)<br>
     *
     * <b>NOTE:</b> for oracleDb dateTime insert supported 3 elements in json {"CREATE_DATE:TIMESTAMP", "2020-01-01 10:00:00"}<br>
     *                      CREATE_DATE = TO_DATE('2020-01-01 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),<br>
     *
     * @param tableName     table name in the format {@code schema_name.table_name}
     * @param providedArray Object[][] with test data for merge {{"column1", "test_value1"},...}<br>
     * <b>NOTE:</b> for oracleDb dateTime insert supported 3 elements {"CREATE_DATE", "2020-01-01 10:00:00", "TIMESTAMP"}<br>
     *                      CREATE_DATE = TO_DATE('2020-01-01 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
     */
    void insertIntoTable(String tableName, Object[][] providedArray);

    /**
     * Merge arrays with template data
     *
     * <p>
     * example:<br>
     * tableName: "schema.table1"<br>
     * (will search in resources file templates/${database_type}/schema.table1.json)<br>
     * database_types : {@link DbTypes}<br>
     * default path can be changed by {@link SqlConfig#setTemplatesDirectory(String)}
     * <pre>
     *{
     *     "id": 2,
     *     "name": "Alex",
     *     "age": 21,
     *     "fee": 33.05
     * }
     * </pre>
     * providedJson:
     * <pre>
     *{
     *     "name": "Nikita",
     *     "fee": null
     * }
     * </pre>
     * result:<br>
     * INSERT INTO schema.table1<br>
     * (id, name, age, fee)<br>
     * VALUES(2, 'Nikita', 21, null)<br>
     *
     * @param tableName     table name in the format {@code schema_name.table_name}
     * @param providedJson String with json to merge """{"column1": "test_value1", ...}"""<br>
     * <b>NOTE:</b> for oracleDb dateTime insert supported 3 elements in json {"CREATE_DATE:TIMESTAMP", "2020-01-01 10:00:00"}<br>
     *                      CREATE_DATE = TO_DATE('2020-01-01 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),<br>
     */
    void insertIntoTable(String tableName, String providedJson);

    /**
     * Executes an SQL script from a file.
     *
     * <p>Used to initialize the database at test startup or in other cases.</p>
     *
     * @param path path to the file in test resources
     */
    void runScriptFromFile(String path);

    /**
     * Executes the specified SQL script manually.
     *
     * <p>Used for manual scripts and not recommended for regular test operations.</p>
     *
     * @param script hardcoded SQL script
     */
    void runScript(String script);

    /**
     * Truncates the table.
     *
     * @param tableName table name in the format {@code schema_name.table_name}
     */
    void truncateTable(String tableName);

    /**
     * Truncates the tables.
     *
     * @param tablesNames list of tables
     */
    void truncateTables(String... tablesNames);

    /**
     * Deletes all records from the table.
     *
     * @param tableName table name in the format {@code schema_name.table_name}
     */
    void deleteFromTable(String tableName);

    /**
     * Deletes all records from the table matching the specified conditions (in an Object[][] array).
     *
     * @param tableName       table name in the format {@code schema_name.table_name}
     * @param conditionsArray Object[][] {{"column1", "value1"},...}
     */
    void deleteFromTable(String tableName,
                         Object[][] conditionsArray);

    /**
     * Deletes all records from the table matching the specified conditions (hardcoded SQL condition string).
     *
     * @param tableName  table name in the format {@code schema_name.table_name}
     * @param conditions String "where id=1"
     */
    void deleteFromTable(String tableName,
                         String conditions);

    /**
     * Updates all records in the table.
     *
     * @param tableName table name in the format {@code schema_name.table_name}
     * @param setArray  values to update in the format {@code {{"column1", "value1"}, ...}}
     */
    void updateTable(String tableName,
                     Object[][] setArray);

    /**
     * Updates all records in the table matching the specified conditions (in an Object[][] array).
     *
     * @param tableName       table name in the format {@code schema_name.table_name}
     * @param setArray        Object[][] {{"column1", "value1"},...}
     * @param conditionsArray Object[][] {{"column1", "value1"},...}
     */
    void updateTable(String tableName,
                     Object[][] setArray,
                     Object[][] conditionsArray);

    /**
     * Updates all records in the table matching the specified conditions (hardcoded SQL condition string).
     *
     * @param tableName  table name in the format {@code schema_name.table_name}
     * @param conditions String "SET USERNAME='Alex' where ID=5"
     */
    void updateTable(String tableName,
                     String conditions);

    /**
     * Renames the specified table by adding the {@code "_X_"} suffix.
     *
     * <p><b>Note:</b> Roll back the table name after the test using {@link #rollbackTable}.</p>
     *
     * @param tableName table name in the format {@code schema_name.table_name}
     */
    void renameTable(String tableName);

    /**
     * Rolls back the table name by removing the {@code "_X_"} suffix added by {@link #renameTable}.
     *
     * @param tableName table name in the format {@code schema_name.table_name}
     */
    void rollbackTable(String tableName);

}