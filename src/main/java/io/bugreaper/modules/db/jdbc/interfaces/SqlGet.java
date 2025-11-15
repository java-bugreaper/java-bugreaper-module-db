package io.bugreaper.modules.db.jdbc.interfaces;


import org.awaitility.core.ConditionTimeoutException;

public interface SqlGet {

    /**
     * Assert of records EXACTLY count by conditions with await
     *
     * @param tableName       schema_name.table_name
     * @param conditionsArray Object[][] with template {{"column1", "value1"},...}
     * @param expectedCount   expected rows count
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    void seeRowsCountInTableExactly(String tableName,
                                    Object[][] conditionsArray,
                                    int expectedCount);

    /**
     * Assert of records EXACTLY count by conditions with await
     *
     * @param tableName     schema_name.table_name
     * @param conditions    String "where id=1"
     * @param expectedCount expected rows count
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    void seeRowsCountInTableExactly(String tableName,
                                    String conditions,
                                    int expectedCount);

    /**
     * Assert of records EXACTLY count with await
     *
     * @param tableName     schema_name.table_name
     * @param expectedCount expected rows count
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    void seeRowsCountInTableExactly(String tableName,
                                    int expectedCount);

    /**
     * Assert of records EXACTLY count by conditions with custom await
     *
     * @param tableName       schema_name.table_name
     * @param conditionsArray Object[][] with template {{"column1", "value1"},...}
     * @param expectedCount   expected rows count
     * @param awaitMs         await in ms
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    void seeRowsCountInTableExactly(String tableName,
                                    Object[][] conditionsArray,
                                    int expectedCount,
                                    int awaitMs);

    /**
     * Assert of records EXACTLY count by conditions with custom await
     *
     * @param tableName     schema_name.table_name
     * @param conditions    String "where id=1"
     * @param expectedCount expected rows count
     * @param awaitMs       await in ms
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    void seeRowsCountInTableExactly(String tableName,
                                    String conditions,
                                    int expectedCount,
                                    int awaitMs);

    /**
     * Assert of records EXACTLY count with custom await
     *
     * @param tableName     schema_name.table_name
     * @param expectedCount expected rows count
     * @param awaitMs       await in ms
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    void seeRowsCountInTableExactly(String tableName, int expectedCount, int awaitMs);


    /**
     * Assert That table has at least one record with await
     *
     * @param tableName schema_name.table_name
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    void seeTableIsEmpty(String tableName);


    /**
     * Assert That table has no records with await
     *
     * @param tableName schema_name.table_name
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    void seeTableDoesNotEmpty(String tableName);


    /**
     * Assert that exists at least one record by conditions with await
     *
     * @param tableName       schema_name.table_name
     * @param conditionsArray Object[][] with template {{"column1", "value1"},...}
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    void seeRecordExistsInTable(String tableName,
                                Object[][] conditionsArray);

    /**
     * Assert that exists at least one record by conditions with await
     *
     * @param tableName  schema_name.table_name
     * @param conditions String "where id=1"
     * @throws ConditionTimeoutException on assert fail (and adds Allure attachments)
     */
    void seeRecordExistsInTable(String tableName, String conditions);


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
     * @throws ConditionTimeoutException if record/s not found by conditions
     */
    void showDataByCondition(String tableName, Object[][] conditionsArray);

}