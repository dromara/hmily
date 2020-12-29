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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLManipulation;
import org.dromara.hmily.repository.spi.entity.tuple.HmilySQLTuple;
import org.dromara.hmily.tac.metadata.HmilyMetaDataManager;
import org.dromara.hmily.tac.metadata.model.TableMetaData;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.simple.HmilyParameterMarkerExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.HmilyAndPredicate;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.HmilyPredicateSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.value.HmilyPredicateBetweenRightValue;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.value.HmilyPredicateCompareRightValue;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.value.HmilyPredicateInRightValue;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.value.HmilyPredicateRightValue;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.table.HmilySimpleTableSegment;
import org.dromara.hmily.tac.sqlparser.model.statement.dml.HmilyUpdateStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Hmily update SQL compute engine.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class HmilyUpdateSQLComputeEngine extends AbstractHmilySQLComputeEngine {
    
    private static final String AFTER_IMAGE_COLUMN_SUFFIX = "_v2";
    
    private final HmilyUpdateStatement sqlStatement;
    
    @Override
    Collection<HmilySQLTuple> createTuples(final String sql, final List<Object> parameters, final Connection connection, final String resourceId) throws SQLException {
        Collection<HmilySQLTuple> result = new LinkedList<>();
        Preconditions.checkState(sqlStatement.getTables().size() == 1, "Do not support multiple tables in update statement");
        HmilySimpleTableSegment tableSegment = sqlStatement.getTables().iterator().next();
        String tableName = sql.substring(tableSegment.getStartIndex(), tableSegment.getStopIndex() + 1);
        String selectSQL = String.format("SELECT %s FROM %s %s", Joiner.on(", ").join(getSelectItems(parameters, tableSegment)), tableName, getWhereCondition(sql));
        Collection<Map<String, Object>> records = HmilySQLComputeUtils.executeQuery(connection, selectSQL, getWhereParameters(parameters));
        result.addAll(doConvert(records, HmilyMetaDataManager.get(resourceId).getTableMetaDataMap().get(tableSegment.getTableName().getIdentifier().getValue())));
        return result;
    }
    
    private List<String> getSelectItems(final List<Object> parameters, final HmilySimpleTableSegment tableSegment) {
        List<String> result = new LinkedList<>();
        result.add(HmilySQLComputeUtils.getAllColumns(tableSegment));
        sqlStatement.getSetAssignment().getAssignments().forEach(assignment -> result.add(
            String.format("%s AS %s", ExpressionHandler.getValue(parameters, assignment.getValue()), assignment.getColumn().getIdentifier().getValue() + AFTER_IMAGE_COLUMN_SUFFIX)));
        return result;
    }
    
    private String getWhereCondition(final String sql) {
        return sqlStatement.getWhere().map(segment -> sql.substring(segment.getStartIndex(), segment.getStopIndex() + 1)).orElse("");
    }
    
    private List<Object> getWhereParameters(final List<Object> parameters) {
        List<Object> result = new LinkedList<>();
        sqlStatement.getWhere().ifPresent(whereSegment -> {
            for (HmilyAndPredicate predicate : whereSegment.getHmilyAndPredicates()) {
                result.addAll(getPredicateSegmentParameters(parameters, predicate));
            }
        });
        return result;
    }
    
    private List<Object> getPredicateSegmentParameters(final List<Object> parameters, final HmilyAndPredicate predicate) {
        List<Object> result = new LinkedList<>();
        for (HmilyPredicateSegment segment : predicate.getPredicates()) {
            HmilyPredicateRightValue rightValue = segment.getRightValue();
            if (rightValue instanceof HmilyPredicateBetweenRightValue) {
                result.addAll(getPredicateBetweenRightValueParameters(parameters, (HmilyPredicateBetweenRightValue) rightValue));
            } else if (rightValue instanceof HmilyPredicateCompareRightValue) {
                Optional<Object> parameter = getExpressionParameter(parameters, ((HmilyPredicateCompareRightValue) rightValue).getExpression());
                parameter.ifPresent(result::add);
            } else if (rightValue instanceof HmilyPredicateInRightValue) {
                result.addAll(getPredicateInRightValue(parameters, (HmilyPredicateInRightValue) rightValue));
            }
        }
        return result;
    }
    
    private List<Object> getPredicateBetweenRightValueParameters(final List<Object> parameters, final HmilyPredicateBetweenRightValue predicateRightValue) {
        List<Object> result = new LinkedList<>();
        Optional<Object> andParameter = getExpressionParameter(parameters, predicateRightValue.getAndExpression());
        Optional<Object> betweenParameter = getExpressionParameter(parameters, predicateRightValue.getBetweenExpression());
        andParameter.ifPresent(result::add);
        betweenParameter.ifPresent(result::add);
        return result;
    }
    
    private List<Object> getPredicateInRightValue(final List<Object> parameters, final HmilyPredicateInRightValue predicateInRightValue) {
        List<Object> result = new LinkedList<>();
        for (HmilyExpressionSegment expressionSegment : predicateInRightValue.getSqlExpressions()) {
            Optional<Object> parameter = getExpressionParameter(parameters, expressionSegment);
            parameter.ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<Object> getExpressionParameter(final List<Object> parameters, final HmilyExpressionSegment expressionSegment) {
        if (expressionSegment instanceof HmilyParameterMarkerExpressionSegment) {
            return Optional.of(parameters.get(((HmilyParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex()));
        }
        return Optional.empty();
    }
    
    private Collection<HmilySQLTuple> doConvert(final Collection<Map<String, Object>> records, final TableMetaData tableMetaData) {
        Collection<HmilySQLTuple> result = new LinkedList<>();
        for (Map<String, Object> record : records) {
            Map<String, Object> before = new LinkedHashMap<>();
            Map<String, Object> after = new LinkedHashMap<>();
            record.forEach((key, value) -> {
                if (!key.contains(AFTER_IMAGE_COLUMN_SUFFIX)) {
                    before.put(key, value);
                } else {
                    // TODO skip date column here
                    after.put(key.replace(AFTER_IMAGE_COLUMN_SUFFIX, ""), value);
                }
            });
            List<Object> primaryKeyValues = tableMetaData.getPrimaryKeyColumns().stream().map(before::get).collect(Collectors.toList());
            result.add(new HmilySQLTuple(tableMetaData.getTableName(), HmilySQLManipulation.UPDATE, primaryKeyValues, before, after));
        }
        return result;
    }
}
