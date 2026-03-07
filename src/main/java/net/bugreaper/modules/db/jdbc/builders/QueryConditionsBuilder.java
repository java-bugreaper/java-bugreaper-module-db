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
package net.bugreaper.modules.db.jdbc.builders;

import net.bugreaper.modules.db.DbTypes;
import net.bugreaper.modules.db.exceptions.QueryBuilderException;
import net.bugreaper.modules.db.logger.Log;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.bugreaper.core.assertions.JsonAsserts.assertValidJson;


public class QueryConditionsBuilder {

    private final String dbType;

    private static final List<String> basicConditions = Arrays.asList("=", "!=", "<>", ">", ">=", "<", "<=", "IS", "IS NOT", "LIKE");
    private static final String REGEX_CONDITION = "REGEX_MATCH";


    public QueryConditionsBuilder(String dbType) {
        this.dbType = dbType;
    }


    /**
     * Used for Oracle Date\DateTime select conditions
     *
     * @param conditionsArray array with conditions:
     *                        Oracle(Date\DateTime) {column, operator, date, enum}
     *                        <p>
     *                        Oracle:
     *                        <p>{"COLUMN NAME", ">= ", "2020-01-01", "DATE"} will build: COLUMN NAME" >= TO_DATE('2022-01-01’, 'YYYY-MM-DD')
     *                        <p>{"COLUMN NAME", "=", "2022-01-01 10:10:22", "TIMESTAMP"} will build: COLUMN NAME = TO_DATE('2022-01-01 10:10:22’, 'YYYY-MM-DD HH24:MI:SS')
     */
    protected StringBuilder arrayCondition4Mapper(Object[] conditionsArray) {

        if (Objects.equals(dbType, DbTypes.ORACLE_DB.value())) {
            return arrayOracle4Mapper(conditionsArray);
        } else {
            Log.LOGGER.error("Query build failed for db: {} \narray {}", dbType, conditionsArray);
            throw new QueryBuilderException("4 elements not supported for database: " + dbType);
        }
    }

    private StringBuilder arrayOracle4Mapper(Object[] conditionsArray) {

        final String oracleDateEnum = "DATE";
        final String oracleDateTimeEnum = "TIMESTAMP";

        StringBuilder conditions = new StringBuilder();
        conditions.append(conditionsArray[0]);

        if (Objects.equals(conditionsArray[2], oracleDateEnum)) {
            return conditions.append(" ").append(conditionsArray[1]).append(" TO_DATE('").append(conditionsArray[3]).append("', 'YYYY-MM-DD')");
        } else if (Objects.equals(conditionsArray[2], oracleDateTimeEnum)) {
            return conditions.append(" ").append(conditionsArray[1]).append(" TO_DATE('").append(conditionsArray[3]).append("', 'YYYY-MM-DD HH24:MI:SS')");
        } else {
            Log.LOGGER.error("Query build failed for db: {} \narray {}", dbType, conditionsArray);
            throw new QueryBuilderException("ERROR, wrong 4 elements option for Oracle mapper: <" + conditionsArray[2] + "> supported: " +
                    oracleDateEnum + " or " + oracleDateTimeEnum);
        }
    }


    protected StringBuilder arrayCondition3Mapper(Object[] conditionsArray) {

        StringBuilder conditions = new StringBuilder();

        // basic conditions
        if (basicConditions.contains(conditionsArray[1].toString().toUpperCase())) {
            conditions.append(conditionsArray[0]);
            if (conditionsArray[2] != null) {
                conditions.append(" ").append(conditionsArray[1]).append(" ").append(toSqlValue(conditionsArray[2]));
            } else {
                // for nullable value
                conditions.append(" ").append(conditionsArray[1]).append(" ").append("null");
            }
        }

        // specific conditions
        else {
            if (Objects.equals(dbType, DbTypes.MY_SQL.value()) || Objects.equals(dbType, DbTypes.MARIA_DB.value())) {
                return arrayMaria3Mapper(conditionsArray);
            } else {
                Log.LOGGER.error("Query build failed for db: {} \narray {}\n use basic: {}", dbType, conditionsArray, basicConditions);

                throw new QueryBuilderException(MessageFormat.format("ERROR, wrong 3 elements operator: <{0}> supported: {1}",
                        conditionsArray[1], basicConditions));
            }
        }

        return conditions;
    }

    private StringBuilder arrayMaria3Mapper(Object[] conditionsArray) {

        List<String> mariaJsonContains = Arrays.asList("JSON_CONTAINS", "JSON_EQUALS");
        StringBuilder conditions = new StringBuilder();

        //mysql not support JSON_EQUALS
        if (mariaJsonContains.get(1) == conditionsArray[1] && Objects.equals(dbType, DbTypes.MY_SQL.value())) {
            throw new QueryBuilderException(MessageFormat.format("ERROR, wrong 3 elements operator for mysql mapper: <{0}> supported: {1} or {2}, {3}",
                    conditionsArray[1], basicConditions, mariaJsonContains.get(0), REGEX_CONDITION));
        } else if (mariaJsonContains.contains(conditionsArray[1])) {

            // check JSON/JSONArray
            assertValidJson((String) conditionsArray[2]);

            return conditions
                    .append(conditionsArray[1]).append("(").append(conditionsArray[0]).append(", '").append(conditionsArray[2]).append("')");

        } else if (REGEX_CONDITION.equals(conditionsArray[1])) {
            //for REGEX conditions
            return conditions
                    .append(conditionsArray[0]).append(" RLIKE ").append("'").append(conditionsArray[2]).append("'");

        } else {

            if (Objects.equals(dbType, DbTypes.MY_SQL.value())) {
                //remove equals(not supported for mysql
                mariaJsonContains.remove(1);
            }

            Log.LOGGER.error("Query build failed for db: {} \narray {}\n use basic: {} or specific: {}, {}",
                    dbType, conditionsArray, basicConditions, mariaJsonContains, REGEX_CONDITION);

            throw new QueryBuilderException(MessageFormat.format("ERROR, wrong 3 elements operator for {0} mapper: <{1}> supported: {2} or {3}, {4}",
                    dbType, conditionsArray[1], basicConditions, mariaJsonContains, REGEX_CONDITION));
        }
    }

    protected StringBuilder arrayCondition2Mapper(Object[] conditionsArray) {

        StringBuilder conditions = new StringBuilder();
        if (conditionsArray[1] == null) {
            conditions.append(" IS null");
        } else {
            conditions.append("=").append(toSqlValue(conditionsArray[1]));
        }

        return conditions;
    }


    private static String toSqlValue(Object value) {

        // Otherwise use normal conversion
        if (value instanceof String) {
            return "'" + value + "'";
        }
        if (value instanceof Boolean bool) {
            return (Boolean.TRUE.equals(bool)) ? "true" : "false";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return "'" + value + "'";
    }

}
