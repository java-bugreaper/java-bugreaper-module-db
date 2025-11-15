package io.bugreaper.modules.db.jdbc.interfaces;


import io.bugreaper.modules.db.jdbc.JdbcExecutor;

public interface SqlPost {

    /**
     * Merge arrays with data for insert into table
     * <p><b>Wrap this method with table name and template to provide only providedArray</b>
     *
     * <p>
     * example:<br>
     * tableName: "schema.table1"<br>
     * templateArray {{"id", "2"}, {"name", "Alex"}, {"age", "21"}, {"fee", "33.05"}}<br>
     * providedArray {{"name", "Nikita"}, {"fee", null}}<br>
     *
     * result:<br>
     * INSERT INTO schema.table1<br>
     * (id, name, age, fee)<br>
     * VALUES('2', 'Nikita', '21', null)
     *
     * @param tableName     schema_name.table_name
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
     * <p><b>Wrap this method with table name and template to provide only providedArray</b>
     *
     * <p>
     * example:<br>
     * tableName: "schema.table1"<br>
     * (will search in resources file templates/{database_type}/schema.table1.json)<br>
     * default path can be changed by {@link JdbcExecutor#setTemplateDirectory(String)}<br>
     * {<br>
     *     "id": 2,<br>
     *     "name": "Alex",<br>
     *     "age": 21,<br>
     *     "fee": 33.05<br>
     * }<br>
     * <b>NOTE:</b> for oracleDb dateTime insert supported 3 elements in json {"CREATE_DATE:TIMESTAMP", "2020-01-01 10:00:00"}<br>
     *                      CREATE_DATE = TO_DATE('2020-01-01 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),<br>
     * providedArray {{"name", "Nikita"}, {"fee", null}}<br>
     *
     * result:<br>
     * INSERT INTO schema.table1<br>
     * (id, name, age, fee)<br>
     * VALUES(2, 'Nikita', 21, null)
     *
     * @param tableName     schema_name.table_name
     * @param providedArray Object[][] with test data for merge {{"column1", "test_value1"},...}<br>
     * <b>NOTE:</b> for oracleDb dateTime insert supported 3 elements {"CREATE_DATE", "2020-01-01 10:00:00", "TIMESTAMP"}<br>
     *                      CREATE_DATE = TO_DATE('2020-01-01 10:00:00', 'YYYY-MM-DD HH24:MI:SS'),
     */
    void insertIntoTable(String tableName, Object[][] providedArray);

    /**
     * Execute sql script from file
     * used for init db on test startUp
     *
     * @param path path to file in resources
     */
    void runScriptFromFile(String path);

    /**
     * Execute sql script manual
     * used for manual scripts (not recommended)
     *
     * @param script hardcode SQL script
     */
    void runScript(String script);

    /**
     * Truncate(clean) table
     *
     * @param tableName schema_name.table_name
     */
    void truncateTable(String tableName);

    /**
     * Truncate(clean) tables
     *
     * @param tablesNames list of tables
     */
    void truncateTables(String... tablesNames);

    /**
     * Delete all records in table
     *
     * @param tableName schema_name.table_name
     */
    void deleteFromTable(String tableName);

    /**
     * Delete from table by conditions
     *
     * @param tableName       schema_name.table_name
     * @param conditionsArray Object[][] {{"column1", "value1"},...}
     */
    void deleteFromTable(String tableName,
                         Object[][] conditionsArray);

    /**
     * Delete from table by conditions
     *
     * @param tableName  schema_name.table_name
     * @param conditions String "where id=1"
     */
    void deleteFromTable(String tableName,
                         String conditions);

    /**
     * Update all records in table
     *
     * @param tableName schema_name.table_name
     * @param setArray  Object[][] {{"column1", "value1"},...}
     */
    void updateTable(String tableName,
                     Object[][] setArray);

    /**
     * Update records in table by conditions
     *
     * @param tableName       schema_name.table_name
     * @param setArray        Object[][] {{"column1", "value1"},...}
     * @param conditionsArray Object[][] {{"column1", "value1"},...}
     */
    void updateTable(String tableName,
                     Object[][] setArray,
                     Object[][] conditionsArray);

    /**
     * Update records in table by conditions
     *
     * @param tableName  schema_name.table_name
     * @param conditions String "SET USERNAME='Alex' where ID=5"
     */
    void updateTable(String tableName,
                     String conditions);
}