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

package org.dromara.hmily.repository.database.manager;

import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariDataSource;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.dromara.hmily.config.HmilyConfig;
import org.dromara.hmily.config.HmilyDbConfig;
import org.dromara.hmily.repository.spi.HmilyRepository;
import org.dromara.hmily.repository.spi.entity.HmilyTransaction;
import org.dromara.hmily.repository.spi.exception.HmilyRepositoryException;
import org.dromara.hmily.serializer.spi.HmilySerializer;

/**
 * The type Abstract hmily database.
 */
@Slf4j
public abstract class AbstractHmilyDatabase implements HmilyRepository {
    
    protected static final String INSERT_HMILY_TRANSACTION = "INSERT INTO hmily_transaction_global (trans_id,app_name,status,trans_type,"
            + "retry,version,create_time,update_time) VALUES(?,?,?,?,?,?,?,?,?)";
    
    /**
     * The data source.
     */
    protected DataSource dataSource;
    
    /**
     * The hmily serializer
     */
    protected HmilySerializer hmilySerializer;
    
    /**
     * The App name.
     */
    protected String appName;
    
    /**
     * Sql file path string.
     *
     * @return the string
     */
    protected abstract String sqlFilePath();
    
    /**
     * Convert data type object.
     *
     * @param params the params
     * @return the object
     */
    protected abstract Object convertDataType(final Object params);
    
    @Override
    public void init(final HmilyConfig hmilyConfig) {
        try {
            HmilyDbConfig hmilyDbConfig = hmilyConfig.getHmilyDbConfig();
            HikariDataSource hikariDataSource = new HikariDataSource();
            hikariDataSource.setJdbcUrl(hmilyDbConfig.getUrl());
            hikariDataSource.setDriverClassName(hmilyDbConfig.getDriverClassName());
            hikariDataSource.setUsername(hmilyDbConfig.getUsername());
            hikariDataSource.setPassword(hmilyDbConfig.getPassword());
            hikariDataSource.setMaximumPoolSize(hmilyDbConfig.getMaxActive());
            hikariDataSource.setMinimumIdle(hmilyDbConfig.getMinIdle());
            hikariDataSource.setConnectionTimeout(hmilyDbConfig.getConnectionTimeout());
            hikariDataSource.setIdleTimeout(hmilyDbConfig.getIdleTimeout());
            hikariDataSource.setMaxLifetime(hmilyDbConfig.getMaxLifetime());
            hikariDataSource.setConnectionTestQuery(hmilyDbConfig.getConnectionTestQuery());
            if (hmilyDbConfig.getDataSourcePropertyMap() != null && !hmilyDbConfig.getDataSourcePropertyMap().isEmpty()) {
                hmilyDbConfig.getDataSourcePropertyMap().forEach(hikariDataSource::addDataSourceProperty);
            }
            this.dataSource = hikariDataSource;
            this.appName = hmilyConfig.getAppName();
            if (hmilyConfig.isAutoSql()) {
                this.executeScript(dataSource.getConnection(), sqlFilePath());
            }
        } catch (Exception e) {
            log.error("hmily jdbc log init exception please check config:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void setSerializer(final HmilySerializer hmilySerializer) {
        this.hmilySerializer = hmilySerializer;
    }
    
    @Override
    public int beginHmilyTransaction(final HmilyTransaction hmilyTransaction) {
        try {
            return executeUpdate(INSERT_HMILY_TRANSACTION, hmilyTransaction.getTransId(), hmilyTransaction.getAppName(), hmilyTransaction.getStatus(),
                    hmilyTransaction.getTransType(), hmilyTransaction.getRetry(), hmilyTransaction.getVersion(), hmilyTransaction.getCreateTime(), hmilyTransaction.getUpdateTime());
        } catch (HmilyRepositoryException e) {
            log.error("create begin hmilyTransaction have exception ", e);
            return FAIL_ROWS;
        }
    }
    
    /**
     * Execute update int.
     *
     * @param sql    the sql
     * @param params the params
     * @return the int
     */
    protected int executeUpdate(final String sql, final Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, convertDataType(params[i]));
                }
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error("executeUpdate-> " + e.getMessage());
            return FAIL_ROWS;
        } finally {
            close(connection, ps, null);
        }
    }
    
    private List<Map<String, Object>> executeQuery(final String sql, final Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, convertDataType(params[i]));
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> rowData = Maps.newHashMap();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            log.error("executeQuery-> " + e.getMessage());
        } finally {
            close(connection, ps, rs);
        }
        return list;
    }
    
    private static void close(final AutoCloseable... closeables) {
        if (null != closeables && closeables.length > 0) {
            for (AutoCloseable closeable : closeables) {
                close(closeable);
            }
        }
    }
    
    private static void close(final AutoCloseable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
    
    private void executeScript(final Connection conn, final String sqlPath) throws Exception {
        ScriptRunner runner = new ScriptRunner(conn);
        // doesn't print logger
        runner.setLogWriter(null);
        runner.setAutoCommit(false);
        Resources.setCharset(StandardCharsets.UTF_8);
        Reader read = Resources.getResourceAsReader(sqlPath);
        runner.runScript(read);
        conn.commit();
        runner.closeConnection();
        conn.close();
    }
}
