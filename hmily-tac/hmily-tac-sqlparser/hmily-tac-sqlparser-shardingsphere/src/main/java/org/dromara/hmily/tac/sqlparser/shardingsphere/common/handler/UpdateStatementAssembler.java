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

package org.dromara.hmily.tac.sqlparser.shardingsphere.common.handler;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionBuilder;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.assignment.HmilyAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.assignment.HmilySetAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.column.HmilyColumnSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.complex.HmilyCommonExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.simple.HmilyLiteralExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.expr.simple.HmilyParameterMarkerExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.item.HmilyExpressionProjectionSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.predicate.HmilyAndPredicate;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.predicate.HmilyWhereSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.HmilyAliasSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.HmilyOwnerSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.table.HmilySimpleTableSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.generic.table.HmilyTableNameSegment;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyUpdateStatement;
import org.dromara.hmily.tac.sqlparser.model.common.value.identifier.HmilyIdentifierValue;

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
        assembleSimpleTableSegment(updateStatement, hmilyUpdateStatement);
        assembleSetAssignmentSegment(updateStatement, hmilyUpdateStatement);
        if (updateStatement.getWhere().isPresent()) {
            assembleWhereSegment(updateStatement, hmilyUpdateStatement);
        }
        return hmilyUpdateStatement;
    }
    
    private static void assembleSimpleTableSegment(final UpdateStatement updateStatement, final HmilyUpdateStatement hmilyUpdateStatement) {
        SimpleTableSegment simpleTableSegment = (SimpleTableSegment) updateStatement.getTableSegment();
        TableNameSegment tableNameSegment = simpleTableSegment.getTableName();
        HmilyIdentifierValue hmilyIdentifierValue = new HmilyIdentifierValue(tableNameSegment.getIdentifier().getValue());
        HmilyTableNameSegment hmilyTableNameSegment = new HmilyTableNameSegment(tableNameSegment.getStartIndex(), tableNameSegment.getStopIndex(), hmilyIdentifierValue);
        HmilyOwnerSegment hmilyOwnerSegment = null;
        OwnerSegment ownerSegment;
        if (simpleTableSegment.getOwner().isPresent()) {
            ownerSegment = simpleTableSegment.getOwner().get();
            hmilyOwnerSegment = new HmilyOwnerSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(),
                    new HmilyIdentifierValue(ownerSegment.getIdentifier().getValue()));
        }
        HmilyAliasSegment hmilyAliasSegment = null;
        String aliasSegmentString;
        if (simpleTableSegment.getAlias().isPresent()) {
            aliasSegmentString = simpleTableSegment.getAlias().get();
            hmilyAliasSegment = new HmilyAliasSegment(0, 0, new HmilyIdentifierValue(aliasSegmentString));
        }
        HmilySimpleTableSegment hmilySimpleTableSegment = new HmilySimpleTableSegment(hmilyTableNameSegment);
        hmilySimpleTableSegment.setOwner(hmilyOwnerSegment);
        hmilySimpleTableSegment.setAlias(hmilyAliasSegment);
        hmilyUpdateStatement.setTableSegment(hmilySimpleTableSegment);
    }
    
    private static void assembleSetAssignmentSegment(final UpdateStatement updateStatement, final HmilyUpdateStatement result) {
        Collection<HmilyAssignmentSegment> assignments = new LinkedList<>();
        for (AssignmentSegment each : updateStatement.getSetAssignment().getAssignments()) {
            HmilyColumnSegment hmilyColumnSegment = assembleColumnSegment(each.getColumn());
            HmilyAssignmentSegment hmilyAssignmentSegment = new HmilyAssignmentSegment(each.getStartIndex(), each.getStopIndex(), hmilyColumnSegment, assembleExpressionSegment(each.getValue()));
            assignments.add(hmilyAssignmentSegment);
        }
        HmilySetAssignmentSegment hmilySetAssignmentSegment = new HmilySetAssignmentSegment(updateStatement.getSetAssignment().getStartIndex(),
                updateStatement.getSetAssignment().getStopIndex(), assignments);
        result.setSetAssignment(hmilySetAssignmentSegment);
    }
    
    private static void assembleWhereSegment(final UpdateStatement updateStatement, final HmilyUpdateStatement result) {
        updateStatement.getWhere().ifPresent(whereSegment -> {
            HmilyWhereSegment hmilyWhereSegment = null;
            OrPredicateSegment orPredicateSegment = new ExpressionBuilder(whereSegment.getExpr()).extractAndPredicates();
            for (AndPredicate andPredicate : orPredicateSegment.getAndPredicates()) {
                HmilyAndPredicate hmilyAndPredicate = new HmilyAndPredicate();
                for (ExpressionSegment expression : andPredicate.getPredicates()) {
                    if (expression instanceof BinaryOperationExpression && ((BinaryOperationExpression) expression).getLeft() instanceof ColumnSegment) {
                        HmilyColumnSegment hmilyColumnSegment = assembleColumnSegment((ColumnSegment) ((BinaryOperationExpression) expression).getLeft());
                        ExpressionSegment right = ((BinaryOperationExpression) expression).getRight();
                        HmilyExpressionSegment hmilyExpressionSegment;
                        if (right instanceof ParameterMarkerExpressionSegment) {
                            hmilyExpressionSegment = new HmilyParameterMarkerExpressionSegment(right.getStartIndex(), right.getStopIndex(), ((ParameterMarkerExpressionSegment) right)
                                    .getParameterMarkerIndex());
                            hmilyWhereSegment = new HmilyWhereSegment(whereSegment.getStartIndex(), whereSegment.getStopIndex(), hmilyExpressionSegment);
                        }
                    }
                    if (expression instanceof InExpression && ((InExpression) expression).getLeft() instanceof ColumnSegment) {
                        // TODO
                        ColumnSegment column = (ColumnSegment) ((InExpression) expression).getLeft();
                    }
                    if (expression instanceof BetweenExpression && ((BetweenExpression) expression).getLeft() instanceof ColumnSegment) {
                        // TODO
                        ColumnSegment column = (ColumnSegment) ((BetweenExpression) expression).getLeft();
                    }
                }
            }
            result.setWhere(hmilyWhereSegment);
        });
    }
    
    private static HmilyColumnSegment assembleColumnSegment(final ColumnSegment column) {
        HmilyIdentifierValue hmilyIdentifierValue = new HmilyIdentifierValue(column.getIdentifier().getValue());
        HmilyColumnSegment result = new HmilyColumnSegment(column.getStartIndex(), column.getStopIndex(), hmilyIdentifierValue);
        column.getOwner().ifPresent(ownerSegment -> {
            HmilyIdentifierValue identifierValue = new HmilyIdentifierValue(ownerSegment.getIdentifier().getValue());
            result.setOwner(new HmilyOwnerSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), identifierValue));
        });
        return result;
    }
    
    private static HmilyExpressionSegment assembleExpressionSegment(final ExpressionSegment expression) {
        HmilyExpressionSegment result = null;
        if (expression instanceof ColumnSegment) {
            result = assembleColumnSegment((ColumnSegment) expression);
        } else if (expression instanceof CommonExpressionSegment) {
            result = new HmilyCommonExpressionSegment(expression.getStartIndex(),
                    expression.getStopIndex(), ((CommonExpressionSegment) expression).getText());
        } else if (expression instanceof ExpressionProjectionSegment) {
            result = new HmilyExpressionProjectionSegment(expression.getStartIndex(),
                    expression.getStopIndex(), ((ExpressionProjectionSegment) expression).getText());
        } else if (expression instanceof LiteralExpressionSegment) {
            result = new HmilyLiteralExpressionSegment(expression.getStartIndex(),
                    expression.getStopIndex(), ((LiteralExpressionSegment) expression).getLiterals());
        } else if (expression instanceof ParameterMarkerExpressionSegment) {
            result = new HmilyParameterMarkerExpressionSegment(expression.getStartIndex(),
                    expression.getStopIndex(), ((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex());
        }
        return result;
    }
    
}
