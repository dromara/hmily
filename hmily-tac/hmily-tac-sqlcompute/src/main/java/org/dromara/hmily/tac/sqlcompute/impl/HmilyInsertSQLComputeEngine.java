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
import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLManipulation;
import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLTuple;
import org.dromara.hmily.tac.metadata.HmilyMetaDataManager;
import org.dromara.hmily.tac.metadata.model.TableMetaData;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.HmilyAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.HmilyInsertValuesSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.HmilySetAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.statement.dml.HmilyInsertStatement;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hmily insert SQL compute engine.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class HmilyInsertSQLComputeEngine extends AbstractHmilySQLComputeEngine {
    
    private final HmilyInsertStatement sqlStatement;
    
    @Override
    Collection<HmilySQLTuple> createTuples(final String sql, final List<Object> parameters, final Connection connection, final String resourceId) {
        String tableName = sql.substring(sqlStatement.getTable().getStartIndex(), sqlStatement.getTable().getStopIndex());
        TableMetaData tableMetaData = HmilyMetaDataManager.get(resourceId).getTableMetaDataMap().get(tableName);
        return sqlStatement.getSetAssignment().isPresent()
            ? createTuplesBySet(parameters, sqlStatement.getSetAssignment().get(), tableMetaData) : createTuplesByValues(parameters, tableMetaData);
    }
    
    private Collection<HmilySQLTuple> createTuplesBySet(final List<Object> parameters, final HmilySetAssignmentSegment setAssignmentsSegment, final TableMetaData tableMetaData) {
        return doConvert(Collections.singletonList(getRecord(parameters, setAssignmentsSegment)), tableMetaData);
    }
    
    private Collection<HmilySQLTuple> createTuplesByValues(final List<Object> parameters, final TableMetaData tableMetaData) {
        Collection<Map<String, Object>> records = new LinkedList<>();
        for (HmilyInsertValuesSegment each : sqlStatement.getValues()) {
            List<String> columnNames = sqlStatement.getColumnNames().isEmpty() ? new LinkedList<>(tableMetaData.getColumns().keySet()) : sqlStatement.getColumnNames();
            records.add(getRecord(parameters, columnNames, each));
        }
        return doConvert(records, tableMetaData);
    }
    
    private Map<String, Object> getRecord(final List<Object> parameters, final HmilySetAssignmentSegment setAssignments) {
        Map<String, Object> result = new LinkedHashMap<>(setAssignments.getAssignments().size(), 1);
        for (HmilyAssignmentSegment each : setAssignments.getAssignments()) {
            result.put(each.getColumn().getQualifiedName(), ExpressionHandler.getValue(parameters, each.getValue()));
        }
        return result;
    }
    
    private Map<String, Object> getRecord(final List<Object> parameters, final Collection<String> columnNames, final HmilyInsertValuesSegment insertValues) {
        Map<String, Object> result = new LinkedHashMap<>(columnNames.size());
        Iterator<String> columnNameIterator = columnNames.iterator();
        for (HmilyExpressionSegment each : insertValues.getValues()) {
            result.put(columnNameIterator.next(), ExpressionHandler.getValue(parameters, each));
        }
        return result;
    }
    
    private Collection<HmilySQLTuple> doConvert(final Collection<Map<String, Object>> records, final TableMetaData tableMetaData) {
        Collection<HmilySQLTuple> result = new LinkedList<>();
        for (Map<String, Object> record : records) {
            List<Object> primaryKeyValues = tableMetaData.getPrimaryKeyColumns().stream().map(record::get).collect(Collectors.toList());
            result.add(new HmilySQLTuple(tableMetaData.getTableName(), HmilySQLManipulation.INSERT, primaryKeyValues, new LinkedHashMap<>(), record));
        }
        return result;
    }
}
