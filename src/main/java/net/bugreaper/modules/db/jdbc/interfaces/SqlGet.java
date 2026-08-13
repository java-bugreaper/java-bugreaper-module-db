package net.bugreaper.modules.db.jdbc.interfaces;

import net.bugreaper.modules.db.exceptions.QueryBuilderException;

/**
 * Defines operations for executing SQL SELECT queries.
 * <p>
 * Implementations of this interface are responsible for performing
 * read-only database operations that retrieve data without modifying
 * the database state, such as get data for asserts or reports.
 * </p>
 *
 * <p>
 * This interface should contain only non-mutating operations.
 * All data-changing SQL commands must be handled by {@link SqlPost}.
 * </p>
 */
public interface SqlGet {

    /**
     * Asserts that the table contains exactly the expected number of records matching the specified conditions (in an Object[][] array).
     *
     * <p><b>Uses await.</b></p>
     *
     * <p>for example:
     * <pre>{@code
     * db.seeRowsCountInTableExactly("test.users", new Object[][]{
     *      {"name", "Alex"},
     *      {"id", ">=", 2}
     * }, 1);}</pre>
     *
     * @param tableName       table name in the format {@code schema_name.table_name}
     * @param conditionsArray conditions in Object[][] format
     *                        support up to 2 options after field (column, condition, date, oracle-enum)<br>
     *                        {@code {"COLUMN NAME", ">=", "2020-01-01", "DATE"}} for oracle<br>
     * @param expectedCount   expected rows count
     * @throws AssertionError        if the assertion fails (and adds Allure attachments)
     * @throws QueryBuilderException if failed to convert Object[][]
     */
    void seeRowsCountInTableExactly(String tableName,
                                    Object[][] conditionsArray,
                                    int expectedCount);

    /**
     * Asserts that the table contains exactly the expected number of records matching the specified conditions (hardcoded SQL conditions string).
     *
     * <p><b>Uses await.</b></p>
     *
     * <p>for example:
     * <pre>{@code
     * db.seeRowsCountInTableExactly("test.users", "where id=1", 1);}</pre>
     *
     * @param tableName     table name in the format {@code schema_name.table_name}
     * @param conditions    hardcoded SQL conditions string
     *                      <p>for example: {@code "where id=1"}</p>
     * @param expectedCount expected rows count
     * @throws AssertionError if the assertion fails (and adds Allure attachments)
     */
    void seeRowsCountInTableExactlyCustom(String tableName,
                                          String conditions,
                                          int expectedCount);

    /**
     * Asserts that the table contains exactly the expected number of records.
     *
     * <p><b>Uses await.</b></p>
     *
     * @param tableName     table name in the format {@code schema_name.table_name}
     * @param expectedCount expected rows count
     * @throws AssertionError if the assertion fails (and adds Allure attachments)
     */
    void seeRowsCountInTableExactly(String tableName,
                                    int expectedCount);

    /**
     * Asserts that the table contains exactly the expected number of records matching the specified conditions (in a JSON string).
     *
     * <p><b>Uses await.</b></p>
     *
     * <p>for example:
     * <pre>{@code
     * db.seeRowsCountInTableExactly("test.users",
     *      """
     *      {
     *          "name": "Alex",
     *          "id:>": 2
     *      }"""), 1;}</pre>
     *
     * @param tableName     table name in the format {@code schema_name.table_name}
     * @param providedJson  conditions in JSON format
     *                      support up to 2 options in key after field (column:condition:oracle-enum)<br>
     *                      {@code "CREATE_DATE:>="}<br>
     *                      {@code "CREATE_DATE:=:TIMESTAMP"} (for oracleDd)<br>
     * @param expectedCount expected rows count
     * @throws AssertionError        if the assertion fails (and adds Allure attachments)
     * @throws QueryBuilderException if failed to convert JSON
     */
    void seeRowsCountInTableExactly(String tableName,
                                    String providedJson,
                                    int expectedCount);


    /**
     * Asserts that the table has no records.
     *
     * <p><b>Uses await.</b></p>
     *
     * @param tableName table name in the format {@code schema_name.table_name}
     * @throws AssertionError if the assertion fails (and adds Allure attachments)
     */
    void seeTableIsEmpty(String tableName);


    /**
     * Asserts that the table is not empty.
     *
     * <p><b>Uses await.</b></p>
     *
     * @param tableName table name in the format {@code schema_name.table_name}
     * @throws AssertionError if the assertion fails (and adds Allure attachments)
     */
    void seeTableIsNotEmpty(String tableName);


    /**
     * Asserts that the table has at least one record by conditions (in an Object[][] array).
     *
     * <p><b>Uses await.</b></p>
     *
     * <p>for example:
     * <pre>{@code
     * db.seeRecordExistsInTable("test.users", new Object[][]{
     *      {"name", "Alex"},
     *      {"id", ">=", 2}
     * });}</pre>
     *
     * @param tableName       table name in the format {@code schema_name.table_name}
     * @param conditionsArray conditions in Object[][] format
     *                        support up to 2 options after field (column, condition, date, oracle-enum)<br>
     *                        {@code {"COLUMN NAME", ">=", "2020-01-01", "DATE"}} for oracle<br>
     * @throws AssertionError if the assertion fails (and adds Allure attachments)
     */
    void seeRecordExistsInTable(String tableName,
                                Object[][] conditionsArray);

    /**
     * Asserts that the table has at least one record by conditions (in a JSON string).
     *
     * <p><b>Uses await.</b></p>
     *
     * <p>for example:
     * <pre>{@code
     * db.seeRecordExistsInTable("test.users",
     *      """
     *      {
     *          "name": "Alex",
     *          "id:>": 2
     *      }""");}</pre>
     *
     * @param tableName    table name in the format {@code schema_name.table_name}
     * @param providedJson  conditions in JSON format
     *                      support up to 2 options in key after field (column:condition:oracle-enum)<br>
     *                      {@code "CREATE_DATE:>="}<br>
     *                      {@code "CREATE_DATE:=:TIMESTAMP"} (for oracleDd)<br>
     * @throws AssertionError if the assertion fails (and adds Allure attachments)
     */
    void seeRecordExistsInTable(String tableName,
                                String providedJson);

    /**
     * Asserts that the table has at least one record by conditions (hardcoded SQL condition string).
     *
     * <p><b>Uses await.</b></p>
     *
     * <p>for example:
     * <pre>{@code
     * db.seeRecordExistsInTableCustom("test.users", "where id=1");}</pre>
     *
     * @param tableName  table name in the format {@code schema_name.table_name}
     * @param conditions    hardcoded SQL condition string
     *                      <p>for example: {@code "where id=1"}</p>
     * @throws AssertionError if the assertion fails (and adds Allure attachments)
     */
    void seeRecordExistsInTableCustom(String tableName, String conditions);


    /**
     /**
     * Creates an attachment containing all records and columns from the table.
     *
     * @param tableName table name in the format {@code schema_name.table_name}
     */
    void showDataFromTable(String tableName);

    /**
     * Creates an attachment containing all records matching the specified conditions.
     *
     * <pre>{@code
     * db.showDataByCondition("test.users", new Object[][]{
     *      {"ID", 1},
     *      {"NAME", "Alex"}
     * });}</pre>
     *
     * @param tableName       table name in the format {@code schema_name.table_name}
     * @param conditionsArray conditions in the format {@code {{"ID", "1"}, {"NAME", "Alex"}}}
     * @throws AssertionError if no records are found matching the specified conditions
     */
    void showDataByCondition(String tableName, Object[][] conditionsArray);

}