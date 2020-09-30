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

package org.dromara.hmily.tac.sqlcompute;

import org.dromara.hmily.repository.spi.entity.HmilyUndoInvocation;
import org.dromara.hmily.tac.sqlcompute.exception.SQLComputeException;

import java.sql.Connection;

/**
 * Hmily SQL compute engine interface.
 *
 * @author zhaojun
 */
public interface HmilySQLComputeEngine {
    
    /**
     * Revert hmily undo invocation.
     *
     * @param connection   connection
     * @param sql          the sql
     * @return the hmily undo invocation
     * @throws SQLComputeException the SQL compute exception
     */
    HmilyUndoInvocation generateImage(Connection connection, String sql) throws SQLComputeException;
}
