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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import lombok.Getter;

/**
 * The type Abstract hmily connection.
 *
 * @author xiaoyu
 */
@Getter
public abstract class AbstractHmilyConnection implements Connection {
    
    /**
     * The Hmily datasource.
     */
    protected HmilyTacDatasource hmilyDatasource;
    
    /**
     * The Target connection.
     */
    protected Connection targetConnection;
    
    /**
     * Instantiates a new Abstract hmily connection.
     *
     * @param dataSourceProxy  the data source proxy
     * @param targetConnection the target connection
     */
    public AbstractHmilyConnection(final HmilyTacDatasource dataSourceProxy, final Connection targetConnection) {
        this.hmilyDatasource = dataSourceProxy;
        this.targetConnection = targetConnection;
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        Statement targetStatement = getTargetConnection().createStatement();
        return new HmilyStatement<>(this, targetStatement);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        //进行sql解析什么的
        return new HmilyPreparedStatement(this, getTargetConnection().prepareStatement(sql), sql);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        return targetConnection.prepareCall(sql);
    }
    
    @Override
    public String nativeSQL(final String sql) throws SQLException {
        return targetConnection.nativeSQL(sql);
    }
    
    @Override
    public boolean getAutoCommit() throws SQLException {
        return targetConnection.getAutoCommit();
    }
    
    @Override
    public void close() throws SQLException {
        targetConnection.close();
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return targetConnection.isClosed();
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return targetConnection.getMetaData();
    }
    
    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        targetConnection.setReadOnly(readOnly);
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        return targetConnection.isReadOnly();
    }
    
    @Override
    public void setCatalog(final String catalog) throws SQLException {
        targetConnection.setCatalog(catalog);
    }
    
    @Override
    public String getCatalog() throws SQLException {
        return targetConnection.getCatalog();
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        targetConnection.setTransactionIsolation(level);
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        return targetConnection.getTransactionIsolation();
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return targetConnection.getWarnings();
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        targetConnection.clearWarnings();
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        Statement statement = targetConnection.createStatement(resultSetType, resultSetConcurrency);
        return new HmilyStatement<>(this, statement);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        PreparedStatement preparedStatement = targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        return new HmilyPreparedStatement(this, preparedStatement, sql);
    }
    
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return targetConnection.getTypeMap();
    }
    
    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        targetConnection.setTypeMap(map);
    }
    
    @Override
    public void setHoldability(final int holdability) throws SQLException {
        targetConnection.setHoldability(holdability);
    }
    
    @Override
    public int getHoldability() throws SQLException {
        return targetConnection.getHoldability();
    }
    
    @Override
    public Savepoint setSavepoint() throws SQLException {
        return targetConnection.setSavepoint();
    }
    
    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        return targetConnection.setSavepoint(name);
    }
    
    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        targetConnection.rollback(savepoint);
    }
    
    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        targetConnection.releaseSavepoint(savepoint);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability)
            throws SQLException {
        Statement statement = targetConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        return new HmilyStatement<>(this, statement);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        PreparedStatement preparedStatement = targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        return new HmilyPreparedStatement(this, preparedStatement, sql);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        PreparedStatement preparedStatement = targetConnection.prepareStatement(sql, autoGeneratedKeys);
        return new HmilyPreparedStatement(this, preparedStatement, sql);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        PreparedStatement preparedStatement = targetConnection.prepareStatement(sql, columnIndexes);
        return new HmilyPreparedStatement(this, preparedStatement, sql);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        PreparedStatement preparedStatement = targetConnection.prepareStatement(sql, columnNames);
        return new HmilyPreparedStatement(this, preparedStatement, sql);
    }
    
    @Override
    public Clob createClob() throws SQLException {
        return targetConnection.createClob();
    }
    
    @Override
    public Blob createBlob() throws SQLException {
        return targetConnection.createBlob();
    }
    
    @Override
    public NClob createNClob() throws SQLException {
        return targetConnection.createNClob();
    }
    
    @Override
    public SQLXML createSQLXML() throws SQLException {
        return targetConnection.createSQLXML();
    }
    
    @Override
    public boolean isValid(int timeout) throws SQLException {
        return targetConnection.isValid(timeout);
    }
    
    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        targetConnection.setClientInfo(name, value);
    }
    
    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        targetConnection.setClientInfo(properties);
    }
    
    @Override
    public String getClientInfo(final String name) throws SQLException {
        return targetConnection.getClientInfo(name);
    }
    
    @Override
    public Properties getClientInfo() throws SQLException {
        return targetConnection.getClientInfo();
    }
    
    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        return targetConnection.createArrayOf(typeName, elements);
    }
    
    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        return targetConnection.createStruct(typeName, attributes);
    }
    
    @Override
    public void setSchema(final String schema) throws SQLException {
        targetConnection.setSchema(schema);
    }
    
    @Override
    public String getSchema() throws SQLException {
        return targetConnection.getSchema();
    }
    
    @Override
    public void abort(final Executor executor) throws SQLException {
        targetConnection.abort(executor);
    }
    
    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        targetConnection.setNetworkTimeout(executor, milliseconds);
    }
    
    @Override
    public int getNetworkTimeout() throws SQLException {
        return targetConnection.getNetworkTimeout();
    }
    
    @Override
    public <T> T unwrap(final Class<T> clazz) throws SQLException {
        return targetConnection.unwrap(clazz);
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> clazz) throws SQLException {
        return targetConnection.isWrapperFor(clazz);
    }
}
