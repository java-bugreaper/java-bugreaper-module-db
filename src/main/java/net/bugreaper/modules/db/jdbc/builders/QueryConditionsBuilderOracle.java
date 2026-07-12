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

import java.util.Objects;


public class QueryConditionsBuilderOracle extends QueryConditionsBuilder{


    public QueryConditionsBuilderOracle(DbTypes dbType) {
        super(dbType);
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
     * @return StringBuilder with condition part of query
     */
    @Override
    protected StringBuilder arrayCondition4Mapper(Object[] conditionsArray) {
        final String oracleDateEnum = "DATE";
        final String oracleDateTimeEnum = "TIMESTAMP";

        StringBuilder conditions = new StringBuilder();
        conditions.append(conditionsArray[0]);

        if (Objects.equals(conditionsArray[2], oracleDateEnum)) {
            return conditions.append(" ").append(conditionsArray[1]).append(" TO_DATE('").append(conditionsArray[3]).append("', 'YYYY-MM-DD')");
        } else if (Objects.equals(conditionsArray[2], oracleDateTimeEnum)) {
            return conditions.append(" ").append(conditionsArray[1]).append(" TO_DATE('").append(conditionsArray[3]).append("', 'YYYY-MM-DD HH24:MI:SS')");
        } else {
            Log.LOGGER.error("Query build failed for db: {} \narray {}", dbTypeKey, conditionsArray);
            throw new QueryBuilderException("ERROR, wrong 4 elements option for Oracle mapper: <" + conditionsArray[2] + "> supported: " +
                    oracleDateEnum + " or " + oracleDateTimeEnum);
        }
    }
}
