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

package org.dromara.hmily.xa.p6spy;

import com.p6spy.engine.spy.P6DataSource;
import com.p6spy.engine.wrapper.ConnectionWrapper;
import lombok.Getter;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The type Hmily p 6 datasource xa.
 *
 * @author xiaoyu
 */
public class HmilyXaP6Datasource extends P6DataSource {
    
    @Getter
    private DataSource targetDataSource;
    
    /**
     * Instantiates a new Hmily p 6 datasource.
     *
     * @param delegate the delegate
     */
    public HmilyXaP6Datasource(final DataSource delegate) {
        super(delegate);
        init(delegate);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        ConnectionWrapper connection = (ConnectionWrapper)super.getConnection();
        return getHmilyConnection(connection.getDelegate());
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        ConnectionWrapper connection = (ConnectionWrapper)super.getConnection(username, password);
        return getHmilyConnection(connection.getDelegate());
    }
    
    /**
     * Gets hmily connection.
     *
     * @param connection the connection
     * @return the hmily connection
     */
    private Connection getHmilyConnection(final Connection connection) throws SQLException {
        XAConnection xaConnection;
        try {
            Connection unwrap = connection.unwrap(Connection.class);
            xaConnection = XaConnectionFactory.warp(unwrap);
        } catch (Exception e) {
            throw new SQLException(e);
        }
        return new HmilyXaConnection(xaConnection);
    }
    
    private void init(final DataSource delegate) {
        if (delegate == null) {
            throw new NullPointerException("targetDataSource is null");
        }
        targetDataSource = delegate;
    }
}
