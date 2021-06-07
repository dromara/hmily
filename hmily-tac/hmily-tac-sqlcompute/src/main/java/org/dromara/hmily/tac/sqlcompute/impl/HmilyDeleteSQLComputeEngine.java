/*
 * Copyright 2017-2021 Dromara.org

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLManipulation;
import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLTuple;
import org.dromara.hmily.tac.metadata.HmilyMetaDataManager;
import org.dromara.hmily.tac.metadata.model.TableMetaData;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.table.HmilySimpleTableSegment;
import org.dromara.hmily.tac.sqlparser.model.dialect.mysql.dml.HmilyMySQLDeleteStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hmily delete SQL compute engine.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class HmilyDeleteSQLComputeEngine extends AbstractHmilySQLComputeEngine {
    
    private final HmilyMySQLDeleteStatement sqlStatement;
    
    @Override
    Collection<HmilySQLTuple> createTuples(final String sql, final List<Object> parameters, final Connection connection, final String resourceId) throws SQLException {
        Collection<HmilySQLTuple> result = new LinkedList<>();
        HmilySimpleTableSegment tableSegment = (HmilySimpleTableSegment) sqlStatement.getTableSegment();
        String tableName = sql.substring(tableSegment.getStartIndex(), tableSegment.getStopIndex());
        String selectSQL = String.format("SELECT %s FROM %s %s", HmilySQLComputeUtils.getAllColumns(tableSegment, tableName), tableName, getWhereCondition(sql));
        Collection<Map<String, Object>> records = HmilySQLComputeUtils.executeQuery(connection, selectSQL, parameters);
        result.addAll(doConvert(records, HmilyMetaDataManager.get(resourceId).getTableMetaDataMap().get(tableName)));
        return result;
    }
    
    private String getWhereCondition(final String sql) {
        return sqlStatement.getWhere().map(segment -> sql.substring(segment.getStartIndex(), segment.getStopIndex())).orElse("");
    }
    
    private Collection<HmilySQLTuple> doConvert(final Collection<Map<String, Object>> records, final TableMetaData tableMetaData) {
        Collection<HmilySQLTuple> result = new LinkedList<>();
        for (Map<String, Object> record : records) {
            List<Object> primaryKeyValues = tableMetaData.getPrimaryKeyColumns().stream().map(record::get).collect(Collectors.toList());
            result.add(buildTuple(tableMetaData.getTableName(), HmilySQLManipulation.DELETE, primaryKeyValues, record, new LinkedHashMap<>()));
        }
        return result;
    }
}
