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
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.HmilyStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.postgresql.dml.HmilyPostgreSQLDeleteStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.postgresql.dml.HmilyPostgreSQLInsertStatement;
import org.dromara.hmily.tac.sqlparser.model.dialect.postgresql.dml.HmilyPostgreSQLUpdateStatement;
import org.dromara.hmily.tac.sqlparser.shardingsphere.common.AbstractHmilySQLParserExecutor;

/**
 * Hmily PostgreSQL parser executor.
 */
public final class HmilyPostgreSQLParserExecutor extends AbstractHmilySQLParserExecutor {
    
    @Override
    public HmilyStatement executeUpdateStatement(final UpdateStatement updateStatement) {
        HmilyPostgreSQLUpdateStatement hmilyPostgreSQLUpdateStatement = new HmilyPostgreSQLUpdateStatement();
        return generateHmilyUpdateStatement(updateStatement, hmilyPostgreSQLUpdateStatement);
    }
    
    @Override
    public HmilyStatement executeInsertStatement(final InsertStatement insertStatement) {
        HmilyPostgreSQLInsertStatement hmilyPostgreSQLInsertStatement = new HmilyPostgreSQLInsertStatement();
        return generateHmilyInsertStatement(insertStatement, hmilyPostgreSQLInsertStatement);
    }
    
    @Override
    public HmilyStatement executeDeleteStatement(final DeleteStatement deleteStatement) {
        HmilyPostgreSQLDeleteStatement hmilyPostgreSQLDeleteStatement = new HmilyPostgreSQLDeleteStatement();
        return generateHmilyDeleteStatement(deleteStatement, hmilyPostgreSQLDeleteStatement);
    }
}
