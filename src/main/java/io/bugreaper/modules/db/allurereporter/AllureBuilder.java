package io.bugreaper.modules.db.allurereporter;

import java.util.Map;

import static io.bugreaper.core.allurereporter.AllureReporter.createHtmlAllureAttachment;
import static io.bugreaper.core.mappers.StringMappers.stringMapperV2;

public class AllureBuilder {

    private AllureBuilder() {
        throw new IllegalStateException("Utility class");
    }

    private static final String ROW_END = "</tr>";
    private static final String HEADER_END = "</th>";
    private static final String CELL_END = "</td>";

    protected static String reportHtmlIWantToSeeBuilder(String name, Object[][] conditions) {

        // if no conditions, do nothing
        if (conditions == null) {
            return null;
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder dataRows = new StringBuilder();

        //create columns and data cells
        dataRows.append("<tr>");
        for (Object[] condition : conditions) {

            columns.append("<th>").append(condition[0]).append(HEADER_END);

            dataRows.append("<td>");
            if (condition.length > 2) {
                dataRows.append("<b><i>").append(condition[1]).append("</i></b> ").append(valueBeautifier(condition[2]));
            } else {
                dataRows.append(valueBeautifier(condition[1]));
            }
            dataRows.append(CELL_END);

        }
        dataRows.append(ROW_END).append("\n");

        // Allure attachment
        var attachment = reportHtmlBuilder(columns, dataRows);

        createHtmlAllureAttachment(name, attachment);

        return attachment;
    }

    private static Object valueBeautifier(Object value) {

        if (value == null) {
            return "<span style=\"color: silver;\">[NULL]</span>";
        }

        if (value instanceof Boolean) {
            return "<span style=\"color: #0EA5E9;\">" + value + "</span>";
        }

        return value;
    }

    public static String reportHtmlTableAllBuilder(String tableName, String[] columnsList, Object[] resultArray) {

        StringBuilder columns = new StringBuilder();
        StringBuilder dataRows = new StringBuilder();
        final int COLUMN_NUMBER = columnsList.length;

        //create columns names row
        for (String s : columnsList) {
            columns.append("<th>").append(s).append(HEADER_END);
        }

        //create records rows (can be more than one)
        final int ROWS = resultArray.length / COLUMN_NUMBER;
        for (int r = 0; r < ROWS; r++) {

            dataRows.append("<tr>");
            for (int i = COLUMN_NUMBER * r; i < COLUMN_NUMBER * (r + 1); i++) {
                if (resultArray[i] != null) {
                    dataRows.append("<td>").append(resultArray[i]).append(CELL_END);
                } else {
                    dataRows.append("<td style=\"color:silver;\">[NULL]</td>");
                }

            }
            dataRows.append(ROW_END).append("\n");
        }

        // Allure attachment
        var attachment = reportHtmlBuilder(columns, dataRows);

        createHtmlAllureAttachment("I see in table " + tableName, attachment);

        return attachment;
    }

    private static String reportHtmlBuilder(StringBuilder columns, StringBuilder dataRows) {

        return
                stringMapperV2("""
                                <html title="test title">
                                <head>
                                <style>
                                                    table {
                                                        width: 100%;
                                                        margin-bottom: 1rem;
                                                        color: #111314;
                                                        border-collapse: collapse;
                                                        font-size: 12;
                                                    }
                                                    table td, table th {
                                                        vertical-align: center;
                                                        text-align:center;
                                                        border: 1px solid #dee2e6;
                                                    }
                                                    tr:nth-of-type(even) {
                                                        background-color:#faf7eb;
                                                    }
                                                    tr:nth-of-type(odd) {
                                                        background-color:#f0faf4;
                                                    }
                                                    table th{
                                                        position: sticky;
                                                        top: 0;
                                                        background-color:#FBFCFC!important;
                                                    }
                                                    #stick-column {
                                                        position: sticky;
                                                        left: 0;
                                                        font-weight: bold;
                                                    }
                                </style>
                                </head>
                                <body>
                                <table>
                                <tr>
                                $${columns}</tr>
                                $${dataRows}</table>
                                </body>
                                </html>""",
                        Map.of(
                                "columns", columns,
                                "dataRows", dataRows
                        ));

    }

}
