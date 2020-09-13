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

package org.dromara.hmily.tac.sqlrevert.spi;

import org.dromara.hmily.repository.spi.entity.HmilyUndoInvocation;
import org.dromara.hmily.tac.sqlparser.model.statement.HmilyStatement;
import org.dromara.hmily.tac.sqlrevert.spi.exception.SqlRevertException;

import java.sql.Connection;

/**
 * The interface Hmily sql revert engine.
 *
 * @author xiaoyu
 */
public interface HmilySqlRevertEngine {
    
    /**
     * Revert hmily undo invocation.
     *
     * @param hmilyStatement the sql statement
     * @param connection   connection
     * @param sql          the sql
     * @return the hmily undo invocation
     * @throws SqlRevertException the sql revert exception
     */
    HmilyUndoInvocation revert(HmilyStatement hmilyStatement, Connection connection, String sql) throws SqlRevertException;
}
