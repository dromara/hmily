/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dromara.hmily.xa.p6spy;

import org.dromara.hmily.xa.p6spy.mysql.Mysql5WarpXaConnection;
import org.dromara.hmily.xa.p6spy.mysql.Mysql6WarpXaConnection;
import org.dromara.hmily.xa.p6spy.mysql.Mysql8WarpXaConnection;

import javax.sql.XAConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XaConnectionFactory .
 *
 * @author sixh chenbin
 */
public class XaConnectionFactory {
    
    private static final Map<Integer, WarpXaConnection> MYSQL_CONNECTION = new ConcurrentHashMap<>(3);
    
    public static XAConnection warp(final Connection connection) throws Exception {
        String url = connection.getMetaData().getURL();
        JDBCProtocol protocol = JDBCProtocol.getProtocol(url);
        if (protocol == null) {
            throw new SQLFeatureNotSupportedException("不支持的数据库." + url);
        }
        if (protocol == JDBCProtocol.MYSQL) {
            return mysqlWarpConnection(connection).warp(connection);
        }
        throw new SQLFeatureNotSupportedException();
    }
    
    private static synchronized WarpXaConnection mysqlWarpConnection(final Connection connection) throws SQLException {
        int jdbcMajorVersion = connection.getMetaData().getDriverMajorVersion();
        WarpXaConnection warpXaConnection = MYSQL_CONNECTION.get(jdbcMajorVersion);
        if (warpXaConnection == null) {
            switch (jdbcMajorVersion) {
                case 5:
                    warpXaConnection = new Mysql5WarpXaConnection();
                    break;
                case 6:
                    warpXaConnection = new Mysql6WarpXaConnection();
                    break;
                case 8:
                    warpXaConnection = new Mysql8WarpXaConnection();
                    break;
                default:
                    throw new SQLFeatureNotSupportedException("不支持的Mysql驱动版本:" + jdbcMajorVersion);
            }
        }
        MYSQL_CONNECTION.put(jdbcMajorVersion, warpXaConnection);
        return warpXaConnection;
    }
}
