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
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.AssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.InsertValuesSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.SetAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.column.ColumnSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.column.InsertColumnsSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.ExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.table.SimpleTableSegment;

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
public final class InsertStatement extends DMLStatement {
    
    private SimpleTableSegment table;
    
    private InsertColumnsSegment insertColumns;
    
    private SetAssignmentSegment setAssignment;
    
    private OnDuplicateKeyColumnsSegment onDuplicateKeyColumns;
    
    private final Collection<InsertValuesSegment> values = new LinkedList<>();
    
    /**
     * Get insert columns segment.
     * 
     * @return insert columns segment
     */
    public Optional<InsertColumnsSegment> getInsertColumns() {
        return Optional.ofNullable(insertColumns);
    }
    
    /**
     * Get columns.
     * 
     * @return columns
     */
    public Collection<ColumnSegment> getColumns() {
        return null == insertColumns ? Collections.emptyList() : insertColumns.getColumns();
    }
    
    /**
     * Get set assignment segment.
     * 
     * @return set assignment segment
     */
    public Optional<SetAssignmentSegment> getSetAssignment() {
        return Optional.ofNullable(setAssignment);
    }
    
    /**
     * Get on duplicate key columns segment.
     *
     * @return on duplicate key columns segment
     */
    public Optional<OnDuplicateKeyColumnsSegment> getOnDuplicateKeyColumns() {
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
        for (ColumnSegment each : getColumns()) {
            result.add(each.getIdentifier().getValue().toLowerCase());
        }
        return result;
    }
    
    private List<String> getColumnNamesForSetAssignment() {
        List<String> result = new LinkedList<>();
        for (AssignmentSegment each : setAssignment.getAssignments()) {
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
    public List<List<ExpressionSegment>> getAllValueExpressions() {
        return null == setAssignment ? getAllValueExpressionsFromValues() : Collections.singletonList(getAllValueExpressionsFromSetAssignment());
    }
    
    private List<List<ExpressionSegment>> getAllValueExpressionsFromValues() {
        List<List<ExpressionSegment>> result = new ArrayList<>(values.size());
        for (InsertValuesSegment each : values) {
            result.add(each.getValues());
        }
        return result;
    }
    
    private List<ExpressionSegment> getAllValueExpressionsFromSetAssignment() {
        List<ExpressionSegment> result = new ArrayList<>(setAssignment.getAssignments().size());
        for (AssignmentSegment each : setAssignment.getAssignments()) {
            result.add(each.getValue());
        }
        return result;
    }
}
