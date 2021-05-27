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

package org.dromara.hmily.tac.sqlparser.shardingsphere.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyDeleteStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyInsertStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyUpdateStatement;
import org.dromara.hmily.tac.sqlparser.shardingsphere.common.handler.DeleteStatementAssembler;
import org.dromara.hmily.tac.sqlparser.shardingsphere.common.handler.InsertStatementAssembler;
import org.dromara.hmily.tac.sqlparser.shardingsphere.common.handler.UpdateStatementAssembler;

/**
 * Abstract Hmily SQL parser executor.
 */
@Slf4j
public abstract class AbstractHmilySQLParserExecutor implements HmilySQLParserExecutor {
    
    /**
     * Generate Hmily update statement.
     *
     * @param updateStatement update statement
     * @param hmilyUpdateStatement hmily update statement
     * @return hmily update statement
     */
    public HmilyUpdateStatement generateHmilyUpdateStatement(final UpdateStatement updateStatement, final HmilyUpdateStatement hmilyUpdateStatement) {
        return UpdateStatementAssembler.assembleHmilyUpdateStatement(updateStatement, hmilyUpdateStatement);
    }
    
    /**
     * Generate Hmily insert statement.
     *
     * @param insertStatement insert statement
     * @param hmilyInsertStatement hmily insert statement
     * @return hmily insert statement
     */
    public HmilyInsertStatement generateHmilyInsertStatement(final InsertStatement insertStatement, final HmilyInsertStatement hmilyInsertStatement) {
        return InsertStatementAssembler.assembleHmilyInsertStatement(insertStatement, hmilyInsertStatement);
    }
    
    /**
     * Generate Hmily delete statement.
     *
     * @param deleteStatement delete statement
     * @param hmilyDeleteStatement hmily delete statement
     * @return hmily delete statement
     */
    public HmilyDeleteStatement generateHmilyDeleteStatement(final DeleteStatement deleteStatement, final HmilyDeleteStatement hmilyDeleteStatement) {
        return DeleteStatementAssembler.assembleHmilyDeleteStatement(deleteStatement, hmilyDeleteStatement);
    }
}
