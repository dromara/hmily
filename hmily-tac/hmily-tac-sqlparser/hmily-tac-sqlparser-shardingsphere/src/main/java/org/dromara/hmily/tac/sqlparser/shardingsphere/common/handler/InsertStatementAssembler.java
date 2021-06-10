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

import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.dromara.hmily.tac.sqlparser.model.common.statement.dml.HmilyInsertStatement;

public final class InsertStatementAssembler {
    
    /**
     * Assemble Hmily insert statement.
     *
     * @param insertStatement insert statement
     * @param hmilyInsertStatement hmily insert statement
     * @return hmily insert statement
     */
    public static HmilyInsertStatement assembleHmilyInsertStatement(final InsertStatement insertStatement, final HmilyInsertStatement hmilyInsertStatement) {
        return hmilyInsertStatement;
    }
}
