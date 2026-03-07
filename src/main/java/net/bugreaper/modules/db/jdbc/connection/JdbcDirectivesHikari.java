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
package net.bugreaper.modules.db.jdbc.connection;

import net.bugreaper.modules.db.exceptions.SqlExecuteException;
import net.bugreaper.modules.db.logger.Log;
import net.bugreaper.modules.db.allurereporter.AllureReporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

public class JdbcDirectivesHikari {

    private ResultSet resultSet;
    DataSource dataSource;


    public JdbcDirectivesHikari(String dbUrl,
                                String username,
                                String passwd,
                                String dbType) {

        dataSource = new DataSource(dbUrl, username, passwd, dbType);

    }

    public List<Object> getSqlResultList(String query, String... columnName) {

        List<Object> columnResultList = new LinkedList<>();
        resultSet = null;

        tryExecuteLog(query);

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
            throwException(sqlException, query);
        }

        return columnResultList;
    }

    public String getSqlResultForCount(String query, String columnName) {

        resultSet = null;
        String columnResult = null;

        tryExecuteLog(query);

        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            resultSet = pst.executeQuery();
            while (resultSet.next()) {
                columnResult = resultSet.getString(columnName);
            }

        } catch (Exception sqlException) {
            throwException(sqlException, query);
        }
        return columnResult;
    }

    public void sqlNoAttachExecute(String query) {
        execute(query);
    }

    public void sqlExecute(String query) {
        AllureReporter.executeQueryReporter(query);
        execute(query);
    }

    private void execute(String query) {

        try (Connection con = dataSource.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            tryExecuteLog(query);
            pst.executeUpdate();

        } catch (Exception sqlException) {
            throwException(sqlException, query);
        }
    }

    private static void tryExecuteLog(String query) {
        Log.LOGGER.debug("Try to execute sql:\n {}", query);
    }

    private static void throwException(Exception sqlException, String query) {
        Log.LOGGER.error("Failed to execute:\n {}", query);
        throw new SqlExecuteException(sqlException);
    }

}
