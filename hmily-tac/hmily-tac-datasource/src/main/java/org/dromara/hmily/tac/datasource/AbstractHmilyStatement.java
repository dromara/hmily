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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import lombok.Getter;


/**
 * The type Abstract hmily statement.
 *
 * @param <T> the type parameter
 *
 * @author xiaoyu
 */
@Getter
public abstract class AbstractHmilyStatement<T extends Statement> implements Statement {
    
    /**
     * The Connection wrapper.
     */
    protected AbstractHmilyConnection connectionWrapper;
    
    /**
     * The Target statement.
     */
    protected T targetStatement;
    
    private CachedRowSet generatedKeysRowSet;
    
    /**
     * The Target sql.
     */
    protected String targetSQL;
    
    /**
     * Instantiates a new Abstract hmily statement.
     *
     * @param connectionWrapper the connection wrapper
     * @param targetStatement   the target statement
     * @param targetSQL         the target sql
     */
    public AbstractHmilyStatement(final AbstractHmilyConnection connectionWrapper, final T targetStatement, final String targetSQL) {
        this.connectionWrapper = connectionWrapper;
        this.targetStatement = targetStatement;
        this.targetSQL = targetSQL;
    }
    
    /**
     * Instantiates a new Abstract hmily statement.
     *
     * @param hmilyConnection the hmily connection
     * @param targetStatement the target statement
     */
    public AbstractHmilyStatement(final HmilyConnection hmilyConnection, final T targetStatement) {
        this(hmilyConnection, targetStatement, null);
    }
    

    @Override
    public void close() throws SQLException {
        targetStatement.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return targetStatement.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(final int max) throws SQLException {
        targetStatement.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return targetStatement.getMaxRows();
    }

    @Override
    public void setMaxRows(final int max) throws SQLException {
        targetStatement.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        targetStatement.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return targetStatement.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        targetStatement.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        targetStatement.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return targetStatement.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        targetStatement.clearWarnings();
    }

    @Override
    public void setCursorName(final String name) throws SQLException {
        targetStatement.setCursorName(name);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return targetStatement.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return targetStatement.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return targetStatement.getMoreResults();
    }

    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        targetStatement.setFetchDirection(direction);

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return targetStatement.getFetchDirection();
    }

    @Override
    public void setFetchSize(final int rows) throws SQLException {
        targetStatement.setFetchSize(rows);

    }

    @Override
    public int getFetchSize() throws SQLException {
        return targetStatement.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return targetStatement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return targetStatement.getResultSetType();
    }

    @Override
    public void addBatch(final String sql) throws SQLException {
        targetStatement.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        targetStatement.clearBatch();
        targetSQL = null;
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return targetStatement.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return targetStatement.getConnection();
    }

    @Override
    public boolean getMoreResults(final int current) throws SQLException {
        return targetStatement.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        if (generatedKeysRowSet != null) {
            return generatedKeysRowSet;
        }
        ResultSet rs = targetStatement.getGeneratedKeys();
        generatedKeysRowSet = RowSetProvider.newFactory().createCachedRowSet();
        generatedKeysRowSet.populate(rs);
        return generatedKeysRowSet;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return targetStatement.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return targetStatement.isClosed();
    }

    @Override
    public void setPoolable(final boolean poolable) throws SQLException {
        targetStatement.setPoolable(poolable);

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return targetStatement.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        targetStatement.closeOnCompletion();

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return targetStatement.isCloseOnCompletion();
    }

    @Override
    public <t> t unwrap(final Class<t> clazz) throws SQLException {
        return targetStatement.unwrap(clazz);
    }

    @Override
    public boolean isWrapperFor(final Class<?> clazz) throws SQLException {
        return targetStatement.isWrapperFor(clazz);
    }
}
