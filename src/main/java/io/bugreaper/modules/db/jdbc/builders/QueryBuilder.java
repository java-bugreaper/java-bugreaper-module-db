/*
 *  Copyright 2025 Oleksii Betin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.bugreaper.modules.db.jdbc.builders;

import io.bugreaper.modules.db.exceptions.QueryBuilderException;
import io.bugreaper.modules.db.logger.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import static io.bugreaper.modules.db.allurereporter.AllureReporter.attachTableForDb;

/**
 * Class with builders
 * from arrays (Object[][]) to sql scripts
 */
public class QueryBuilder extends QueryConditionsBuilder {



    public QueryBuilder(String dbType) {
        super(dbType);
        Log.LOGGER.debug("Database type: {}", dbType);
    }

    /**
     * Create sql script from array (Object[][])
     * for conditions
     *
     * @param setArray array with conditions: {condition array: {column1, value1 }...}
     * @return condition part: " SET column1 =\is value1 AND ..."
     */
    public String sqlSetBuilder(Object[][] setArray) {

        final int COUNT = setArray.length;

        StringBuilder conditions = new StringBuilder();
        if (COUNT != 0) {
            conditions.append("SET ");

            for (int i = 0; i < COUNT; i++) {

                // 2 elements, with basic operators
                if (setArray[i].length == 2) {
                    conditions.append(setArray[i][0]);
                    conditions.append(arrayCondition2Mapper(setArray[i]));
                } else {
                    throw new QueryBuilderException("ERROR, array length not valid: " + Arrays.toString(setArray[i]));
                }

                //for not last condition
                if (i != COUNT - 1) {
                    conditions.append(", ");
                }

            }
        }
        return conditions.toString();
    }

    /**
     * Create sql script from array (Object[][])
     * for conditions
     *
     * @param conditionsArray array with conditions: {condition array: {column1, value1 }...}
     * @return condition part: " where column1 =\is value1, ..."
     */
    public String sqlConditionBuilder(Object[][] conditionsArray) {

        final int COUNT = conditionsArray.length;

        StringBuilder conditions = new StringBuilder();
        if (COUNT != 0) {
            conditions.append("where ");

            for (int i = 0; i < COUNT; i++) {

                // add column name


                // 2 elements, with basic operators
                if (conditionsArray[i].length == 2) {
                    conditions.append(conditionsArray[i][0]);
                    conditions.append(arrayCondition2Mapper(conditionsArray[i]));

                    // 3 elements, with operator
                } else if (conditionsArray[i].length == 3) {
                    conditions.append(arrayCondition3Mapper(conditionsArray[i]));

                    // 4 elements, Oracle DATE conditions
                } else if (conditionsArray[i].length == 4) {
                    conditions.append(arrayCondition4Mapper(conditionsArray[i]));
                } else {
                    throw new QueryBuilderException("ERROR, array length not valid: " + Arrays.toString(conditionsArray[i]));
                }

                //for not last condition
                if (i != COUNT - 1) {
                    conditions.append(" AND ");
                }

            }
        }
        return conditions.toString();
    }

    /**
     * Create sql script from arrays
     * for Insert
     *
     * @param tableName     table name with schema (SCHEMA.TABLE1)
     * @param templateArray array with table template {{column1, value1 }...}
     * @param providedArray     array with data from test case {{column1, test_value1 }...}
     * @return String with sql script (INSERT INTO SCHEMA.TABLE1 (column1,...) VALUES (value1,...))
     */
    public String sqlInsertBuilder(String tableName, Object[][] templateArray, Object[][] providedArray) {

        // template mapping
        final Object[][] valuesArray = templateMapper(templateArray, providedArray);
        attachTableForDb("Inserted in table " + tableName, valuesArray);

        StringJoiner columns = new StringJoiner(", ", "(", ")");
        StringJoiner values = new StringJoiner(", ", "(", ")");

        for (Object[] entry : valuesArray) {
            String column = (String) entry[0];
            Object value = entry[1];
            String type = entry.length > 2 ? (String) entry[2] : null;

            columns.add(column);
            values.add(toSqlValue(value, type));
        }

        return String.format("""
                INSERT INTO %s
                %s
                VALUES %s""", tableName, columns, values);

    }

    private static String toSqlValue(Object value, String type) {
        if (value == null) {
            return "null";
        }

        // If 3rd element exists
        if (type != null) {
            return type + " '" + escapeSql(String.valueOf(value)) + "'";
        }

        // Otherwise use normal conversion
        if (value instanceof String string) {
            return "'" + escapeSql(string) + "'";
        }
        if (value instanceof Boolean bool) {
            return (Boolean.TRUE.equals(bool)) ? "true" : "false";
        }
        if (value instanceof Number) {
            return value.toString();
        }

        return "'" + escapeSql(value.toString()) + "'";
    }

    private static String escapeSql(String s) {
        return s.replace("'", "''");
    }

    /**
     * @param templateArray array with table template {{column1, value1 }...}
     * @param providedArray     array with data from test case {{column1, test_value1 }...}
     * @return Object[][] with merged array (substitutes values from providedArray to templateArray)
     */
    private Object[][] templateMapper(Object[][] templateArray, Object[][] providedArray) {

        final int COLUMNS_COUNT = templateArray.length;

        // Create list of all columns (from template)
        String[] templateColumnsList = new String[COLUMNS_COUNT];
        for (int i = 0; i < COLUMNS_COUNT; i++) {
            templateColumnsList[i] = templateArray[i][0].toString();
        }

        List<String> templateCheck = new ArrayList<>(Arrays.asList(templateColumnsList));


        // Create lists of columns and values from test data
        String[] testColumnsList = new String[providedArray.length];
        Object[] testValuesList = new Object[providedArray.length];
        for (int i = 0; i < providedArray.length; i++) {

            var column = providedArray[i][0];
            if (templateCheck.contains(column)) {
                testColumnsList[i] = column.toString();
                testValuesList[i] = providedArray[i][1];
                //  Check test column in template column list
            } else {
                throw new QueryBuilderException("ERROR, column not exist in template: " + column);
            }
        }

        //  Duplicate checks
        checkForDuplicates(testColumnsList);

        //  Create list of test columns
        List<String> testCheck = new ArrayList<>(Arrays.asList(testColumnsList));

        //  Create array for insert
        Object[][] newArray = new Object[COLUMNS_COUNT][];

        for (int i = 0; i < COLUMNS_COUNT; i++) {
            // Data from test data
            if (testCheck.contains(templateColumnsList[i])) {
                int index = testCheck.indexOf(templateColumnsList[i]);

                // Part for three elements (ORACLE params)
                Object[] add;
                if (templateArray[i].length > 2) {
                    add = new Object[]{testColumnsList[index], testValuesList[index], templateArray[i][2]};
                } else {
                    add = new Object[]{testColumnsList[index], testValuesList[index]};
                }
                newArray[i] = add;

                //  Data from template
            } else {
                newArray[i] = templateArray[i];
                newArray[i][0] = templateArray[i][0];
            }

        }
        return newArray;
    }

    private void checkForDuplicates(Object[] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (array[i].equals(array[j])) {
                    throw new QueryBuilderException("ERROR, duplicate column found: " + array[i]);
                }
            }
        }
    }

}
