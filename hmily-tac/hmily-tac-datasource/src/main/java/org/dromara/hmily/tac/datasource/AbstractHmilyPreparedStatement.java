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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * The type Abstract hmily prepared statement.
 *
 * @author xiaoyu
 */
@Getter
public abstract class AbstractHmilyPreparedStatement extends HmilyStatement<PreparedStatement> implements PreparedStatement {
    
    /**
     * The Parameters.
     */
    private Map<Integer, List<Object>> parameters = new HashMap<>();
    
    /**
     * Instantiates a new Abstract hmily prepared statement.
     *
     * @param connectionWrapper the connection wrapper
     * @param preparedStatement the prepared statement
     * @param targetSQL         the target sql
     */
    public AbstractHmilyPreparedStatement(final AbstractHmilyConnection connectionWrapper, final PreparedStatement preparedStatement, final String targetSQL) {
        super(connectionWrapper, preparedStatement, targetSQL);
    }
    
    /**
     * Instantiates a new Abstract hmily prepared statement.
     *
     * @param connectionWrapper the connection wrapper
     * @param preparedStatement the prepared statement
     */
    public AbstractHmilyPreparedStatement(final AbstractHmilyConnection connectionWrapper, final PreparedStatement preparedStatement) {
        super(connectionWrapper, preparedStatement);
    }
    
    /**
     * Gets params by index.
     *
     * @param index the index
     * @return the params by index
     */
    public List<Object> getParamsByIndex(final int index) {
        return parameters.get(index);
    }
    
    /**
     * Sets param by index.
     *
     * @param index the index
     * @param obj   the obj
     */
    protected void setParamByIndex(final int index, final Object obj) {
        parameters.computeIfAbsent(index, e -> new ArrayList<>()).add(obj);
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        setParamByIndex(parameterIndex, null);
        getTargetStatement().setNull(parameterIndex, sqlType);
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        setParamByIndex(parameterIndex, null);
        getTargetStatement().setNull(parameterIndex, sqlType, typeName);
    }
    
    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setBoolean(parameterIndex, x);
    }
    
    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setByte(parameterIndex, x);
    }
    
    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setShort(parameterIndex, x);
    }
    
    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setInt(parameterIndex, x);
    }
    
    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setLong(parameterIndex, x);
    }
    
    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setFloat(parameterIndex, x);
    }
    
    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setDouble(parameterIndex, x);
    }
    
    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setBigDecimal(parameterIndex, x);
    }
    
    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setString(parameterIndex, x);
    }
    
    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setBytes(parameterIndex, x);
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setDate(parameterIndex, x);
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setDate(parameterIndex, x, cal);
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setTime(parameterIndex, x);
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setTime(parameterIndex, x, cal);
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setTimestamp(parameterIndex, x);
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setTimestamp(parameterIndex, x, cal);
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setAsciiStream(parameterIndex, x, length);
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setAsciiStream(parameterIndex, x, length);
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setAsciiStream(parameterIndex, x);
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setBinaryStream(parameterIndex, x, length);
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setBinaryStream(parameterIndex, x, length);
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setBinaryStream(parameterIndex, x);
    }
    
    @Override
    public void clearParameters() throws SQLException {
        parameters = new HashMap<>();
        getTargetStatement().clearParameters();
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setObject(parameterIndex, x, targetSqlType);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setObject(parameterIndex, x);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }
    
    @Override
    public void addBatch() throws SQLException {
        getTargetStatement().addBatch();
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        setParamByIndex(parameterIndex, reader);
        getTargetStatement().setCharacterStream(parameterIndex, reader, length);
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        setParamByIndex(parameterIndex, reader);
        getTargetStatement().setCharacterStream(parameterIndex, reader, length);
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader) throws SQLException {
        setParamByIndex(parameterIndex, reader);
        getTargetStatement().setCharacterStream(parameterIndex, reader);
    }
    
    @Override
    public void setRef(final int parameterIndex, final Ref x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setRef(parameterIndex, x);
    }
    
    @Override
    public void setBlob(final int parameterIndex, final Blob x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setBlob(parameterIndex, x);
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
        setParamByIndex(parameterIndex, inputStream);
        getTargetStatement().setBlob(parameterIndex, inputStream, length);
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream) throws SQLException {
        setParamByIndex(parameterIndex, inputStream);
        getTargetStatement().setBlob(parameterIndex, inputStream);
    }
    
    @Override
    public void setClob(final int parameterIndex, final Clob x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setClob(parameterIndex, x);
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        setParamByIndex(parameterIndex, reader);
        getTargetStatement().setClob(parameterIndex, reader, length);
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader) throws SQLException {
        setParamByIndex(parameterIndex, reader);
        getTargetStatement().setClob(parameterIndex, reader);
    }
    
    @Override
    public void setArray(final int parameterIndex, final Array x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setArray(parameterIndex, x);
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return getTargetStatement().getMetaData();
    }
    
    @Override
    public void setURL(final int parameterIndex, final URL x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setURL(parameterIndex, x);
    }
    
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return getTargetStatement().getParameterMetaData();
    }
    
    @Override
    public void setRowId(final int parameterIndex, final RowId x) throws SQLException {
        setParamByIndex(parameterIndex, x);
        getTargetStatement().setRowId(parameterIndex, x);
    }
    
    @Override
    public void setNString(final int parameterIndex, final String value) throws SQLException {
        setParamByIndex(parameterIndex, value);
        getTargetStatement().setNString(parameterIndex, value);
    }
    
    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value, final long length) throws SQLException {
        setParamByIndex(parameterIndex, value);
        getTargetStatement().setNCharacterStream(parameterIndex, value, length);
    }
    
    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value) throws SQLException {
        setParamByIndex(parameterIndex, value);
        getTargetStatement().setNCharacterStream(parameterIndex, value);
    }
    
    @Override
    public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
        setParamByIndex(parameterIndex, value);
        getTargetStatement().setNClob(parameterIndex, value);
    }
    
    @Override
    public void setNClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        setParamByIndex(parameterIndex, reader);
        getTargetStatement().setNClob(parameterIndex, reader, length);
    }
    
    @Override
    public void setNClob(final int parameterIndex, final Reader reader) throws SQLException {
        setParamByIndex(parameterIndex, reader);
        getTargetStatement().setNClob(parameterIndex, reader);
    }
    
    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException {
        setParamByIndex(parameterIndex, xmlObject);
        getTargetStatement().setSQLXML(parameterIndex, xmlObject);
    }
    
    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        getTargetStatement().setUnicodeStream(parameterIndex, x, length);
    }
}
