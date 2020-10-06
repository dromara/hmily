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
import lombok.RequiredArgsConstructor;
import org.dromara.hmily.repository.spi.entity.HmilySQLTuple;
import org.dromara.hmily.repository.spi.entity.HmilyUndoInvocation;
import org.dromara.hmily.tac.sqlcompute.exception.SQLComputeException;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.complex.HmilyCommonExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.simple.HmilyLiteralExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.item.HmilyExpressionProjectionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.table.HmilySimpleTableSegment;
import org.dromara.hmily.tac.sqlparser.model.statement.dml.HmilyUpdateStatement;

import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hmily update SQL compute engine.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class HmilyUpdateSQLComputeEngine extends AbstractHmilySQLComputeEngine {
    
    private final HmilyUpdateStatement statement;
    
    @Override
    // TODO fixture undoInvocation for poc test
    public HmilyUndoInvocation generateImage(final Connection connection, final String sql) throws SQLComputeException {
        Map<String, Object> beforeImage = new LinkedHashMap<>();
        Map<String, Object> afterImage = new LinkedHashMap<>();
        HmilyUndoInvocation result = new HmilyUndoInvocation();
        if (sql.contains("order")) {
            beforeImage.put("status", 3);
            afterImage.put("number", sql.substring(sql.indexOf("'") + 1, sql.length() - 1));
            result.getTuples().add(new HmilySQLTuple("order", "update", beforeImage, afterImage));
        } else if (sql.contains("account")) {
            beforeImage.put("balance", 100);
            afterImage.put("user_id", 10000);
            result.getTuples().add(new HmilySQLTuple("account", "update", beforeImage, afterImage));
        } else {
            beforeImage.put("total_inventory", 100);
            afterImage.put("product_id", 1);
            result.getTuples().add(new HmilySQLTuple("inventory", "update", beforeImage, afterImage));
        }
        return result;
    }
    
    @Override
    Collection<ImageSQLUnit> generateQueryImageSQLs(final String sql) {
        // TODO generate image SQL group according to parsed statement
        Collection<ImageSQLUnit> result = new LinkedList<>();
        String tables = getTables(sql);
        String whereCondition = getWhereCondition(sql);
        statement.getTables().forEach(segment -> {
            String imageSQL = String.format("SELECT %s FROM %s %s", Joiner.on(",").join(getUndoItems(segment), getRedoItems(segment)), tables, whereCondition);
            result.add(new ImageSQLUnit(imageSQL, new LinkedList<>(), "update", sql.substring(segment.getStartIndex(), segment.getStopIndex())));
        });
        return result;
    }
    
    private String getUndoItems(final HmilySimpleTableSegment segment) {
        String result;
        if (segment.getAlias().isPresent()) {
            result = String.format("%s.*", segment.getAlias().get());
        } else if (segment.getOwner().isPresent()) {
            result = String.format("%s.%s.*", segment.getOwner(), segment.getTableName().getIdentifier().getValue());
        } else {
            result = String.format("%s.*", segment.getTableName().getIdentifier().getValue());
        }
        return result;
    }
    
    private List<String> getRedoItems(final HmilySimpleTableSegment tableSegment) {
        List<String> result = new LinkedList<>();
        // TODO filter the column which don't belong to current table
        statement.getSetAssignment().getAssignments().forEach(assignment -> {
            Object value;
            if (assignment.getValue() instanceof HmilyLiteralExpressionSegment) {
                value = ((HmilyLiteralExpressionSegment) assignment.getValue()).getLiterals();
            } else if (assignment.getValue() instanceof HmilyExpressionProjectionSegment) {
                value = ((HmilyExpressionProjectionSegment) assignment.getValue()).getText();
            } else {
                value = ((HmilyCommonExpressionSegment) assignment.getValue()).getText();
            }
            result.add(String.format("%s AS $after_image$%s", value, assignment.getColumn().getIdentifier().getValue()));
        });
        return result;
    }
    
    private String getTables(final String sql) {
        List<String> tables = statement.getTables().stream().map(segment -> sql.substring(segment.getStartIndex(), segment.getStopIndex())).collect(Collectors.toList());
        return Joiner.on(",").join(tables);
    }
    
    private String getWhereCondition(final String sql) {
        return statement.getWhere().map(segment -> sql.substring(segment.getStartIndex(), segment.getStopIndex()))
            .orElseThrow(() -> new SQLComputeException("DML SQL should contain where condition"));
    }
}
