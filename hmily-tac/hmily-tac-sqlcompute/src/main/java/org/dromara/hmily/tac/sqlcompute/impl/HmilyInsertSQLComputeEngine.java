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
import org.dromara.hmily.repository.spi.entity.HmilyDataSnapshot;
import org.dromara.hmily.repository.spi.entity.HmilySQLTuple;
import org.dromara.hmily.tac.sqlcompute.HmilySQLComputeEngine;
import org.dromara.hmily.tac.sqlcompute.exception.SQLComputeException;
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

/**
 * Hmily insert SQL compute engine.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class HmilyInsertSQLComputeEngine implements HmilySQLComputeEngine {
    
    private final HmilyInsertStatement sqlStatement;
    
    @Override
    public HmilyDataSnapshot generateSnapshot(final String sql, final List<Object> parameters, final Connection connection) throws SQLComputeException {
        HmilyDataSnapshot result = new HmilyDataSnapshot();
        result.getTuples().addAll(generateSQLTuples(sql, parameters));
        return result;
    }
    
    private Collection<HmilySQLTuple> generateSQLTuples(final String sql, final List<Object> parameters) {
        String tableName = sql.substring(sqlStatement.getTable().getStartIndex(), sqlStatement.getTable().getStopIndex());
        // TODO support onDuplicateKey
        return sqlStatement.getSetAssignment().isPresent()
            ? createTuplesBySet(tableName, parameters, sqlStatement.getSetAssignment().get()) : createTuplesByValues(tableName, parameters);
    }
    
    private Collection<HmilySQLTuple> createTuplesBySet(final String tableName, final List<Object> parameters, final HmilySetAssignmentSegment setAssignmentsSegment) {
        return Collections.singletonList(new HmilySQLTuple(tableName, "insert", new LinkedHashMap<>(), generateTupleData(parameters, setAssignmentsSegment)));
    }
    
    private Collection<HmilySQLTuple> createTuplesByValues(final String tableName, final List<Object> parameters) {
        Collection<HmilySQLTuple> result = new LinkedList<>();
        for (HmilyInsertValuesSegment each : sqlStatement.getValues()) {
            // TODO use tableMetadata to get default columns
            result.add(new HmilySQLTuple(tableName, "insert", new LinkedHashMap<>(), generateTupleData(parameters, sqlStatement.getColumnNames(), each)));
        }
        return result;
    }
    
    private Map<String, Object> generateTupleData(final List<Object> parameters, final HmilySetAssignmentSegment setAssignments) {
        Map<String, Object> result = new LinkedHashMap<>(setAssignments.getAssignments().size(), 1);
        for (HmilyAssignmentSegment each : setAssignments.getAssignments()) {
            result.put(each.getColumn().getQualifiedName(), ExpressionHandler.getValue(parameters, each.getValue()));
        }
        return result;
    }
    
    private Map<String, Object> generateTupleData(final List<Object> parameters, final Collection<String> columnNames, final HmilyInsertValuesSegment insertValues) {
        Map<String, Object> result = new LinkedHashMap<>(columnNames.size());
        Iterator<String> columnNameIterator = columnNames.iterator();
        for (HmilyExpressionSegment each : insertValues.getValues()) {
            result.put(columnNameIterator.next(), ExpressionHandler.getValue(parameters, each));
        }
        return result;
    }
}
