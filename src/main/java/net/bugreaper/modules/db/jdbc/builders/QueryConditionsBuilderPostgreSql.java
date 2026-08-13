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


public class QueryConditionsBuilderPostgreSql extends QueryConditionsBuilder {

    public QueryConditionsBuilderPostgreSql(DbTypes dbType) {
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

        if (JSON_CONTAINS.equals(conditionsArray[1])) {

            // check JSON/JSONArray
            assertValidJson((String) conditionsArray[2]);

            return conditions
                    .append("%s::jsonb @> '%s'::jsonb".formatted(conditionsArray[0], conditionsArray[2]));


        } else if (JSON_EQUALS.equals(conditionsArray[1])) {

            assertValidJson((String) conditionsArray[2]);

            return conditions // like varchar but the key order does not matter
                    .append("%s::jsonb = '%s'::jsonb".formatted(conditionsArray[0], conditionsArray[2]));


        } else if (REGEX_CONDITION.equals(conditionsArray[1])) {
            //for REGEX conditions
            return conditions
                    .append(("%s ~ '%s'".formatted(conditionsArray[0], conditionsArray[2])));

        } else {
            throw error3Elements(conditionsArray);
        }
    }

}
