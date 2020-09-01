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

package org.dromara.hmily.tac.sqlrevert.core;

import javax.sql.DataSource;
import org.dromara.hmily.repository.spi.entity.HmilyUndoInvocation;
import org.dromara.hmily.spi.HmilySPI;
import org.dromara.hmily.tac.sqlparser.model.statement.SQLStatement;
import org.dromara.hmily.tac.sqlrevert.spi.HmilySqlRevertEngine;
import org.dromara.hmily.tac.sqlrevert.spi.exception.SqlRevertException;

/**
 * The type Default sql revert engine.
 *
 * @author xiaoyu
 */
@HmilySPI("default")
public class DefaultSqlRevertEngine implements HmilySqlRevertEngine {
    
    @Override
    public HmilyUndoInvocation revert(final SQLStatement sqlStatement, final DataSource dataSource) throws SqlRevertException {
        HmilyUndoInvocation undoInvocation = new HmilyUndoInvocation();
        undoInvocation.setSql("select 1");
        //根据jdbcUrl获取 datasource
        return undoInvocation;
    }
}
