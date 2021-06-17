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

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.assignment.HmilyAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.assignment.HmilySetAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.column.HmilyColumnSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.predicate.HmilyWhereSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.table.HmilySimpleTableSegment;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyUpdateStatement;

import java.util.Collection;
import java.util.LinkedList;

public final class UpdateStatementAssembler {
    
    /**
     * Assemble Hmily update statement.
     *
     * @param updateStatement update statement
     * @param hmilyUpdateStatement hmily update statement
     * @return hmily update statement
     */
    public static HmilyUpdateStatement assembleHmilyUpdateStatement(final UpdateStatement updateStatement, final HmilyUpdateStatement hmilyUpdateStatement) {
        HmilySimpleTableSegment hmilySimpleTableSegment = CommonAssembler.assembleHmilySimpleTableSegment((SimpleTableSegment) updateStatement.getTableSegment());
        HmilySetAssignmentSegment hmilySetAssignmentSegment = assembleHmilySetAssignmentSegment(updateStatement.getSetAssignment());
        HmilyWhereSegment hmilyWhereSegment = null;
        if (updateStatement.getWhere().isPresent()) {
            hmilyWhereSegment = assembleHmilyWhereSegment(updateStatement.getWhere().get());
        }
        hmilyUpdateStatement.setTableSegment(hmilySimpleTableSegment);
        hmilyUpdateStatement.setSetAssignment(hmilySetAssignmentSegment);
        hmilyUpdateStatement.setWhere(hmilyWhereSegment);
        return hmilyUpdateStatement;
    }
    
    private static HmilySetAssignmentSegment assembleHmilySetAssignmentSegment(final SetAssignmentSegment setAssignmentSegment) {
        Collection<HmilyAssignmentSegment> assignments = new LinkedList<>();
        for (AssignmentSegment each : setAssignmentSegment.getAssignments()) {
            HmilyColumnSegment hmilyColumnSegment = CommonAssembler.assembleHmilyColumnSegment(each.getColumn());
            HmilyAssignmentSegment hmilyAssignmentSegment =
                    new HmilyAssignmentSegment(each.getStartIndex(), each.getStopIndex(), hmilyColumnSegment, CommonAssembler.assembleHmilyExpressionSegment(each.getValue()));
            assignments.add(hmilyAssignmentSegment);
        }
        return new HmilySetAssignmentSegment(setAssignmentSegment.getStartIndex(), setAssignmentSegment.getStopIndex(), assignments);
    }
    
    private static HmilyWhereSegment assembleHmilyWhereSegment(final WhereSegment whereSegment) {
        HmilyExpressionSegment hmilyExpressionSegment = CommonAssembler.assembleHmilyExpressionSegment(whereSegment.getExpr());
        return new HmilyWhereSegment(whereSegment.getStartIndex(), whereSegment.getStopIndex(), hmilyExpressionSegment);
    }
}
