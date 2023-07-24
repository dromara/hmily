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

import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.HmilyStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.oracle.dml.HmilyOracleDeleteStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.oracle.dml.HmilyOracleInsertStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.oracle.dml.HmilyOracleSelectStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.oracle.dml.HmilyOracleUpdateStatement;
import org.dromara.hmily.tac.sqlparser.shardingsphere.common.AbstractHmilySQLParserExecutor;

/**
 * Hmily Oracle parser executor.
 */
public final class HmilyOracleParserExecutor extends AbstractHmilySQLParserExecutor {
    
    @Override
    public HmilyStatement executeUpdateStatement(final UpdateStatement updateStatement) {
        HmilyOracleUpdateStatement hmilyOracleUpdateStatement = new HmilyOracleUpdateStatement();
        return generateHmilyUpdateStatement(updateStatement, hmilyOracleUpdateStatement);
    }
    
    @Override
    public HmilyStatement executeInsertStatement(final InsertStatement insertStatement) {
        HmilyOracleInsertStatement hmilyOracleInsertStatement = new HmilyOracleInsertStatement();
        return generateHmilyInsertStatement(insertStatement, hmilyOracleInsertStatement);
    }
    
    @Override
    public HmilyStatement executeDeleteStatement(final DeleteStatement deleteStatement) {
        HmilyOracleDeleteStatement hmilyOracleDeleteStatement = new HmilyOracleDeleteStatement();
        return generateHmilyDeleteStatement(deleteStatement, hmilyOracleDeleteStatement);
    }

    @Override
    public HmilyStatement executeSelectStatement(final SelectStatement selectStatement) {
        HmilyOracleSelectStatement hmilyOracleSelectStatement = new HmilyOracleSelectStatement();
        return generateHmilySelectStatement(selectStatement, hmilyOracleSelectStatement);
    }
}
