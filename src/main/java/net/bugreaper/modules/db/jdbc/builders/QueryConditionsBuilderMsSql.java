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


public class QueryConditionsBuilderMsSql extends QueryConditionsBuilder {


    //JSON_EQUALS not supported (only by JSON_VALUE)

    public QueryConditionsBuilderMsSql(DbTypes dbType) {
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
            return arrayMsSql3Mapper(conditionsArray);
        }
    }


    private StringBuilder arrayMsSql3Mapper(Object[] conditionsArray) {

        StringBuilder conditions = new StringBuilder();

        if (REGEX_CONDITION.equals(conditionsArray[1])) {
            //for REGEX conditions
            return conditions
                    .append(("REGEXP_LIKE(%s, '%s')".formatted(conditionsArray[0], conditionsArray[2])));

        } else {

            Log.LOGGER.error("Query build failed for database: {} \nprovided array: {}\n supported operators basic: {} or specific: {}",
                    dbTypeKey, conditionsArray, BASIC_CONDITIONS, REGEX_CONDITION);

            throw new QueryBuilderException("ERROR: wrong 3 elements operator '%s' for database '%s' supported basic %s or specific %s"
                    .formatted(conditionsArray[1], dbTypeKey, BASIC_CONDITIONS, REGEX_CONDITION));
        }
    }
}
