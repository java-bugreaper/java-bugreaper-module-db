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

import java.util.*;

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

        // If 3rd element exists (oracleDb) => DATE '3033-03-33'
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

        // Collect template keys
        Set<Object> templateKeys = new HashSet<>();
        for (Object[] row : templateArray) {
            templateKeys.add(row[0]);
        }

        // Validate provided columns (existence + uniqueness)
        Map<Object, Object[]> providedMap = new HashMap<>();
        for (Object[] row : providedArray) {
            Object columns = row[0];

            if (!templateKeys.contains(columns)) {
                throw new QueryBuilderException("ERROR, column not exist in template: " + columns);
            }

            if (providedMap.containsKey(columns)) {
                throw new QueryBuilderException("ERROR, duplicate column found: " + columns);
            }

            providedMap.put(columns, row);
        }

        // Merge
        Object[][] result = new Object[templateArray.length][];

        for (int i = 0; i < templateArray.length; i++) {
            Object[] templateRow = templateArray[i];
            Object[] mergedRow = Arrays.copyOf(templateRow, templateRow.length);

            Object[] providedRow = providedMap.get(templateRow[0]);
            if (providedRow != null) {
                for (int j = 1; j < mergedRow.length && j < providedRow.length; j++) {
                    // overwrite EVEN if provided value is null
                    mergedRow[j] = providedRow[j];
                }
            }

            result[i] = mergedRow;
        }

        return result;
    }

}
