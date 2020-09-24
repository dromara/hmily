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

import java.sql.Connection;
import java.sql.SQLException;


/**
 * The type Hmily connection.
 *
 * @author xiaoyu
 */
public class HmilyConnection extends AbstractHmilyConnection {
    
    /**
     * Instantiates a new Hmily connection.
     *
     * @param hmilyDatasource  the hmily datasource
     * @param targetConnection the target connection
     */
    public HmilyConnection(final HmilyTacDatasource hmilyDatasource, final Connection targetConnection) {
        super(hmilyDatasource, targetConnection);
    }
    
    @Override
    public void commit() throws SQLException {
        try {
            doCommit();
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public void rollback() throws SQLException {
        getTargetConnection().rollback();
    }

    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        if (autoCommit && !getAutoCommit()) {
            doCommit();
        }
        getTargetConnection().setAutoCommit(autoCommit);
    }
    
    private void doCommit() throws SQLException {
        getTargetConnection().commit();
    }
}
