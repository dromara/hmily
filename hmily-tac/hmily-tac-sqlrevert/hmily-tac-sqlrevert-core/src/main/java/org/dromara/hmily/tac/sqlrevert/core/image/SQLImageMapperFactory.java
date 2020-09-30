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

package org.dromara.hmily.tac.sqlrevert.core.image;

import org.dromara.hmily.repository.spi.entity.HmilyUndoInvocation;
import org.dromara.hmily.tac.sqlrevert.core.image.impl.DeleteSQLImageMapper;
import org.dromara.hmily.tac.sqlrevert.core.image.impl.InsertSQLImageMapper;
import org.dromara.hmily.tac.sqlrevert.core.image.impl.UpdateSQLImageMapper;
import org.dromara.hmily.tac.sqlrevert.spi.exception.SQLRevertException;

/**
 * SQL image mapper factory.
 *
 * @author zhaojun
 */
public class SQLImageMapperFactory {
    
    /**
     * Create new instance of SQL image mapper.
     *
     * @param undoInvocation undo invocation
     * @return SQL image mapper
     */
    public static SQLImageMapper newInstance(final HmilyUndoInvocation undoInvocation) {
        switch (undoInvocation.getManipulationType()) {
            case "insert":
                return new InsertSQLImageMapper(undoInvocation.getTableName(), undoInvocation.getAfterImage());
            case "update":
                return new UpdateSQLImageMapper(undoInvocation.getTableName(), undoInvocation.getBeforeImage(), undoInvocation.getAfterImage(), undoInvocation.getOriginSql());
            case "delete":
                return new DeleteSQLImageMapper(undoInvocation.getTableName(), undoInvocation.getBeforeImage());
            default:
                throw new SQLRevertException(String.format("unsupported SQL manipulate type [%s]", undoInvocation.getManipulationType()));
        }
    }
}
