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

package org.dromara.hmily.tac.datasource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The type Hmily prepared statement.
 *
 * @author xiaoyu
 */
public class HmilyPreparedStatement extends AbstractHmilyPreparedStatement implements PreparedStatement {
    
    /**
     * Instantiates a new Hmily prepared statement.
     *
     * @param connectionWrapper the connection wrapper
     * @param targetStatement   the target statement
     * @param targetSQL         the target sql
     */
    public HmilyPreparedStatement(final AbstractHmilyConnection connectionWrapper, final PreparedStatement targetStatement, final String targetSQL) {
        super(connectionWrapper, targetStatement, targetSQL);
    }
    
    @Override
    public boolean execute() throws SQLException {
        return getTargetStatement().execute();
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        return getTargetStatement().executeQuery();
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        return getTargetStatement().executeUpdate();
    }
}
