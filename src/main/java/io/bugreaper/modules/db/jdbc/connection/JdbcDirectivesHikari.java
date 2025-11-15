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
package io.bugreaper.modules.db.jdbc.connection;

import io.bugreaper.modules.db.exceptions.SqlExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import static io.bugreaper.modules.db.allurereporter.AllureReporter.executeQueryReporter;

public class JdbcDirectivesHikari {


    private static final String TRY_EXECUTE = "Try to execute sql:\n {}";
    private static final String FAILED_EXECUTE = "Failed to execute:\n {}";
    private ResultSet resultSet;
    DataSource dataSource;

    private static final Logger LOGGER = LoggerFactory.getLogger("bugreaper-module-db");


    public JdbcDirectivesHikari(String dbUrl,
                                String username,
                                String passwd) {

        dataSource = new DataSource(dbUrl, username, passwd);

    }

    public List<Object> getSqlResultList(String query, String... columnName) {

        List<Object> columnResultList = new LinkedList<>();
        resultSet = null;

        LOGGER.debug(TRY_EXECUTE, query);

        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            resultSet = pst.executeQuery();

            while (resultSet.next()) {
                for (String i : columnName) {

                    Object column = resultSet.getObject(i);
                    columnResultList.add(column);
                }
            }

        } catch (Exception sqlException) {
            LOGGER.error(FAILED_EXECUTE, query);
            throw new SqlExecuteException(sqlException);
        }

        return columnResultList;
    }

    public String getSqlResultForCount(String query, String columnName) {

        resultSet = null;
        String columnResult = null;

        LOGGER.debug(TRY_EXECUTE, query);

        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            resultSet = pst.executeQuery();
            while (resultSet.next()) {
                columnResult = resultSet.getString(columnName);
            }

        } catch (Exception sqlException) {
            LOGGER.error(FAILED_EXECUTE, query);
            throw new SqlExecuteException(sqlException);
        }
        return columnResult;
    }

    public void sqlNoAttachExecute(String query) {
        execute(query);
    }

    public void sqlExecute(String query) {
        executeQueryReporter(query);
        execute(query);
    }

    private void execute(String query) {

        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            LOGGER.debug(TRY_EXECUTE, query);
            pst.executeUpdate();

        } catch (Exception sqlException) {
            LOGGER.error(FAILED_EXECUTE, query);
            throw new SqlExecuteException(sqlException);
        }
    }

}
