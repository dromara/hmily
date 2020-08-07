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
import javax.sql.DataSource;
import org.dromara.hmily.tac.common.HmilyTacResource;
import org.dromara.hmily.tac.common.HmilyResourceManager;
import org.dromara.hmily.tac.common.HmilyTacRollbackExecutor;

/**
 * The type Hmily datasource.
 *
 * @author xiaoyu
 */
public class HmilyTacDatasource extends AbstractHmilyDataSource implements HmilyTacResource {
    
    private String jdbcUrl;
    
    /**
     * Instantiates a new Abstract data source proxy.
     *
     * @param targetDataSource the target data source
     */
    public HmilyTacDatasource(final DataSource targetDataSource) {
        super(targetDataSource);
        init(targetDataSource);
    }
    
    private void init(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            jdbcUrl = connection.getMetaData().getURL();
        } catch (SQLException e) {
            throw new IllegalStateException("can not init dataSource", e);
        }
        HmilyResourceManager.register(this);
        HmilyTacRollbackExecutor.getInstance();
    }
    
    @Override
    public String getResourceId() {
        if (jdbcUrl.contains("?")) {
            return jdbcUrl.substring(0, jdbcUrl.indexOf('?'));
        } else {
            return jdbcUrl;
        }
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        Connection targetConnection = targetDataSource.getConnection();
        return new HmilyConnection(this, targetConnection);
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        Connection targetConnection = targetDataSource.getConnection(username, password);
        return new HmilyConnection(this, targetConnection);
    }
}
