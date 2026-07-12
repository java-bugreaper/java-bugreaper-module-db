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

import static net.bugreaper.core.assertions.JsonAsserts.assertValidJson;


public class QueryConditionsBuilderMysql extends QueryConditionsBuilder{

    private static final String REGEX_CONDITION = "REGEX_MATCH";
    private static final String JSON_CONDITIONS = "JSON_CONTAINS"; //mysql not support JSON_EQUALS

    public QueryConditionsBuilderMysql(DbTypes dbType) {
        super(dbType);
    }

    @Override
    protected StringBuilder arrayCondition3Mapper(Object[] conditionsArray) {

        // basic conditions
        if (BASIC_CONDITIONS.contains(conditionsArray[1].toString().toUpperCase())) {
            return basicConditionBuild(conditionsArray);
        }

        // specific conditions
        else {
            return arrayMySql3Mapper(conditionsArray);
        }
    }


    private StringBuilder arrayMySql3Mapper(Object[] conditionsArray) {

        StringBuilder conditions = new StringBuilder();

        if(JSON_CONDITIONS.equals(conditionsArray[1])) {

            // check JSON/JSONArray
            assertValidJson((String) conditionsArray[2]);

            return conditions
                    .append(conditionsArray[1]).append("(").append(conditionsArray[0]).append(", '").append(conditionsArray[2]).append("')");

        } else if (REGEX_CONDITION.equals(conditionsArray[1])) {
            //for REGEX conditions
            return conditions
                    .append(conditionsArray[0]).append(" RLIKE ").append("'").append(conditionsArray[2]).append("'");

        } else {

            Log.LOGGER.error("Query build failed for db: {} \narray {}\n use basic: {} or specific: {}, {}",
                    dbTypeKey, conditionsArray, BASIC_CONDITIONS, JSON_CONDITIONS, REGEX_CONDITION);

            throw new QueryBuilderException(MessageFormat.format("ERROR, wrong 3 elements operator for {0} mapper: <{1}> supported: {2} or {3}, {4}",
                    dbTypeKey, conditionsArray[1], BASIC_CONDITIONS, JSON_CONDITIONS, REGEX_CONDITION));
        }
    }

}
