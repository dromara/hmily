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

package org.dromara.hmily.tac.sqlparser.shardingsphere;

import org.apache.shardingsphere.sql.parser.SQLParserEngineFactory;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.dromara.hmily.spi.HmilySPI;
import org.dromara.hmily.tac.sqlparser.model.constant.HmilyQuoteCharacter;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.HmilyAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.assignment.HmilySetAssignmentSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.column.HmilyColumnSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.HmilyExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.complex.HmilyCommonExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.simple.HmilyLiteralExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.expr.simple.HmilyParameterMarkerExpressionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.item.HmilyExpressionProjectionSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.HmilyAndPredicate;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.HmilyPredicateSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.HmilyWhereSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.dml.predicate.value.HmilyPredicateRightValue;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.HmilyAliasSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.HmilyOwnerSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.table.HmilySimpleTableSegment;
import org.dromara.hmily.tac.sqlparser.model.segment.generic.table.HmilyTableNameSegment;
import org.dromara.hmily.tac.sqlparser.model.statement.HmilyStatement;
import org.dromara.hmily.tac.sqlparser.model.statement.dml.HmilyDeleteStatement;
import org.dromara.hmily.tac.sqlparser.model.statement.dml.HmilyInsertStatement;
import org.dromara.hmily.tac.sqlparser.model.statement.dml.HmilyUpdateStatement;
import org.dromara.hmily.tac.sqlparser.model.value.identifier.HmilyIdentifierValue;
import org.dromara.hmily.tac.sqlparser.spi.HmilySqlParserEngine;
import org.dromara.hmily.tac.sqlparser.spi.exception.SqlParserException;

import java.util.Collection;
import java.util.LinkedList;

/**
 * The type Sharding sphere sql parser engine.
 *
 * @author xiaoyu
 */
@HmilySPI("shardingSphere")
public class ShardingSphereSqlParserEngine implements HmilySqlParserEngine {
    
    @Override
    public HmilyStatement parser(final String sql, final String databaseType) throws SqlParserException {
        SQLStatement sqlStatement = SQLParserEngineFactory.getSQLParserEngine(databaseType).parse(sql, false);
        if (sqlStatement instanceof UpdateStatement) {
            return generateHmilyUpdateStatement((UpdateStatement) sqlStatement);
        } else if (sqlStatement instanceof InsertStatement) {
            return generateHmilyInsertStatement((InsertStatement) sqlStatement);
        } else if (sqlStatement instanceof DeleteStatement) {
            return generateHmilyDeleteStatement((DeleteStatement) sqlStatement);
        } else {
            throw new SqlParserException("Unsupported SQL Type.");
        }
    }
    
    private HmilyUpdateStatement generateHmilyUpdateStatement(final UpdateStatement updateStatement) {
        HmilyUpdateStatement result = new HmilyUpdateStatement();
        assembleSimpleTableSegment(updateStatement, result);
        assembleSetAssignmentSegment(updateStatement, result);
        if (updateStatement.getWhere().isPresent()) {
            assembleWhereSegment(updateStatement, result);
        }
        return result;
    }
    
