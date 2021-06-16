/*
 * Copyright 2017-2021 Dromara.org
 *
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

package org.dromara.hmily.tac.sqlparser.shardingsphere.common.handler;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.assignment.HmilyInsertValuesSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.column.HmilyColumnSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.column.HmilyInsertColumnsSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.table.HmilySimpleTableSegment;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyInsertStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class InsertStatementAssembler {
    
    /**
     * Assemble Hmily insert statement.
     *
     * @param insertStatement insert statement
     * @param hmilyInsertStatement hmily insert statement
     * @return hmily insert statement
     */
    public static HmilyInsertStatement assembleHmilyInsertStatement(final InsertStatement insertStatement, final HmilyInsertStatement hmilyInsertStatement) {
        HmilySimpleTableSegment hmilySimpleTableSegment = CommonAssembler.assembleHmilySimpleTableSegment(insertStatement.getTable());
        HmilyInsertColumnsSegment hmilyInsertColumnsSegment = null;
        if (insertStatement.getInsertColumns().isPresent()) {
            hmilyInsertColumnsSegment = assembleHmilyInsertColumnsSegment(insertStatement.getInsertColumns().get());
        }
        Collection<HmilyInsertValuesSegment> hmilyInsertValuesSegments = assembleHmilyInsertValuesSegments(insertStatement.getValues());
        hmilyInsertStatement.setTable(hmilySimpleTableSegment);
        hmilyInsertStatement.setInsertColumns(hmilyInsertColumnsSegment);
        for (HmilyInsertValuesSegment each : hmilyInsertValuesSegments) {
            hmilyInsertStatement.getValues().add(each);
        }
        return hmilyInsertStatement;
    }
    
    private static HmilyInsertColumnsSegment assembleHmilyInsertColumnsSegment(final InsertColumnsSegment insertColumnsSegment) {
        Collection<HmilyColumnSegment> hmilyColumnSegments = new LinkedList<>();
        for (ColumnSegment each : insertColumnsSegment.getColumns()) {
            hmilyColumnSegments.add(CommonAssembler.assembleHmilyColumnSegment(each));
        }
        return new HmilyInsertColumnsSegment(insertColumnsSegment.getStartIndex(), insertColumnsSegment.getStopIndex(), hmilyColumnSegments);
    }
    
    private static Collection<HmilyInsertValuesSegment> assembleHmilyInsertValuesSegments(final Collection<InsertValuesSegment> insertValuesSegments) {
        Collection<HmilyInsertValuesSegment> hmilyInsertValuesSegments = new LinkedList<>();
        for (InsertValuesSegment each : insertValuesSegments) {
            List<HmilyExpressionSegment> hmilyExpressionSegments = new LinkedList<>();
            for (ExpressionSegment expressionSegment : each.getValues()) {
                hmilyExpressionSegments.add(CommonAssembler.assembleHmilyExpressionSegment(expressionSegment));
            }
            hmilyInsertValuesSegments.add(new HmilyInsertValuesSegment(each.getStartIndex(), each.getStopIndex(), hmilyExpressionSegments));
        }
        return hmilyInsertValuesSegments;
    }
}
