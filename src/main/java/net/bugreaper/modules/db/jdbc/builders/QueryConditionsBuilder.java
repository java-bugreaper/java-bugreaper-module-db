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

import net.bugreaper.modules.db.enums.DbTypes;
import net.bugreaper.modules.db.exceptions.QueryBuilderException;
import net.bugreaper.modules.db.logger.Log;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;


public class QueryConditionsBuilder {

    protected final String dbTypeKey;

    protected static final List<String> BASIC_CONDITIONS = Arrays.asList("=", "!=", "<>", ">", ">=", "<", "<=", "IS", "IS NOT", "LIKE");

    public QueryConditionsBuilder(DbTypes dbType) {
        this.dbTypeKey = dbType.value();
    }

    protected StringBuilder arrayCondition4Mapper(Object[] conditionsArray) {
            Log.LOGGER.error("Query build failed for db: {} \narray {}", dbTypeKey, conditionsArray);
            throw new QueryBuilderException("4 elements not supported for database: " + dbTypeKey);
    }

    protected StringBuilder arrayCondition3Mapper(Object[] conditionsArray) {

        // basic conditions
        if (BASIC_CONDITIONS.contains(conditionsArray[1].toString().toUpperCase())) {
            return basicConditionBuild(conditionsArray);
        }

        // specific conditions not supported
        else {
            throw new QueryBuilderException(MessageFormat.format("ERROR, wrong 3 elements operator: <{0}> database <{1}> supported: {2}",
                    conditionsArray[1], dbTypeKey, BASIC_CONDITIONS));
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

    protected static StringBuilder basicConditionBuild(Object[] conditionsArray) {

        StringBuilder conditions = new StringBuilder();

        conditions.append(conditionsArray[0]);
        if (conditionsArray[2] != null) {
            conditions.append(" ").append(conditionsArray[1]).append(" ").append(toSqlValue(conditionsArray[2]));
        } else {
            // for nullable value
            conditions.append(" ").append(conditionsArray[1]).append(" ").append("null");
        }

        return conditions;
    }

    protected static String toSqlValue(Object value) {

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

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
