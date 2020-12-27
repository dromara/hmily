/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hmily.tac.sqlcompute.impl;

import org.dromara.hmily.tac.sqlparser.model.segment.generic.table.HmilySimpleTableSegment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Hmily SQL utils.
 *
 * @author zhaojun
 */
public class HmilySQLComputeUtils {
    
    /**
     * Execute query.
     *
     * @param connection connection
     * @param sql sql
     * @param parameters parameters
     * @return records
     * @throws SQLException SQL exception
     */
    public static Collection<Map<String, Object>> executeQuery(final Connection connection, final String sql, final List<Object> parameters) throws SQLException {
        Collection<Map<String, Object>> result = new LinkedList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            for (Object each : parameters) {
                preparedStatement.setObject(parameterIndex, each);
                parameterIndex++;
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> record = new LinkedHashMap<>();
                for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
                    record.put(resultSetMetaData.getColumnLabel(columnIndex), resultSet.getObject(columnIndex));
                }
                result.add(record);
            }
        }
        return result;
    }
    
    /**
     * Get all columns.
     *
     * @param segment hmily simple table segment
     * @return all table columns in asterisk way
     */
    public static String getAllColumns(final HmilySimpleTableSegment segment) {
        String result;
        if (segment.getAlias().isPresent()) {
            result = String.format("%s.*", segment.getAlias().get());
        } else if (segment.getOwner().isPresent()) {
            result = String.format("%s.%s.*", segment.getOwner(), segment.getTableName().getIdentifier().toString());
        } else {
            result = String.format("%s.*", segment.getTableName().getIdentifier().toString());
        }
        return result;
    }
}
