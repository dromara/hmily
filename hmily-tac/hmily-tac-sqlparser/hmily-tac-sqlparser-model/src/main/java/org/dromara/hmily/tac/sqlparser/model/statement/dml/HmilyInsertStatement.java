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

package org.dromara.hmily.tac.sqlparser.model.statement.dml;

import lombok.Getter;
import lombok.Setter;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.HmilyAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.HmilyInsertValuesSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.HmilySetAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.column.HmilyColumnSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.column.HmilyInsertColumnsSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.column.HmilyOnDuplicateKeyColumnsSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.table.HmilySimpleTableSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Insert statement.
 */
@Getter
@Setter
public final class HmilyInsertStatement extends HmilyDMLStatement {
    
    private HmilySimpleTableSegment table;
    
    private HmilyInsertColumnsSegment insertColumns;
    
    private HmilySetAssignmentSegment setAssignment;
    
    private HmilyOnDuplicateKeyColumnsSegment onDuplicateKeyColumns;
    
    private final Collection<HmilyInsertValuesSegment> values = new LinkedList<>();
    
    /**
     * Get insert columns segment.
     * 
     * @return insert columns segment
     */
    public Optional<HmilyInsertColumnsSegment> getInsertColumns() {
        return Optional.ofNullable(insertColumns);
    }
    
    /**
     * Get columns.
     * 
     * @return columns
     */
    public Collection<HmilyColumnSegment> getColumns() {
        return null == insertColumns ? Collections.emptyList() : insertColumns.getColumns();
    }
    
    /**
     * Get set assignment segment.
     * 
     * @return set assignment segment
     */
    public Optional<HmilySetAssignmentSegment> getSetAssignment() {
        return Optional.ofNullable(setAssignment);
    }
    
    /**
     * Get on duplicate key columns segment.
     *
     * @return on duplicate key columns segment
     */
    public Optional<HmilyOnDuplicateKeyColumnsSegment> getOnDuplicateKeyColumns() {
        return Optional.ofNullable(onDuplicateKeyColumns);
    }
    
    /**
     * Judge is use default columns or not.
     * 
     * @return is use default columns or not
     */
    public boolean useDefaultColumns() {
        return getColumns().isEmpty() && null == setAssignment;
    }
    
    /**
     * Get column names.
     *
     * @return column names
     */
    public List<String> getColumnNames() {
        return null == setAssignment ? getColumnNamesForInsertColumns() : getColumnNamesForSetAssignment();
    }
    
    private List<String> getColumnNamesForInsertColumns() {
        List<String> result = new LinkedList<>();
        for (HmilyColumnSegment each : getColumns()) {
            result.add(each.getIdentifier().getValue().toLowerCase());
        }
        return result;
    }
    
    private List<String> getColumnNamesForSetAssignment() {
        List<String> result = new LinkedList<>();
        for (HmilyAssignmentSegment each : setAssignment.getAssignments()) {
            result.add(each.getColumn().getIdentifier().getValue().toLowerCase());
        }
        return result;
    }
    
    /**
     * Get value list count.
     *
     * @return value list count
     */
    public int getValueListCount() {
        return null == setAssignment ? values.size() : 1;
    }
    
    /**
     * Get value count for per value list.
     * 
     * @return value count
     */
    public int getValueCountForPerGroup() {
        if (!values.isEmpty()) {
            return values.iterator().next().getValues().size();
        }
        if (null != setAssignment) {
            return setAssignment.getAssignments().size();
        }
        return 0;
    }
    
    /**
     * Get all value expressions.
     * 
     * @return all value expressions
     */
    public List<List<HmilyExpressionSegment>> getAllValueExpressions() {
        return null == setAssignment ? getAllValueExpressionsFromValues() : Collections.singletonList(getAllValueExpressionsFromSetAssignment());
    }
    
    private List<List<HmilyExpressionSegment>> getAllValueExpressionsFromValues() {
        List<List<HmilyExpressionSegment>> result = new ArrayList<>(values.size());
        for (HmilyInsertValuesSegment each : values) {
            result.add(each.getValues());
        }
        return result;
    }
    
    private List<HmilyExpressionSegment> getAllValueExpressionsFromSetAssignment() {
        List<HmilyExpressionSegment> result = new ArrayList<>(setAssignment.getAssignments().size());
        for (HmilyAssignmentSegment each : setAssignment.getAssignments()) {
            result.add(each.getValue());
        }
        return result;
    }
}
