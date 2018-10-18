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

package com.hmily.tcc.core.spi.repository;

import com.google.common.collect.Maps;
import com.hmily.tcc.common.bean.entity.Participant;
import com.hmily.tcc.common.bean.entity.TccTransaction;
import com.hmily.tcc.common.config.TccConfig;
import com.hmily.tcc.common.config.TccDbConfig;
import com.hmily.tcc.common.constant.CommonConstant;
import com.hmily.tcc.common.enums.RepositorySupportEnum;
import com.hmily.tcc.common.exception.TccException;
import com.hmily.tcc.common.exception.TccRuntimeException;
import com.hmily.tcc.common.serializer.ObjectSerializer;
import com.hmily.tcc.common.utils.DbTypeUtils;
import com.hmily.tcc.common.utils.LogUtil;
import com.hmily.tcc.common.utils.RepositoryPathUtils;
import com.hmily.tcc.core.helper.SqlHelper;
import com.hmily.tcc.core.spi.CoordinatorRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * jdbc impl.
 *
 * @author xiaoyu
 */
@SuppressWarnings("all")
public class JdbcCoordinatorRepository implements CoordinatorRepository {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcCoordinatorRepository.class);

    private DataSource dataSource;

    private String tableName;

    private String currentDBType;

    private ObjectSerializer serializer;

    @Override
    public void setSerializer(final ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public int create(final TccTransaction tccTransaction) {
        String sql = "insert into " + tableName + "(trans_id,target_class,target_method,retried_count,"
                + "create_time,last_time,version,status,invocation,role,pattern,confirm_method,cancel_method)"
                + " values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            final byte[] serialize = serializer.serialize(tccTransaction.getParticipants());
            return executeUpdate(sql, tccTransaction.getTransId(), tccTransaction.getTargetClass(), tccTransaction.getTargetMethod(),
                    tccTransaction.getRetriedCount(), tccTransaction.getCreateTime(), tccTransaction.getLastTime(),
                    tccTransaction.getVersion(), tccTransaction.getStatus(), serialize, tccTransaction.getRole(),
                    tccTransaction.getPattern(), tccTransaction.getConfirmMethod(), tccTransaction.getCancelMethod());
        } catch (TccException e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
    }

    @Override
    public int remove(final String id) {
        String sql = "delete from " + tableName + " where trans_id = ? ";
        return executeUpdate(sql, id);
    }

    @Override
    public int update(final TccTransaction tccTransaction) {
        final Integer currentVersion = tccTransaction.getVersion();
        tccTransaction.setLastTime(new Date());
        tccTransaction.setVersion(tccTransaction.getVersion() + 1);
        String sql = "update " + tableName
                + " set last_time = ?,version =?,retried_count =?,invocation=?,status=? ,pattern=? where trans_id = ? and version=? ";
        try {
            final byte[] serialize = serializer.serialize(tccTransaction.getParticipants());
            return executeUpdate(sql, tccTransaction.getLastTime(),
                    tccTransaction.getVersion(), tccTransaction.getRetriedCount(), serialize,
                    tccTransaction.getStatus(), tccTransaction.getPattern(),
                    tccTransaction.getTransId(), currentVersion);
        } catch (TccException e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
    }

    @Override
    public int updateParticipant(final TccTransaction tccTransaction) {
        String sql = "update " + tableName + " set invocation=?  where trans_id = ?  ";
        try {
            final byte[] serialize = serializer.serialize(tccTransaction.getParticipants());
            return executeUpdate(sql, serialize, tccTransaction.getTransId());
        } catch (TccException e) {
            e.printStackTrace();
            return FAIL_ROWS;
        }
    }

    @Override
    public int updateStatus(final String id, final Integer status) {
        String sql = "update " + tableName + " set status=?  where trans_id = ?  ";
        return executeUpdate(sql, status, id);
    }

    @Override
    public TccTransaction findById(final String id) {
        String selectSql = "select * from " + tableName + " where trans_id=?";
        List<Map<String, Object>> list = executeQuery(selectSql, id);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(this::buildByResultMap)
                    .findFirst().orElse(null);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TccTransaction> listAll() {
        String selectSql = "select * from " + tableName;
        List<Map<String, Object>> list = executeQuery(selectSql);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(this::buildByResultMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TccTransaction> listAllByDelay(final Date date) {
        String sb = "select * from " + tableName + " where last_time <?";
        List<Map<String, Object>> list = executeQuery(sb, date);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.stream().filter(Objects::nonNull)
                    .map(this::buildByResultMap)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private TccTransaction buildByResultMap(final Map<String, Object> map) {
        TccTransaction tccTransaction = new TccTransaction();
        tccTransaction.setTransId((String) map.get("trans_id"));
        tccTransaction.setRetriedCount((Integer) map.get("retried_count"));
        tccTransaction.setCreateTime((Date) map.get("create_time"));
        tccTransaction.setLastTime((Date) map.get("last_time"));
        tccTransaction.setVersion((Integer) map.get("version"));
        tccTransaction.setStatus((Integer) map.get("status"));
        tccTransaction.setRole((Integer) map.get("role"));
        tccTransaction.setPattern((Integer) map.get("pattern"));
        byte[] bytes = (byte[]) map.get("invocation");
        try {
            final List<Participant> participants = serializer.deSerialize(bytes, CopyOnWriteArrayList.class);
            tccTransaction.setParticipants(participants);
        } catch (TccException e) {
            e.printStackTrace();
        }
        return tccTransaction;
    }

    @Override
    public void init(final String modelName, final TccConfig txConfig) {
        try {
            final TccDbConfig tccDbConfig = txConfig.getTccDbConfig();
            if (tccDbConfig.getDataSource() != null && StringUtils.isBlank(tccDbConfig.getUrl())) {
                dataSource = tccDbConfig.getDataSource();
            } else {
                HikariDataSource hikariDataSource = new HikariDataSource();
                hikariDataSource.setJdbcUrl(tccDbConfig.getUrl());
                hikariDataSource.setDriverClassName(tccDbConfig.getDriverClassName());
                hikariDataSource.setUsername(tccDbConfig.getUsername());
                hikariDataSource.setPassword(tccDbConfig.getPassword());
                hikariDataSource.setMaximumPoolSize(tccDbConfig.getMaxActive());
                hikariDataSource.setMinimumIdle(tccDbConfig.getMinIdle());
                hikariDataSource.setConnectionTimeout(tccDbConfig.getConnectionTimeout());
                hikariDataSource.setIdleTimeout(tccDbConfig.getIdleTimeout());
                hikariDataSource.setMaxLifetime(tccDbConfig.getMaxLifetime());
                hikariDataSource.setConnectionTestQuery(tccDbConfig.getConnectionTestQuery());
                if (tccDbConfig.getDataSourcePropertyMap() != null && !tccDbConfig.getDataSourcePropertyMap().isEmpty()) {
                    tccDbConfig.getDataSourcePropertyMap().forEach(hikariDataSource::addDataSourceProperty);
                }
                dataSource = hikariDataSource;
            }
            this.tableName = RepositoryPathUtils.buildDbTableName(modelName);
            //save current database type
            this.currentDBType = DbTypeUtils.buildByDriverClassName(tccDbConfig.getDriverClassName());
            executeUpdate(SqlHelper.buildCreateTableSql(tccDbConfig.getDriverClassName(), tableName));
        } catch (Exception e) {
            LogUtil.error(LOGGER, "hmily jdbc log init exception please check config:{}", e::getMessage);
            throw new TccRuntimeException(e);
        }
    }

    @Override
    public String getScheme() {
        return RepositorySupportEnum.DB.getSupport();
    }

    private int executeUpdate(final String sql, final Object... params) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, convertDataTypeToDB(params[i]));
                }
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("executeUpdate-> " + e.getMessage());
            return FAIL_ROWS;
        } finally {
            close(connection, ps, null);
        }

    }

    private Object convertDataTypeToDB(final Object params) {
        //https://jdbc.postgresql.org/documentation/head/8-date-time.html
        if (CommonConstant.DB_POSTGRESQL.equals(currentDBType) && params instanceof java.util.Date) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(((Date) params).getTime()), ZoneId.systemDefault());
        }
        return params;
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
                    ps.setObject(i + 1, convertDataTypeToDB(params[i]));
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
            LOGGER.error("executeQuery-> " + e.getMessage());
        } finally {
            close(connection, ps, rs);
        }
        return list;
    }

    private void close(final Connection connection, final PreparedStatement ps, final ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
