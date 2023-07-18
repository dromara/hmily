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

package org.dromara.hmily.tac.sqlparser.shardingsphere.dialect;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.pagination.HmilyPaginationValueSegment;
import org.dromara.hmily.tac.sqlparser.model.common.segment.dml.pagination.limit.HmilyLimitSegment;
import org.dromara.hmily.tac.sqlparser.model.common.statement.HmilyStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.sqlserver.dml.HmilySQLServerDeleteStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.sqlserver.dml.HmilySQLServerInsertStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.sqlserver.dml.HmilySQLServerSelectStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.sqlserver.dml.HmilySQLServerUpdateStatement;
import org.dromara.hmily.tac.sqlparser.shardingsphere.common.AbstractHmilySQLParserExecutor;
import org.dromara.hmily.tac.sqlparser.shardingsphere.common.handler.CommonAssembler;

/**
 * Hmily SQLServer parser executor.
 */
public final class HmilySQLServerParserExecutor extends AbstractHmilySQLParserExecutor {
    
    @Override
    public HmilyStatement executeUpdateStatement(final UpdateStatement updateStatement) {
        HmilySQLServerUpdateStatement hmilySQLServerUpdateStatement = new HmilySQLServerUpdateStatement();
        return generateHmilyUpdateStatement(updateStatement, hmilySQLServerUpdateStatement);
    }
    
    @Override
    public HmilyStatement executeInsertStatement(final InsertStatement insertStatement) {
        HmilySQLServerInsertStatement hmilySQLServerInsertStatement = new HmilySQLServerInsertStatement();
        return generateHmilyInsertStatement(insertStatement, hmilySQLServerInsertStatement);
    }
    
    @Override
    public HmilyStatement executeDeleteStatement(final DeleteStatement deleteStatement) {
        HmilySQLServerDeleteStatement hmilySQLServerDeleteStatement = new HmilySQLServerDeleteStatement();
        return generateHmilyDeleteStatement(deleteStatement, hmilySQLServerDeleteStatement);
    }

    @Override
    public HmilyStatement executeSelectStatement(final SelectStatement selectStatement) {
        HmilySQLServerSelectStatement hmilySQLServerSelectStatement = new HmilySQLServerSelectStatement();
        generateHmilySelectStatement(selectStatement, hmilySQLServerSelectStatement);
        SQLServerSelectStatement sqlServerSelectStatement = (SQLServerSelectStatement) selectStatement;
        if (sqlServerSelectStatement.getLimit().isPresent()) {
            LimitSegment limitSegment = sqlServerSelectStatement.getLimit().get();
            HmilyPaginationValueSegment offset = null;
            HmilyPaginationValueSegment rowCount = null;
            if (limitSegment.getOffset().isPresent()) {
                offset = CommonAssembler.assembleHmilyPaginationValueSegment(limitSegment.getOffset().get());
            }
            if (limitSegment.getRowCount().isPresent()) {
                rowCount = CommonAssembler.assembleHmilyPaginationValueSegment(limitSegment.getRowCount().get());
            }
            HmilyLimitSegment hmilyLimitSegment = new HmilyLimitSegment(limitSegment.getStartIndex(), limitSegment.getStopIndex(), offset, rowCount);
            hmilySQLServerSelectStatement.setLimit(hmilyLimitSegment);
        }
        return hmilySQLServerSelectStatement;
    }
}
