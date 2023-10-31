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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * MysqlWarpXaConnection .
 *
 * @author sixh chenbin
 */
public class Mysql5WarpXaConnection implements WarpXaConnection {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Mysql5WarpXaConnection.class);
    private boolean isJdbc4 = false;
    
    private volatile boolean loadClass = false;
    
    private Map<String, Class<?>> clazz = new HashMap<>();
    
    private Map<String, Method> methods = new HashMap<>();
    
    private Map<String, Constructor<?>> constructor = new HashMap<>();
    
    private final String utilClass = "com.mysql.jdbc.Util";
    private final String connectionClass = "com.mysql.jdbc.Connection";
    private final String suspendableXAConnectionClass = "com.mysql.jdbc.jdbc2.optional.SuspendableXAConnection";
    private final String jdbc4SuspendableXAConnectionClass = "com.mysql.jdbc.jdbc2.optional.JDBC4SuspendableXAConnection";
    private final String mysqlXAConnectionClass = "com.mysql.jdbc.jdbc2.optional.MysqlXAConnection";
    
    @Override
    @SuppressWarnings("all")
    public XAConnection warp(final Connection connection) {
        int majorVersion = 0;
        try {
            majorVersion = connection.getMetaData().getDriverMajorVersion();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //版本5的处理.
        if (majorVersion == 5) {
            this.init();
            //mysql5.0
            try {
                boolean isPinGlobal = (boolean) this.getPinGlobalTxToPhysicalConnection(connection).invoke(connection);
                if (isPinGlobal) {
                    if (isJdbc4) {
                        return (XAConnection) this.getJdbc4SuspendableXAConnectionConstructor().newInstance(connection);
                    }
                    return (XAConnection) this.getSuspendableXAConnectionConstructor().newInstance(connection);
                }
                return (XAConnection) this.getMysqlXAConnectionConstructor().newInstance(connection, Boolean.FALSE);
            } catch (Exception e) {
                LOGGER.info("exception:", e);
            }
            
        }
        throw new RuntimeException("mysql jdbc version: " + majorVersion + " 不能使用当前的类进行初始化.");
    }
    
    @SuppressWarnings("all")
    private void init() {
        try {
            if (loadClass) {
                return;
            }
            clazz.put(utilClass, Class.forName(utilClass));
            clazz.put(connectionClass, Class.forName(connectionClass));
            clazz.put(suspendableXAConnectionClass, Class.forName(suspendableXAConnectionClass));
            clazz.put(jdbc4SuspendableXAConnectionClass, Class.forName(jdbc4SuspendableXAConnectionClass));
            clazz.put(mysqlXAConnectionClass, Class.forName(mysqlXAConnectionClass));
            
            isJdbc4 = (Boolean) getUtilClass().getMethod("isJdbc4").invoke(null);
            //method.
            Method getPinGlobalTxToPhysicalConnectionMethod = this.getConnectionClass().getMethod("getPinGlobalTxToPhysicalConnection");
            methods.put("getPinGlobalTxToPhysicalConnection", getPinGlobalTxToPhysicalConnectionMethod);
            //constructor
            constructor.put(suspendableXAConnectionClass, this.getSuspendableXAConnectionClass().getConstructor(this.getConnectionClass()));
            constructor.put(mysqlXAConnectionClass, this.getMysqlXAConnectionClass().getConstructor(this.getConnectionClass(), boolean.class));
            constructor.put(jdbc4SuspendableXAConnectionClass, this.getJdbc4SuspendableXAConnectionClass().getConstructor(this.getConnectionClass()));
            loadClass = true;
        } catch (Exception exception) {
            LOGGER.info("exception:", exception);
            loadClass = false;
        }
    }
    
    private Method getPinGlobalTxToPhysicalConnection(Connection connection) throws NoSuchMethodException {
        return connection.getClass().getMethod("getPinGlobalTxToPhysicalConnection");
    }
    
    private Class<?> getUtilClass() {
        return clazz.get(utilClass);
    }
    
    public Class<?> getConnectionClass() {
        return clazz.get(connectionClass);
    }
    
    public Class<?> getSuspendableXAConnectionClass() {
        return clazz.get(suspendableXAConnectionClass);
    }
    
    public Class<?> getJdbc4SuspendableXAConnectionClass() {
        return clazz.get(jdbc4SuspendableXAConnectionClass);
    }
    
    public Class<?> getMysqlXAConnectionClass() {
        return clazz.get(mysqlXAConnectionClass);
    }
    
    public Constructor<?> getSuspendableXAConnectionConstructor() {
        return constructor.get(suspendableXAConnectionClass);
    }
    
    public Constructor<?> getJdbc4SuspendableXAConnectionConstructor() {
        return constructor.get(jdbc4SuspendableXAConnectionClass);
    }
    
    public Constructor<?> getMysqlXAConnectionConstructor() {
        return constructor.get(mysqlXAConnectionClass);
    }
}
