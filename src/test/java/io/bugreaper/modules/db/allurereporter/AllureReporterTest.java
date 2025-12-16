package io.bugreaper.modules.db.allurereporter;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

import static io.bugreaper.modules.db.allurereporter.AllureBuilder.reportHtmlIWantToSeeBuilder;
import static io.bugreaper.modules.db.allurereporter.AllureBuilder.reportHtmlTableAllBuilder;
import static java.nio.file.Files.readString;
import static org.junit.jupiter.api.Assertions.*;

class AllureReporterTest {

    final String filePath = "src/test/resources/";
    final String tableName = "SCHEMA.TABLE_1";


    @Test
    void utilClassTest() throws NoSuchMethodException {
        Constructor<AllureReporter> constructor = AllureReporter.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        Throwable cause = thrown.getCause();
        assert (cause instanceof IllegalStateException);
        assert ("Utility class".equals(cause.getMessage()));
    }


    @Test
    void testHtmlReportAllFromTableEmpty() throws IOException {
        var columns = new String[]{"ID", "NAME"};
        var dataRows = new String[]{};

        assertEquals(readString(Path.of(filePath + "testdata/allure/see_in_db_empty.html")),
                reportHtmlTableAllBuilder(tableName, columns, dataRows),
                "HTML with empty result from table");
    }

    @Test
    void testHtmlReportAllFromTableTwoRows() throws IOException {
        var columns = new String[]{"ID", "NAME", "IS_BOOL"};
        var dataRows = new Object[]{"1", "name_1", true, "2", null, false, "3", "", true};

        assertEquals(readString(Path.of(filePath + "testdata/allure/see_in_db_two_rows.html")),
                reportHtmlTableAllBuilder(tableName, columns, dataRows),
                "HTML with two rows in result from table");
    }

    @Test
    void testHtmlReportWantToSeeNull() {

        assertNull(
                reportHtmlIWantToSeeBuilder(tableName, null),
                "HTML with null");
    }

    @Test
    void testHtmlReportWantToSee() throws IOException {
        var conditions = new Object[][]{{"ID", "1"}, {"NAME", null}, {"PRICE", "100.00"}, {"COMMENT", "text"}};

        assertEquals(readString(Path.of(filePath + "testdata/allure/want_to_see_1.html")),
                reportHtmlIWantToSeeBuilder(tableName, conditions),
                "HTML with 3 elements in conditions");
    }

    @Test
    void testHtmlReportWantToSeeWithConditions() throws IOException {
        var conditions = new Object[][]{{"ID", "1"}, {"NAME", "is not", null}, {"PRICE", ">=", "100.00"}, {"COMMENT", "LIKE", "text"}};

        assertEquals(readString(Path.of(filePath + "testdata/allure/want_to_see_with_conditions_2.html")),
                reportHtmlIWantToSeeBuilder(tableName, conditions),
                "HTML with 3 elements in conditions");
    }

    @Test
    void testHtmlReportWantToSeeWithFourElementConditions() throws IOException {
        var conditions = new Object[][]{
                {"ORACLE_DATE", "=", "DATE", "2020-01-01", },
                {"DATA", "OPERATOR_1", "OPTION_2", "VALUE_3"}};

        assertEquals(readString(Path.of(filePath + "testdata/allure/want_to_see_with_4_conditions.html")),
                reportHtmlIWantToSeeBuilder(tableName, conditions),
                "HTML with 4 elements in conditions");
    }

    @Test
    void testHtmlReportWantToSeeWithOneElement() throws IOException {
        var conditions = new Object[][]{
                {"ORACLE_DATE"}};

        assertEquals(readString(Path.of(filePath + "testdata/allure/want_to_see_with_1_element.html")),
                reportHtmlIWantToSeeBuilder(tableName, conditions),
                "HTML with 1 element -> impossible (will be validate on QueryBuilder)");
    }

    @Test
    void testHtmlReportWantToSeeWithObjectsTypes() throws IOException {
        var conditions = new Object[][]{
                {"ID", 1},
                {"NUM", ">=", 35},
                {"PRICE", null},
                {"IS_ADMIN", true}};

        assertEquals(readString(Path.of(filePath + "testdata/allure/want_to_see_objects.html")),
                reportHtmlIWantToSeeBuilder(tableName, conditions),
                "HTML with objects");
    }

    @Test
    void testHtmlReportWantToSeeWithNull() {

        assertNull(
                reportHtmlIWantToSeeBuilder(tableName, null),
                "if condition is null, no allure attachment");
    }

}
