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

package org.dromara.hmily.tac.sqlparser.shardingsphere.common;

import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.HmilyStatement;

/**
 * Hmily SQL parser executor.
 */
public interface HmilySQLParserExecutor {
    
    /**
     * Execute update statement.
     *
     * @param updateStatement update statement
     * @return hmily statement
     */
    HmilyStatement executeUpdateStatement(UpdateStatement updateStatement);
    
    /**
     * Execute insert statement.
     *
     * @param insertStatement insert statement
     * @return hmily statement
     */
    HmilyStatement executeInsertStatement(InsertStatement insertStatement);
    
    /**
     * Execute delete statement.
     *
     * @param deleteStatement delete statement
     * @return hmily statement
     */
    HmilyStatement executeDeleteStatement(DeleteStatement deleteStatement);

    /**
     * Execute select statement.
     *
     * @param selectStatement select statement
     * @return hmily statement
     */
    HmilyStatement executeSelectStatement(SelectStatement selectStatement);
}
