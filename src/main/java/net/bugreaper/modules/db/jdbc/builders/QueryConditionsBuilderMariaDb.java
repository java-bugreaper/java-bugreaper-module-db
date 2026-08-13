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


import static net.bugreaper.core.assertions.JsonAsserts.assertValidJson;


public class QueryConditionsBuilderMariaDb extends QueryConditionsBuilder {

    public QueryConditionsBuilderMariaDb(DbTypes dbType) {
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
            return arrayMaria3Mapper(conditionsArray);
        }
    }


    private StringBuilder arrayMaria3Mapper(Object[] conditionsArray) {

        StringBuilder conditions = new StringBuilder();

        if (JSON_EQUALS.equals(conditionsArray[1]) || JSON_CONTAINS.equals(conditionsArray[1])) {

            // check JSON/JSONArray
            assertValidJson((String) conditionsArray[2]);

            return conditions
                    .append(conditionsArray[1]).append("(").append(conditionsArray[0]).append(", '").append(conditionsArray[2]).append("')");

        } else if (REGEX_CONDITION.equals(conditionsArray[1])) {
            //for REGEX conditions
            return conditions
                    .append(conditionsArray[0]).append(" RLIKE ").append("'").append(conditionsArray[2]).append("'");

        } else {
            throw error3Elements(conditionsArray);
        }

    }

}
