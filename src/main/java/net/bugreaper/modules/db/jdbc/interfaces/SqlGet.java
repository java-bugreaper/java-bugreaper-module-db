package net.bugreaper.modules.db.jdbc.interfaces;

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
     * Assert of records EXACTLY count by conditions
     * <p><b>with await</b></p>
     *
     * @param tableName       schema_name.table_name
     * @param conditionsArray Object[][] with template {{"column1", "value1"},...}
     * @param expectedCount   expected rows count
     * @throws AssertionError on assert fail (and adds Allure attachments)
     */
    void seeRowsCountInTableExactly(String tableName,
                                    Object[][] conditionsArray,
                                    int expectedCount);

    /**
     * Assert of records EXACTLY count by conditions
     * <p><b>with await</b></p>
     *
     * @param tableName     schema_name.table_name
     * @param conditions    String "where id=1"
     * @param expectedCount expected rows count
     * @throws AssertionError on assert fail (and adds Allure attachments)
     */
    void seeRowsCountInTableExactlyCustom(String tableName,
                                    String conditions,
                                    int expectedCount);

    /**
     * Assert of records EXACTLY count
     * <p><b>with await</b></p>
     *
     * @param tableName     schema_name.table_name
     * @param expectedCount expected rows count
     * @throws AssertionError on assert fail (and adds Allure attachments)
     */
    void seeRowsCountInTableExactly(String tableName,
                                    int expectedCount);

    /**
     * Assert of records EXACTLY count
     * <p><b>with await</b></p>
     * <p>Use custom sql conditions only if it`s necessary, otherwise use {@link #seeRowsCountInTableExactly}
     *
     * @param tableName     schema_name.table_name
     * @param providedJson String with json """{"column1": "test_value1", ...}"""<br>
     *     support up to 2 options in key after field <br>
     *    "CREATE_DATE:>="<br>
     *    "CREATE_DATE:=:TIMESTAMP" (for oracleDd)<br>
     * @param expectedCount expected rows count
     * @throws AssertionError on assert fail (and adds Allure attachments)
     */
    void seeRowsCountInTableExactly(String tableName,
                                    String providedJson,
                                    int expectedCount);


    /**
     * Assert That table has no records
     * <p><b>with await</b></p>
     *
     * @param tableName schema_name.table_name
     * @throws AssertionError on assert fail (and adds Allure attachments)
     */
    void seeTableIsEmpty(String tableName);


    /**
     * Assert That table has at least one record
     * <p><b>with await</b></p>
     *
     * @param tableName schema_name.table_name
     * @throws AssertionError on assert fail (and adds Allure attachments)
     */
    void seeTableDoesNotEmpty(String tableName);


    /**
     * Assert that exists at least one record by conditions
     * <p><b>with await</b></p>
     *
     * @param tableName       schema_name.table_name
     * @param conditionsArray Object[][] with template {{"column1", "value1"},...}
     * @throws AssertionError on assert fail (and adds Allure attachments)
     */
    void seeRecordExistsInTable(String tableName,
                                Object[][] conditionsArray);

    /**
     * Assert that exists at least one record by conditions
     * <p><b>with await</b></p>
     *
     * @param tableName       schema_name.table_name
     * @param providedJson String with json """{"column1": "test_value1", ...}"""<br>
     *     support up to 2 options in key after field <br>
     *    "CREATE_DATE:>="<br>
     *    "CREATE_DATE:=:TIMESTAMP" (for oracleDd)<br>
     * @throws AssertionError on assert fail (and adds Allure attachments)
     */
    void seeRecordExistsInTable(String tableName,
                                String providedJson);

    /**
     * Assert that exists at least one record by conditions with await
     * <p>Use custom sql conditions only if it`s necessary, otherwise use {@link #seeRecordExistsInTable}
     *
     * @param tableName  schema_name.table_name
     * @param conditions String "where id=1"
     * @throws AssertionError on assert fail (and adds Allure attachments)
     */
    void seeRecordExistsInTableCustom(String tableName, String conditions);


    /**
     * Method to create attachment with all rows by all columns in table
     *
     * @param tableName schema_name.table_name
     */
    void showDataFromTable(String tableName);

    /**
     * Method to show full record/records by condition if exists
     *
     * @param tableName       schema_name.table_name
     * @param conditionsArray Object[][] with conditions {{"ID", "1"}, {"NAME", "Alex"}}
     * @throws AssertionError if record/s not found by conditions
     */
    void showDataByCondition(String tableName, Object[][] conditionsArray);

}