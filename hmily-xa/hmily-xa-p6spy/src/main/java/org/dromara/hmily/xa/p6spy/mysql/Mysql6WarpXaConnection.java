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

package org.dromara.hmily.xa.p6spy.mysql;

import org.dromara.hmily.xa.p6spy.WarpXaConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.XAConnection;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;

/**
 * Mysql8WarpXaConnection .
 * 版本为mysql6相关的处理.
 *
 * @author sixh chenbin
 */
public class Mysql6WarpXaConnection implements WarpXaConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(Mysql6WarpXaConnection.class);
    private final String getPropertySetMethod = "getPropertySet";
    private final String getBooleanReadableProperty = "getBooleanReadableProperty";
    private final String version6ConnectionClass = "com.mysql.cj.api.jdbc.JdbcConnection";
    private final String getValueMethod = "getValue";
    private final String getInstance = "getInstance";
    private final Map<String, Class<?>> clazz = new HashMap<>();
    private final Map<String, Method> methods = new HashMap<>();
    private volatile boolean initError = false;
    private volatile boolean isInit = false;
    
    @Override
    public XAConnection warp(final Connection connection) throws SQLFeatureNotSupportedException {
        try {
            int majorVersion = connection.getMetaData().getDriverMajorVersion();
            if (majorVersion != this.getVersion()) {
                throw new RuntimeException("mysql jdbc version: " + majorVersion + " 不能使用当前的类进行初始化.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.init();
        try {
            Boolean pinGlobalTx = (Boolean) this.getValueMethod().invoke(
                    this.getBooleanReadableProperty().invoke(
                            this.getPropertySetMethod().invoke(connection), "pinGlobalTxToPhysicalConnection"));
            if (pinGlobalTx != null && pinGlobalTx) {
                if (this.getInstance() == null && !initError) {
                    String xaConnectionClass = "com.mysql.cj.jdbc.SuspendableXAConnection";
                    Class<?> xaConnectionClazz = Class.forName(xaConnectionClass);
                    Method getInstance = xaConnectionClazz.getDeclaredMethod("getInstance", this.getConnectionClass());
                    getInstance.setAccessible(true);
                    methods.put(this.getInstance, this.getInstance());
                }
                return (XAConnection) this.getInstance().invoke(null, connection);
            } else {
                if (this.getInstance() == null && !initError) {
                    String xaConnectionClass2 = "com.mysql.cj.jdbc.MysqlXAConnection";
                    Class<?> xaConnectionClazz = Class.forName(xaConnectionClass2);
                    Method getInstance = xaConnectionClazz.getDeclaredMethod("getInstance", this.getConnectionClass(), boolean.class);
                    getInstance.setAccessible(true);
                    methods.put(this.getInstance, getInstance);
                }
                return (XAConnection) this.getInstance().invoke(null, connection, Boolean.FALSE);
            }
        } catch (Exception e) {
            initError = true;
            LOGGER.error("", e);
        }
        throw new SQLFeatureNotSupportedException();
    }
    
    private void init() {
        if (isInit) {
            return;
        }
        Class<?> connectionClass;
        try {
            connectionClass = Class.forName(this.getConnectionClassName());
            Method getPropertySetMethod = connectionClass.getMethod(this.getPropertySetMethod);
            methods.put(this.getPropertySetMethod, getPropertySetMethod);
            Method getBooleanReadablePropertyMd = Class.forName(this.getPropertySetClass()).getMethod(this.getBooleanReadableProperty, String.class);
            methods.put(this.getBooleanReadableProperty, getBooleanReadablePropertyMd);
            Method getValue = Class.forName(this.getReadablePropertyClassName()).getMethod(this.getValueMethod);
            methods.put(this.getValueMethod, getValue);
        } catch (Exception e) {
            initError = true;
            LOGGER.error("", e);
            return;
        }
        clazz.put(this.getConnectionClassName(), connectionClass);
        isInit = true;
    }
    
    private Method getInstance() {
        return this.methods.get(this.getInstance);
    }
    
    private Class<?> getConnectionClass() {
        return clazz.get(this.version6ConnectionClass);
    }
    
    private Method getPropertySetMethod() {
        return this.methods.get(this.getPropertySetMethod);
    }
    
    private Method getBooleanReadableProperty() {
        return this.methods.get(this.getBooleanReadableProperty);
    }
    
    private Method getValueMethod() {
        return this.methods.get(this.getValueMethod);
    }
    
    protected String getConnectionClassName() {
        return this.version6ConnectionClass;
    }
    
    protected String getPropertySetClass() {
        return "com.mysql.cj.api.conf.PropertySet";
    }
    
    protected String getReadablePropertyClassName() {
        return "com.mysql.cj.api.conf.ReadableProperty";
    }
    
    protected int getVersion() {
        return 6;
    }
}