    private void assembleSimpleTableSegment(final UpdateStatement updateStatement, final HmilyUpdateStatement result) {
        for (final SimpleTableSegment each : updateStatement.getTables()) {
            TableNameSegment tableNameSegment = each.getTableName();
            HmilyQuoteCharacter quoteCharacter = HmilyQuoteCharacter.getQuoteCharacter(tableNameSegment.getIdentifier().getQuoteCharacter().toString());
            HmilyIdentifierValue hmilyIdentifierValue = new HmilyIdentifierValue(tableNameSegment.getIdentifier().getValue(), quoteCharacter);
            HmilyTableNameSegment hmilyTableNameSegment = new HmilyTableNameSegment(tableNameSegment.getStartIndex(), tableNameSegment.getStopIndex(), hmilyIdentifierValue);
            HmilyOwnerSegment hmilyOwnerSegment = null;
            OwnerSegment ownerSegment;
            if (each.getOwner().isPresent()) {
                ownerSegment = each.getOwner().get();
                hmilyOwnerSegment = new HmilyOwnerSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(),
                        new HmilyIdentifierValue(ownerSegment.getIdentifier().getValue(), HmilyQuoteCharacter.getQuoteCharacter(ownerSegment.getIdentifier().getQuoteCharacter().toString())));
            }
            HmilyAliasSegment hmilyAliasSegment = null;
            String aliasSegmentString;
            if (each.getAlias().isPresent()) {
                aliasSegmentString = each.getAlias().get();
                hmilyAliasSegment = new HmilyAliasSegment(0, 0, new HmilyIdentifierValue(aliasSegmentString, HmilyQuoteCharacter.NONE));
            }
            HmilySimpleTableSegment hmilySimpleTableSegment = new HmilySimpleTableSegment(hmilyTableNameSegment);
            hmilySimpleTableSegment.setOwner(hmilyOwnerSegment);
            hmilySimpleTableSegment.setAlias(hmilyAliasSegment);
            result.getTables().add(hmilySimpleTableSegment);
        }
    }
    
    private void assembleSetAssignmentSegment(final UpdateStatement updateStatement, final HmilyUpdateStatement result) {
        Collection<HmilyAssignmentSegment> assignments = new LinkedList<>();
        for (final AssignmentSegment each : updateStatement.getSetAssignment().getAssignments()) {
            HmilyQuoteCharacter quoteCharacter = HmilyQuoteCharacter.getQuoteCharacter(each.getColumn().getIdentifier().getQuoteCharacter().toString());
            HmilyIdentifierValue hmilyIdentifierValue = new HmilyIdentifierValue(each.getColumn().getIdentifier().getValue(), quoteCharacter);
            HmilyColumnSegment hmilyColumnSegment = new HmilyColumnSegment(each.getColumn().getStartIndex(), each.getColumn().getStopIndex(), hmilyIdentifierValue);
            ExpressionSegment expressionSegment = each.getValue();
            HmilyExpressionSegment hmilyExpressionSegment;
            if (expressionSegment instanceof CommonExpressionSegment) {
                hmilyExpressionSegment = new HmilyCommonExpressionSegment(expressionSegment.getStartIndex(),
                        expressionSegment.getStopIndex(), ((CommonExpressionSegment) expressionSegment).getText());
            } else if (expressionSegment instanceof ExpressionProjectionSegment) {
                hmilyExpressionSegment = new HmilyExpressionProjectionSegment(expressionSegment.getStartIndex(),
                        expressionSegment.getStopIndex(), ((ExpressionProjectionSegment) expressionSegment).getText());
            } else if (expressionSegment instanceof LiteralExpressionSegment) {
                hmilyExpressionSegment = new HmilyLiteralExpressionSegment(expressionSegment.getStartIndex(),
                        expressionSegment.getStopIndex(), ((LiteralExpressionSegment) expressionSegment).getLiterals());
            } else {
                hmilyExpressionSegment = new HmilyParameterMarkerExpressionSegment(expressionSegment.getStartIndex(),
                        expressionSegment.getStopIndex(), ((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
            }
            HmilyAssignmentSegment hmilyAssignmentSegment = new HmilyAssignmentSegment(each.getStartIndex(), each.getStopIndex(), hmilyColumnSegment, hmilyExpressionSegment);
            assignments.add(hmilyAssignmentSegment);
        }
        HmilySetAssignmentSegment hmilySetAssignmentSegment = new HmilySetAssignmentSegment(updateStatement.getSetAssignment().getStartIndex(),
                updateStatement.getSetAssignment().getStopIndex(), assignments);
        result.setSetAssignment(hmilySetAssignmentSegment);
    }
    
    private void assembleWhereSegment(final UpdateStatement updateStatement, final HmilyUpdateStatement result) {
        HmilyWhereSegment hmilyWhereSegment = new HmilyWhereSegment(updateStatement.getWhere().get().getStartIndex(), updateStatement.getWhere().get().getStopIndex());
        for (final AndPredicate each : updateStatement.getWhere().get().getAndPredicates()) {
            HmilyAndPredicate hmilyAndPredicate = new HmilyAndPredicate();
            for (final PredicateSegment predicateSegment : each.getPredicates()) {
                HmilyQuoteCharacter quoteCharacter = HmilyQuoteCharacter.getQuoteCharacter(predicateSegment.getColumn().getIdentifier().getQuoteCharacter().toString());
                HmilyIdentifierValue hmilyIdentifierValue = new HmilyIdentifierValue(predicateSegment.getColumn().getIdentifier().getValue(), quoteCharacter);
                PredicateRightValue predicateRightValue = predicateSegment.getRightValue();
                HmilyPredicateRightValue hmilyRightValue;
                // TODO Support other segments except ColumnSegment
                HmilyQuoteCharacter quoteCharacterCS = HmilyQuoteCharacter.getQuoteCharacter(((ColumnSegment) predicateRightValue).getIdentifier().getQuoteCharacter().toString());
                HmilyIdentifierValue hmilyIdentifierValueCS = new HmilyIdentifierValue(((ColumnSegment) predicateRightValue).getIdentifier().getValue(), quoteCharacterCS);
                HmilyQuoteCharacter quoteCharacterOS = HmilyQuoteCharacter.getQuoteCharacter(((ColumnSegment) predicateRightValue).getOwner().get().getIdentifier().getQuoteCharacter().toString());
                HmilyIdentifierValue hmilyIdentifierValueOS = new HmilyIdentifierValue(((ColumnSegment) predicateRightValue).getOwner().get().getIdentifier().getValue(), quoteCharacterOS);
                HmilyOwnerSegment owner = new HmilyOwnerSegment(((ColumnSegment) predicateRightValue).getOwner().get().getStartIndex(),
                        ((ColumnSegment) predicateRightValue).getOwner().get().getStopIndex(), hmilyIdentifierValueOS);
                hmilyRightValue = new HmilyColumnSegment(((ColumnSegment) predicateRightValue).getStartIndex(), ((ColumnSegment) predicateRightValue).getStopIndex(), hmilyIdentifierValueCS);
                ((HmilyColumnSegment) hmilyRightValue).setOwner(owner);
                HmilyColumnSegment hmilyColumnSegment = new HmilyColumnSegment(predicateSegment.getColumn().getStartIndex(), predicateSegment.getColumn().getStopIndex(), hmilyIdentifierValue);
                HmilyPredicateSegment hmilyPredicateSegment = new HmilyPredicateSegment(predicateSegment.getStartIndex(), predicateSegment.getStopIndex(), hmilyColumnSegment, hmilyRightValue);
                hmilyAndPredicate.getPredicates().add(hmilyPredicateSegment);
            }
            hmilyWhereSegment.getHmilyAndPredicates().add(hmilyAndPredicate);
        }
        result.setWhere(hmilyWhereSegment);
    }
    
    private HmilyInsertStatement generateHmilyInsertStatement(final InsertStatement insertStatement) {
        HmilyInsertStatement result = new HmilyInsertStatement();
        return result;
    }
    
    private HmilyDeleteStatement generateHmilyDeleteStatement(final DeleteStatement deleteStatement) {
        HmilyDeleteStatement result = new HmilyDeleteStatement();
        return result;
    }
}
