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

import lombok.RequiredArgsConstructor;
import org.dromara.hmily.repository.spi.entity.HmilySQLTuple;
import org.dromara.hmily.repository.spi.entity.HmilyUndoInvocation;
import org.dromara.hmily.tac.sqlcompute.HmilySQLComputeEngine;
import org.dromara.hmily.tac.sqlcompute.exception.SQLComputeException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Abstract hmily SQL compute engine.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public abstract class AbstractHmilySQLComputeEngine implements HmilySQLComputeEngine {
    
    @Override
    public HmilyUndoInvocation generateImage(final Connection connection, final String sql) throws SQLComputeException {
        HmilyUndoInvocation result = new HmilyUndoInvocation();
        try {
            result.getTuples().addAll(generateSQLTuples(connection, sql));
        } catch (final SQLException ex) {
            throw new SQLComputeException(ex);
        }
        return result;
    }
    
    private Collection<HmilySQLTuple> generateSQLTuples(final Connection connection, final String originalSQL) throws SQLException {
        Collection<HmilySQLTuple> result = new LinkedList<>();
        for (ImageSQLUnit each : generateQueryImageSQLs(originalSQL)) {
            Collection<Map<String, Object>> data = doQueryImage(connection, each.getSql());
            result.addAll(doGenerateSQLTuples(data, each.getTableName(), each.getManipulationType()));
        }
        return result;
    }
    
    /**
     * Abstract method for generating query image SQLs.
     *
     * @param originalSQL original SQL
     * @return list of image SQL unit
     */
    abstract Collection<ImageSQLUnit> generateQueryImageSQLs(String originalSQL);
    
    private Collection<Map<String, Object>> doQueryImage(final Connection connection, final String sql) throws SQLException {
        Collection<Map<String, Object>> result = new LinkedList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            while (resultSet.next()) {
                Map<String, Object> record = new LinkedHashMap<>();
                for (int index = 1; index <= resultSetMetaData.getColumnCount(); index++) {
                    record.put(resultSetMetaData.getColumnLabel(index), resultSet.getObject(index));
                    result.add(record);
                }
            }
        }
        return result;
    }
    
    private Collection<HmilySQLTuple> doGenerateSQLTuples(final Collection<Map<String, Object>> data, final String tableName, final String manipulateType) {
        Collection<HmilySQLTuple> result = new LinkedList<>();
        for (Map<String, Object> each : data) {
            Map<String, Object> beforeImage = new LinkedHashMap<>();
            Map<String, Object> afterImage = new LinkedHashMap<>();
            each.forEach((key, value) -> {
                // FIXME $after_image$ is a marker for redo data item
                if (key.contains("$after_image$")) {
                    afterImage.put(key.replace("$after_image$", ""), value);
                } else {
                    beforeImage.put(key, value);
                }
            });
            beforeImage.forEach(afterImage::putIfAbsent);
            result.add(new HmilySQLTuple(tableName, manipulateType, beforeImage, afterImage));
        }
        return result;
    }
}
