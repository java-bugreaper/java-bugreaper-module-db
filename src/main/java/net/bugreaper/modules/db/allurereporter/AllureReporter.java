package net.bugreaper.modules.db.allurereporter;

import io.qameta.allure.Allure;

import static net.bugreaper.modules.db.allurereporter.AllureBuilder.reportHtmlIWantToSeeBuilder;


public class AllureReporter {

    private AllureReporter() {
        throw new IllegalStateException("Utility class");
    }

    private static final String TYPE_TEXT = "text/plain";


    /**
     * Method to create attachment with executed query
     *
     * @param query sql query
     */
    public static void executeQueryReporter(String query) {
        Allure.addAttachment("execute sql with", TYPE_TEXT, query + ";");
    }


    /**
     * Method to create attach with table by:
     *
     * @param name       message + table name with schema ("SCHEMA.TABLE_1")
     * @param conditions conditions ({{"ID", "1"},...})
     */
    public static void attachTableForDb(String name, Object[][] conditions) {
        reportHtmlIWantToSeeBuilder(name, conditions);
    }


}