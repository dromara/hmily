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

package org.dromara.hmily.xa.p6spy;

import com.p6spy.engine.spy.P6DataSource;
import lombok.Getter;
import org.dromara.hmily.xa.core.TransactionManagerImpl;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The type Hmily p 6 datasource xa.
 *
 * @author xiaoyu
 */
public class HmilyXAP6Datasource extends P6DataSource {

    @Getter
    private final DataSource targetDataSource;

    /**
     * Instantiates a new Hmily p 6 datasource.
     *
     * @param delegate the delegate
     */
    public HmilyXAP6Datasource(final DataSource delegate) {
        super(delegate);
        targetDataSource = delegate;
        init();
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        return getHmilyConnection(connection);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        return getHmilyConnection(connection);
    }

    /**
     * Gets hmily connection.
     *
     * @param connection the connection
     * @return the hmily connection
     */
    private Connection getHmilyConnection(Connection connection) {
        return new HmilyXaConnection(connection);
    }

    private void init() {
        if (this.targetDataSource == null) {
            throw new NullPointerException("targetDataSource is null");
        }
        if (!(this.targetDataSource instanceof XADataSource)) {
            throw new RuntimeException("targetDataSource have not instanceof XADataSource");
        }
        TransactionManagerImpl.INST.initialized();
    }
}
